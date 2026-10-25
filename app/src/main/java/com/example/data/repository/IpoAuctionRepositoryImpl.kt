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

                // 2. 嘗試抓取公開申購抽籤日程 (初上市與初上櫃案件)，獲取抽籤日與承銷價
                val lotteryDateMap = mutableMapOf<String, String>()
                val subPriceMap = mutableMapOf<String, Double>()

                try {
                    val subResponse = subscriptionApiService?.getPublicLotteries()
                    val rows = subResponse?.data
                    if (rows != null) {
                        for (row in rows) {
                            val code = row.getOrNull(3)?.trim().orEmpty()
                            val marketType = row.getOrNull(4)?.trim().orEmpty()
                            val rawLotteryDate = row.getOrNull(1)?.trim().orEmpty()
                            val rawSubPrice = row.getOrNull(10)?.trim().orEmpty()

                            if (code.length == 4 && (marketType.contains("初上市") || marketType.contains("初上櫃"))) {
                                if (rawLotteryDate.isNotBlank()) {
                                    lotteryDateMap[code] = TwseParser.normalizeTwseDate(rawLotteryDate)
                                }
                                val price = TwseParser.parseDoubleSafe(rawSubPrice)
                                if (price > 0.0) {
                                    subPriceMap[code] = price
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
                )

                // 4. 只保留進行中／未過期（掛牌7天內或抽籤10天內）之 IPO 案件
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
