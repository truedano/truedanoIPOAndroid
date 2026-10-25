package com.example.data.repository

import com.example.data.local.SpoLotteryDao
import com.example.data.local.SpoLotteryEntity
import com.example.data.remote.TwseSubscriptionApiService
import com.example.data.remote.TwseSubscriptionParser
import com.example.domain.model.SpoLotteryRecord
import com.example.domain.repository.SpoLotteryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SpoLotteryRepositoryImpl(
    private val dao: SpoLotteryDao,
    private val apiService: TwseSubscriptionApiService
) : SpoLotteryRepository {

    override fun getAllRecords(): Flow<List<SpoLotteryRecord>> {
        return dao.getAllRecords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecordByCode(stockCode: String): SpoLotteryRecord? {
        return dao.getByStockCode(stockCode)?.toDomain()
    }

    override suspend fun insertOrUpdate(record: SpoLotteryRecord) {
        dao.insertRecords(listOf(SpoLotteryEntity.fromDomain(record)))
    }

    override suspend fun seedPresetRecordsIfEmpty() {
        // 不再注入假資料/Preset，直接從 TWSE 即時網路拉取
        fetchTwseLotteries()
    }

    override suspend fun fetchTwseLotteries(): Result<List<SpoLotteryRecord>> = withContext(Dispatchers.IO) {
        try {
            // 1. 抓取 TWSE 公開申購抽籤日程表
            val response = apiService.getPublicLotteries()
            if (response.data.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("TWSE 申購公告查無資料或伺服器無回應"))
            }

            // 2. 嘗試取得上市與上櫃最新現股收盤價 Map
            val priceMap = mutableMapOf<String, Double>()
            try {
                val twseQuotes = apiService.getTwseStockDayAll()
                for (q in twseQuotes) {
                    val code = q.code?.trim().orEmpty()
                    val price = q.closingPrice?.replace(",", "")?.toDoubleOrNull()
                    if (code.isNotBlank() && price != null && price > 0) {
                        priceMap[code] = price
                    }
                }
            } catch (_: Exception) {
                // 忽略行情 API 錯誤
            }

            try {
                val tpexQuotes = apiService.getTpexStockQuotes()
                for (q in tpexQuotes) {
                    val code = q.code?.trim().orEmpty()
                    val price = q.close?.replace(",", "")?.toDoubleOrNull()
                    if (code.isNotBlank() && price != null && price > 0) {
                        priceMap[code] = price
                    }
                }
            } catch (_: Exception) {
                // 忽略上櫃行情 API 錯誤
            }

            // 3. 解析現增標案（filterExpiredLotteries = true，嚴格排除已過抽籤日之案件）
            val parsedRecords = TwseSubscriptionParser.parseResponse(
                response = response,
                priceMap = priceMap,
                filterExpiredLotteries = true
            )

            // 4. 清除舊的歷史與範例資料，只保留最新進行中案件
            dao.clearAll()
            if (parsedRecords.isNotEmpty()) {
                val entities = parsedRecords.map { SpoLotteryEntity.fromDomain(it) }
                dao.insertRecords(entities)
            }

            Result.success(parsedRecords)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
