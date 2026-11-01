package com.example.domain.model

import java.time.LocalDate

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
    val lotteryDate: String? = null,
    val analysisDate: String = "2026-10-26",
    val minBidPrice: Double? = null,
    val biddingStartDate: String? = null,
    val biddingEndDate: String? = null
) {
    /**
     * 計算合格投標倍數（合格投標量 / 競拍釋出總量）
     */
    /**
     * 判定是否已開標完成 (最低得標價與加權均價皆大於 0)
     */
    val isAuctionCompleted: Boolean
        get() = minWinningPrice > 0.0 && avgWeightedPrice > 0.0

    /**
     * 計算合格投標倍數（合格投標量 / 競拍釋出總量）
     */
    val bidRatio: Double
        get() = if (isAuctionCompleted && totalAuctionShares > 0) {
            validBidShares.toDouble() / totalAuctionShares.toDouble()
        } else 0.0

    /**
     * 安全邊際 = 最低得標價 - 公開申購承銷價
     */
    val safetyMargin: Double
        get() = if (isAuctionCompleted) {
            minWinningPrice - subscriptionPrice
        } else 0.0

    /**
     * 行情分類狀態：
     * 1. PENDING_AUCTION: 尚未開標
     * 2. GOLDEN: bidRatio >= 5.0 && safetyMargin > 0
     * 3. SAFE: safetyMargin > 0 && bidRatio < 5.0
     * 4. CRITICAL_DANGER: safetyMargin <= 0
     */
    val statusType: IpoStatusType
        get() = when {
            !isAuctionCompleted -> IpoStatusType.PENDING_AUCTION
            bidRatio >= 5.0 && safetyMargin > 0.0 -> IpoStatusType.GOLDEN
            safetyMargin > 0.0 -> IpoStatusType.SAFE
            else -> IpoStatusType.CRITICAL_DANGER
        }

    /**
     * 判斷是否為進行中／未過期標案
     * 針對「輔助投資人抽籤決策」定位規則：
     * 1. 有抽籤日：抽籤日當天或之前為有效決策期（today <= lotteryDate），過期則移入歷史
     * 2. 無抽籤日但有掛牌日：掛牌日前仍可能籌備抽籤（today <= listingDate）
     * 3. 無掛牌與抽籤日：開標日 14 天內有效
     */
    fun isActive(today: LocalDate = LocalDate.now()): Boolean {
        if (!isAuctionCompleted) {
            val parsedAuction = parseDateSafe(auctionDate)
            if (parsedAuction != null) {
                return !today.isAfter(parsedAuction)
            }
        }

        if (lotteryDate != null && lotteryDate.isNotBlank()) {
            val parsedLottery = parseDateSafe(lotteryDate)
            if (parsedLottery != null) {
                return !today.isAfter(parsedLottery)
            }
        }

        if (listingDate != null && listingDate.isNotBlank()) {
            val parsedListing = parseDateSafe(listingDate)
            if (parsedListing != null) {
                return !today.isAfter(parsedListing)
            }
        }

        val parsedAuction = parseDateSafe(auctionDate) ?: return true
        return !today.isAfter(parsedAuction.plusDays(14))
    }

    /**
     * 取得進行中時程標記文字（供 Chips 與抽籤決策快速顯示）
     */
    val timingBadge: String
        get() {
            val today = LocalDate.now()
            if (!isAuctionCompleted) {
                val parsedAuction = parseDateSafe(auctionDate)
                if (parsedAuction != null && today == parsedAuction) {
                    return "🎉 今日開標"
                }
                if (biddingEndDate != null && biddingEndDate.isNotBlank()) {
                    val parsedEnd = parseDateSafe(biddingEndDate)
                    if (parsedEnd != null && !today.isAfter(parsedEnd)) {
                        return "🔥 投標中"
                    }
                }
                return if (parsedAuction != null) "${parsedAuction.monthValue}/${parsedAuction.dayOfMonth} 開標" else "$auctionDate 開標"
            }
            if (lotteryDate != null && lotteryDate.isNotBlank()) {
                val parsedLottery = parseDateSafe(lotteryDate)
                if (parsedLottery != null && today == parsedLottery) {
                    return "🎉 今日抽籤"
                }
                return "$lotteryDate 抽籤"
            }
            if (listingDate != null && listingDate.isNotBlank()) {
                return "$listingDate 掛牌"
            }
            return "$auctionDate 開標"
        }

    private fun parseDateSafe(str: String): LocalDate? {
        return try {
            val clean = str.trim().replace("/", "-")
            val parts = clean.split("-")
            if (parts.size == 3) {
                val y = parts[0].toInt()
                val m = parts[1].toInt()
                val d = parts[2].toInt()
                LocalDate.of(y, m, d)
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

enum class IpoStatusType(val label: String) {
    PENDING_AUCTION("📢 競拍進行中（未開標）"),
    GOLDEN("GOLDEN HONEYMOON 🏆 黃金公式"),
    SAFE("SAFE"),
    CRITICAL_DANGER("CRITICAL DANGER")
}
