package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.IpoAuctionRecord

@Entity(tableName = "ipo_auctions")
data class IpoAuctionEntity(
    @PrimaryKey
    val stockCode: String,
    val stockName: String,
    val auctionDate: String,
    val totalAuctionShares: Long,
    val validBidShares: Long,
    val minWinningPrice: Double,
    val avgWeightedPrice: Double,
    val maxWinningPrice: Double?,
    val subscriptionPrice: Double,
    val emergingMarketPrice: Double?,
    val note: String,
    val isPreset: Boolean,
    val listingDate: String? = null,
    val lotteryDate: String? = null,
    val analysisDate: String = "2026-10-26",
    val minBidPrice: Double? = null,
    val biddingStartDate: String? = null,
    val biddingEndDate: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomain(): IpoAuctionRecord {
        return IpoAuctionRecord(
            stockCode = stockCode,
            stockName = stockName,
            auctionDate = auctionDate,
            totalAuctionShares = totalAuctionShares,
            validBidShares = validBidShares,
            minWinningPrice = minWinningPrice,
            avgWeightedPrice = avgWeightedPrice,
            maxWinningPrice = maxWinningPrice,
            subscriptionPrice = subscriptionPrice,
            emergingMarketPrice = emergingMarketPrice,
            note = note,
            isPreset = isPreset,
            listingDate = listingDate,
            lotteryDate = lotteryDate,
            analysisDate = analysisDate,
            minBidPrice = minBidPrice,
            biddingStartDate = biddingStartDate,
            biddingEndDate = biddingEndDate
        )
    }

    companion object {
        fun fromDomain(record: IpoAuctionRecord): IpoAuctionEntity {
            return IpoAuctionEntity(
                stockCode = record.stockCode,
                stockName = record.stockName,
                auctionDate = record.auctionDate,
                totalAuctionShares = record.totalAuctionShares,
                validBidShares = record.validBidShares,
                minWinningPrice = record.minWinningPrice,
                avgWeightedPrice = record.avgWeightedPrice,
                maxWinningPrice = record.maxWinningPrice,
                subscriptionPrice = record.subscriptionPrice,
                emergingMarketPrice = record.emergingMarketPrice,
                note = record.note,
                isPreset = record.isPreset,
                listingDate = record.listingDate,
                lotteryDate = record.lotteryDate,
                analysisDate = record.analysisDate,
                minBidPrice = record.minBidPrice,
                biddingStartDate = record.biddingStartDate,
                biddingEndDate = record.biddingEndDate
            )
        }
    }
}
