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
    val analysisDate: String = "2026-10-26"
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

    /**
     * 判斷是否為進行中／未過期標案
     * 規則：
     * 1. 有掛牌日：掛牌日尚未過去（或在掛牌 7 天蜜月期內）
     * 2. 有抽籤日：抽籤日尚未過去（或抽籤後 10 天內等待掛牌）
     * 3. 無掛牌與抽籤日：開標日在 30 天以內
     */
    fun isActive(today: LocalDate = LocalDate.now()): Boolean {
        if (listingDate != null && listingDate.isNotBlank()) {
            val parsedListing = parseDateSafe(listingDate)
            if (parsedListing != null) {
                return !today.isAfter(parsedListing.plusDays(7))
            }
        }

        if (lotteryDate != null && lotteryDate.isNotBlank()) {
            val parsedLottery = parseDateSafe(lotteryDate)
            if (parsedLottery != null) {
                return !today.isAfter(parsedLottery.plusDays(10))
            }
        }

        val parsedAuction = parseDateSafe(auctionDate) ?: return true
        return !today.isAfter(parsedAuction.plusDays(30))
    }

    /**
     * 取得進行中時程標記文字（供 Chips 快速顯示）
     */
    val timingBadge: String
        get() = when {
            lotteryDate != null && lotteryDate.isNotBlank() -> "$lotteryDate 抽籤"
            listingDate != null && listingDate.isNotBlank() -> "$listingDate 掛牌"
            else -> "$auctionDate 開標"
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
    GOLDEN("GOLDEN HONEYMOON 🏆 黃金公式"),
    SAFE("SAFE"),
    CRITICAL_DANGER("CRITICAL DANGER")
}
