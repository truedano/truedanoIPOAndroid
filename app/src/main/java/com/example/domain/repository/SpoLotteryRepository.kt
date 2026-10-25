package com.example.domain.repository

import com.example.domain.model.SpoLotteryRecord
import kotlinx.coroutines.flow.Flow

/**
 * 現金增資抽籤資料 Repository 合約
 */
interface SpoLotteryRepository {

    /**
     * 觀察本地所有儲存的現增抽籤紀錄
     */
    fun getAllRecords(): Flow<List<SpoLotteryRecord>>

    /**
     * 根據股票代號查詢現增抽籤紀錄
     */
    suspend fun getRecordByCode(stockCode: String): SpoLotteryRecord?

    /**
     * 儲存或更新現增抽籤紀錄
     */
    suspend fun insertOrUpdate(record: SpoLotteryRecord)

    /**
     * 初始化內建現增實戰教學範例
     */
    suspend fun seedPresetRecordsIfEmpty()

    /**
     * 連線至 TWSE 臺灣證券交易所官方端點拉取公開申購抽籤資料與最新現股市價
     */
    suspend fun fetchTwseLotteries(): Result<List<SpoLotteryRecord>>
}
