package com.example.domain.repository

import com.example.domain.model.IpoAuctionRecord
import kotlinx.coroutines.flow.Flow

/**
 * 台灣上市櫃競拍開標資料儲存與遠端同步合約 (Clean Architecture Repository Interface)
 */
interface IpoAuctionRepository {

    /**
     * 觀察本地所有儲存的競拍標案紀錄（依開標日期降冪排列）
     */
    fun getAllRecords(): Flow<List<IpoAuctionRecord>>

    /**
     * 根據股票代號查詢競拍紀錄
     */
    suspend fun getRecordByCode(stockCode: String): IpoAuctionRecord?

    /**
     * 儲存或更新競拍紀錄（支援手動覆寫參數）
     */
    suspend fun insertOrUpdate(record: IpoAuctionRecord)

    /**
     * 刪除指定標案紀錄
     */
    suspend fun deleteRecord(stockCode: String)

    /**
     * 清空所有本地紀錄
     */
    suspend fun clearAllRecords()

    /**
     * 連線至 TWSE 臺灣證券交易所官方端點拉取競拍資料
     */
    suspend fun fetchTwseAuctions(startDate: String, endDate: String): Result<List<IpoAuctionRecord>>

    /**
     * 初始化內建經典驗證案例（7825 和亞、7856 漢測、7734 巧新等）
     */
    suspend fun seedPresetRecordsIfEmpty()
}
