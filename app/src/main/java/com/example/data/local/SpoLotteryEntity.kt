package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.SpoLotteryRecord

@Entity(tableName = "spo_lotteries")
data class SpoLotteryEntity(
    @PrimaryKey
    val stockCode: String,
    val stockName: String,
    val subscriptionPrice: Double,
    val marketPrice: Double,
    val startDate: String,
    val endDate: String,
    val lotteryDate: String,
    val listingDate: String,
    val totalLots: Long,
    val sharesPerLot: Int = 1,
    val shortSaleAvailable: Boolean = false,
    val shortSaleQuota: Long? = null,
    val dilutionRate: Double? = null,
    val note: String = "",
    val isPreset: Boolean = false,
    val analysisDate: String = "2026-10-26",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomain(): SpoLotteryRecord {
        return SpoLotteryRecord(
            stockCode = stockCode,
            stockName = stockName,
            subscriptionPrice = subscriptionPrice,
            marketPrice = marketPrice,
            startDate = startDate,
            endDate = endDate,
            lotteryDate = lotteryDate,
            listingDate = listingDate,
            totalLots = totalLots,
            sharesPerLot = sharesPerLot,
            shortSaleAvailable = shortSaleAvailable,
            shortSaleQuota = shortSaleQuota,
            dilutionRate = dilutionRate,
            note = note,
            isPreset = isPreset,
            analysisDate = analysisDate
        )
    }

    companion object {
        fun fromDomain(record: SpoLotteryRecord): SpoLotteryEntity {
            return SpoLotteryEntity(
                stockCode = record.stockCode,
                stockName = record.stockName,
                subscriptionPrice = record.subscriptionPrice,
                marketPrice = record.marketPrice,
                startDate = record.startDate,
                endDate = record.endDate,
                lotteryDate = record.lotteryDate,
                listingDate = record.listingDate,
                totalLots = record.totalLots,
                sharesPerLot = record.sharesPerLot,
                shortSaleAvailable = record.shortSaleAvailable,
                shortSaleQuota = record.shortSaleQuota,
                dilutionRate = record.dilutionRate,
                note = record.note,
                isPreset = record.isPreset,
                analysisDate = record.analysisDate
            )
        }
    }
}
