package com.example.domain.model

import java.time.LocalDate

/**
 * 台灣已上市櫃公司現金增資（SPO）公開申購抽籤紀錄
 */
data class SpoLotteryRecord(
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
    val analysisDate: String = "2026-10-26"
) {
    /**
     * 每股折價差額 (元) = 現股市價 - 申購承銷價
     */
    val discountSpread: Double
        get() = marketPrice - subscriptionPrice

    /**
     * 折價幅度 (%) = (現股市價 - 申購承銷價) / 現股市價 * 100
     */
    val discountRatio: Double
        get() = if (marketPrice > 0.0) {
            ((marketPrice - subscriptionPrice) / marketPrice) * 100.0
        } else 0.0

    /**
     * 每張（1,000股）預估現貨價差金額 (元)
     */
    val spreadAmountPerLot: Double
        get() = discountSpread * sharesPerLot * 1000.0

    /**
     * 投資報酬率預估 (%) = (市價 - 承銷價) / 承銷價 * 100
     */
    val roiPercent: Double
        get() = if (subscriptionPrice > 0.0) {
            ((marketPrice - subscriptionPrice) / subscriptionPrice) * 100.0
        } else 0.0

    /**
     * 預估波動暴露交易天數（從申購截止到撥券上市日，預設約 7 交易日）
     */
    val estimatedExposureDays: Int
        get() = 7

    /**
     * 是否為有效案件（撥券日尚未過期 7 天以上）
     */
    fun isActive(today: LocalDate = LocalDate.now()): Boolean {
        if (isPreset) return true
        val parsed = parseDateSafe(listingDate) ?: parseDateSafe(endDate) ?: return true
        return !today.isAfter(parsed.plusDays(7))
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
