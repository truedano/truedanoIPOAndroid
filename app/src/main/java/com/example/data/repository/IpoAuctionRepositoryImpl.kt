package com.example.data.repository

import com.example.data.local.IpoAuctionDao
import com.example.data.local.IpoAuctionEntity
import com.example.data.local.PresetCases
import com.example.data.remote.TwseAuctionApiService
import com.example.data.remote.TwseParser
import com.example.domain.model.IpoAuctionRecord
import com.example.domain.repository.IpoAuctionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IpoAuctionRepositoryImpl(
    private val dao: IpoAuctionDao,
    private val apiService: TwseAuctionApiService
) : IpoAuctionRepository {

    override fun getAllRecords(): Flow<List<IpoAuctionRecord>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getRecordByCode(stockCode: String): IpoAuctionRecord? = withContext(Dispatchers.IO) {
        val entity = dao.getByCode(stockCode)
        if (entity != null) {
            return@withContext entity.toDomain()
        }
        // 若本地資料庫未找到，從預設清單尋找備援
        PresetCases.ALL_PRESETS.find { it.stockCode == stockCode }
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
                val response = apiService.getAuctionAnnouncements(
                    startDate = startDate,
                    endDate = endDate
                )
                val parsedRecords = TwseParser.parseResponse(response)
                // 智慧調整：不再強制將整批 API 歷史全部存入使用者歷史資料庫，
                // 僅回傳解析後的普通股標案清單供查詢比對，維持歷史紀錄純淨。
                Result.success(parsedRecords)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun seedPresetRecordsIfEmpty() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count == 0) {
            val entities = PresetCases.ALL_PRESETS.map { IpoAuctionEntity.fromDomain(it) }
            dao.insertAll(entities)
        }
    }
}
