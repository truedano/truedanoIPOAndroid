package com.example.domain.model

/**
 * 台灣上市櫃競拍（IPO）開標真實紀錄
 */
data class IpoAuctionRecord(
    val stockCode: String,
    val stockName: String,
    val auctionDate: String,
    val totalAuctionShares: Long,
    val validBidShares: Long,
    val minWinningPrice: Double,
    val avgWeightedPrice: Double,
    val maxWinningPrice: Double? = null,
    val subscriptionPrice: Double,
    val emergingMarketPrice: Double? = null,
    val note: String = "",
    val isPreset: Boolean = false,
    val listingDate: String? = null,
    val analysisDate: String = "2026-10-24"
) {
    /**
     * 計算合格投標倍數（合格投標量 / 競拍釋出總量）
     */
    val bidRatio: Double
        get() = if (totalAuctionShares > 0) {
            validBidShares.toDouble() / totalAuctionShares.toDouble()
        } else 0.0

    /**
     * 安全邊際 = 最低得標價 - 公開申購承銷價
     */
    val safetyMargin: Double
        get() = minWinningPrice - subscriptionPrice

    /**
     * 行情分類狀態：
     * 1. GOLDEN: bidRatio >= 5.0 && safetyMargin > 0
     * 2. SAFE: safetyMargin > 0 && bidRatio < 5.0
     * 3. CRITICAL_DANGER: safetyMargin <= 0
     */
    val statusType: IpoStatusType
        get() = when {
            bidRatio >= 5.0 && safetyMargin > 0.0 -> IpoStatusType.GOLDEN
            safetyMargin > 0.0 -> IpoStatusType.SAFE
            else -> IpoStatusType.CRITICAL_DANGER
        }
}

enum class IpoStatusType(val label: String) {
    GOLDEN("GOLDEN HONEYMOON 🏆 黃金公式"),
    SAFE("SAFE"),
    CRITICAL_DANGER("CRITICAL DANGER")
}
