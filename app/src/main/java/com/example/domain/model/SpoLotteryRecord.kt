package com.example.domain.model

import java.time.LocalDate

/**
 * 台灣已上市櫃公司現金增資（SPO）公開申購抽籤紀錄
 */
enum class SpoStage {
    OPEN,        // 開放申購中 (startDate <= today <= endDate)
    UPCOMING,    // 即將開放申購 (today < startDate)
    BALLOTING,   // 申購已截止，等待抽籤開獎 (endDate < today <= lotteryDate)
    CLOSED       // 抽籤結束 (today > lotteryDate)
}

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
     * 取得當前抽籤決策生命週期階段
     */
    fun getStage(today: LocalDate = LocalDate.now()): SpoStage {
        val start = parseDateSafe(startDate)
        val end = parseDateSafe(endDate)
        val lottery = parseDateSafe(lotteryDate)

        if (lottery != null && today.isAfter(lottery)) {
            return SpoStage.CLOSED
        }
        if (end != null && today.isAfter(end)) {
            return SpoStage.BALLOTING
        }
        if (start != null && today.isBefore(start)) {
            return SpoStage.UPCOMING
        }
        return SpoStage.OPEN
    }

    /**
     * 取得抽籤決策狀態徽章文字
     */
    fun getStatusBadge(today: LocalDate = LocalDate.now()): String {
        return when (getStage(today)) {
            SpoStage.OPEN -> {
                val end = parseDateSafe(endDate)
                if (end != null) {
                    val daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, end)
                    if (daysRemaining <= 0L) "🔥 今日截止"
                    else "🔥 申購中(剩${daysRemaining}天)"
                } else "🔥 申購中"
            }
            SpoStage.UPCOMING -> {
                val start = parseDateSafe(startDate)
                if (start != null) {
                    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, start)
                    if (daysUntil == 1L) "⏳ 明日開放"
                    else "⏳ ${start.monthValue}/${start.dayOfMonth} 開放"
                } else "⏳ 即將開放"
            }
            SpoStage.BALLOTING -> {
                val lottery = parseDateSafe(lotteryDate)
                if (lottery != null && today == lottery) "🎉 今日抽籤"
                else if (lottery != null) "⏳ 待開獎(${lottery.monthValue}/${lottery.dayOfMonth})"
                else "⏳ 等待開獎"
            }
            SpoStage.CLOSED -> "已結束"
        }
    }

    /**
     * 判斷抽籤日是否尚未過期（抽籤日 >= 今天）
     * 針對「輔助投資人抽籤決策」定位：抽籤日當天或之前為有效決策期，過期則移除
     */
    fun isLotteryActive(today: LocalDate = LocalDate.now()): Boolean {
        return getStage(today) != SpoStage.CLOSED
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
