package com.example.data.repository

import com.example.data.local.IpoAuctionDao
import com.example.data.local.IpoAuctionEntity
import com.example.data.remote.TwseAuctionApiService
import com.example.data.remote.TwseParser
import com.example.data.remote.TwseSubscriptionApiService
import com.example.domain.model.IpoAuctionRecord
import com.example.domain.repository.IpoAuctionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IpoAuctionRepositoryImpl(
    private val dao: IpoAuctionDao,
    private val apiService: TwseAuctionApiService,
    private val subscriptionApiService: TwseSubscriptionApiService? = null
) : IpoAuctionRepository {

    override fun getAllRecords(): Flow<List<IpoAuctionRecord>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getRecordByCode(stockCode: String): IpoAuctionRecord? = withContext(Dispatchers.IO) {
        val entity = dao.getByCode(stockCode)
        entity?.toDomain()
    }

    override suspend fun insertOrUpdate(record: IpoAuctionRecord) = withContext(Dispatchers.IO) {
        dao.insert(IpoAuctionEntity.fromDomain(record))
    }

    override suspend fun deleteRecord(stockCode: String) = withContext(Dispatchers.IO) {
        dao.deleteByCode(stockCode)
    }

    override suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    override suspend fun fetchTwseAuctions(startDate: String, endDate: String): Result<List<IpoAuctionRecord>> =
        withContext(Dispatchers.IO) {
            try {
                // 1. 抓取競價拍賣開標公告
                val auctionResponse = apiService.getAuctionAnnouncements(
                    startDate = startDate,
                    endDate = endDate
                )

                // 2. 嘗試抓取公開申購抽籤日程 (初上市與初上櫃案件)，獲取抽籤日、承銷價 (含實際承銷價與暫定承銷價回退)
                val lotteryDateMap = mutableMapOf<String, String>()
                val subPriceMap = mutableMapOf<String, Double>()
                val upcomingIpoList = mutableListOf<IpoAuctionRecord>()

                try {
                    val subResponse = subscriptionApiService?.getPublicLotteries()
                    val rows = subResponse?.data
                    if (rows != null) {
                        for (row in rows) {
                            val code = row.getOrNull(3)?.trim().orEmpty()
                            val name = row.getOrNull(2)?.trim().orEmpty()
                            val marketType = row.getOrNull(4)?.trim().orEmpty()
                            val rawLotteryDate = row.getOrNull(1)?.trim().orEmpty()
                            val rawTentativePrice = row.getOrNull(9)?.trim().orEmpty()
                            val rawActualPrice = row.getOrNull(10)?.trim().orEmpty()
                            val rawListingDate = row.getOrNull(11)?.trim().orEmpty()
                            val rawShares = row.getOrNull(8)?.trim().orEmpty().ifBlank { row.getOrNull(7)?.trim().orEmpty() }

                            if (code.length == 4 && (marketType.contains("初上市") || marketType.contains("初上櫃"))) {
                                val formattedLotteryDate = if (rawLotteryDate.isNotBlank()) TwseParser.normalizeTwseDate(rawLotteryDate) else null
                                val formattedListingDate = if (rawListingDate.isNotBlank()) TwseParser.normalizeTwseDate(rawListingDate) else null

                                if (formattedLotteryDate != null) {
                                    lotteryDateMap[code] = formattedLotteryDate
                                }

                                val actualPrice = TwseParser.parseDoubleSafe(rawActualPrice)
                                val tentativePrice = TwseParser.parseDoubleSafe(rawTentativePrice)
                                // 實際承銷價若為「未訂出」，自動回退使用「承銷價 (暫定承銷價)」
                                val price = if (actualPrice > 0.0) actualPrice else tentativePrice

                                if (price > 0.0) {
                                    subPriceMap[code] = price

                                    val isTentative = actualPrice <= 0.0 && tentativePrice > 0.0
                                    val totalShares = (TwseParser.parseLongSafe(rawShares) / 1000L).coerceAtLeast(1L)
                                    val noteText = if (isTentative) {
                                        "【證交所公開申購排程】暫定承銷價 $price 元（實際承銷價未訂出，開標後將自動更新最終定價）"
                                    } else {
                                        "【證交所公開申購排程】已敲定實際承銷價 $price 元"
                                    }

                                    upcomingIpoList.add(
                                        IpoAuctionRecord(
                                            stockCode = code,
                                            stockName = name,
                                            auctionDate = formattedLotteryDate ?: "即將開標",
                                            totalAuctionShares = totalShares,
                                            validBidShares = totalShares,
                                            minWinningPrice = price,
                                            avgWeightedPrice = price,
                                            maxWinningPrice = price,
                                            subscriptionPrice = price,
                                            lotteryDate = formattedLotteryDate,
                                            listingDate = formattedListingDate,
                                            note = noteText
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // 若申購表連線失敗，不影響競拍本體
                }

                // 3. 雙表關聯解析
                val parsedRecords = TwseParser.parseResponse(
                    response = auctionResponse,
                    lotteryDateMap = lotteryDateMap,
                    subPriceMap = subPriceMap
                ).toMutableList()

                // 補全：如果申購日程表已有「初上市/初上櫃」標案，但競拍開標公告尚未有紀錄 (如未開標之新股)，將其補入清單
                val existingCodes = parsedRecords.map { it.stockCode }.toSet()
                for (upcoming in upcomingIpoList) {
                    if (upcoming.stockCode !in existingCodes) {
                        parsedRecords.add(upcoming)
                    }
                }

                // 4. 只保留抽籤日未過期 (today <= lotteryDate) 之 IPO 標案
                val activeRecords = parsedRecords.filter { it.isActive() }

                // 5. 更新本地資料庫（清空過期舊資料，只寫入最新真實進行中標案）
                dao.clearAll()
                if (activeRecords.isNotEmpty()) {
                    dao.insertAll(activeRecords.map { IpoAuctionEntity.fromDomain(it) })
                }

                Result.success(activeRecords)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun seedPresetRecordsIfEmpty(): Unit = withContext(Dispatchers.IO) {
        // 不再注入死假資料，直接動態發起一次線上同步
        fetchTwseAuctions(startDate = "20250101", endDate = "20261231")
        Unit
    }
}
