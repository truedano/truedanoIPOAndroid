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

    /**
     * 判斷是否為進行中／未過期標案
     * 規則：
     * 1. 內建教學案例 (isPreset) 一律視為有效
     * 2. 有掛牌日：掛牌日尚未過去（或在掛牌 7 天蜜月期內）
     * 3. 無掛牌日：開標日在 30 天以內
     */
    fun isActive(today: LocalDate = LocalDate.now()): Boolean {
        if (isPreset) return true

        val dateToCompare = listingDate?.takeIf { it.isNotBlank() } ?: auctionDate
        val parsed = parseDateSafe(dateToCompare) ?: return true

        val expirationDate = if (listingDate != null) parsed.plusDays(7) else parsed.plusDays(30)
        return !today.isAfter(expirationDate)
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
