package com.example.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

/**
 * 台灣證券交易所 (TWSE) 6 檔升降單位 (Tick Size) 強制對齊計算機
 *
 * 規則：
 * 1. 價格 < 10 元：0.01 元
 * 2. 10 ~ 50 元：0.05 元
 * 3. 50 ~ 100 元：0.10 元
 * 4. 100 ~ 500 元：0.50 元
 * 5. 500 ~ 1000 元：1.00 元
 * 6. >= 1000 元：5.00 元
 */
object TickSizeAligner {

    /**
     * 取得指定價格所處級距之升降單位
     */
    fun getTickSize(price: Double): Double {
        if (price < 0.0) return 0.01
        return when {
            price < 10.0 -> 0.01
            price < 50.0 -> 0.05
            price < 100.0 -> 0.10
            price < 500.0 -> 0.50
            price < 1000.0 -> 1.00
            else -> 5.00
        }
    }

    /**
     * 向下對齊至真實可交易檔位（適合下檔支撐位計算）
     */
    fun alignDown(price: Double): Double {
        if (price <= 0.0) return 0.0
        val tick = getTickSize(price)
        val tickBd = BigDecimal.valueOf(tick)
        val priceBd = BigDecimal.valueOf(price)

        val steps = priceBd.divide(tickBd, 0, RoundingMode.FLOOR)
        val aligned = steps.multiply(tickBd).setScale(2, RoundingMode.HALF_UP)
        return aligned.toDouble()
    }

    /**
     * 向上對齊至真實可交易檔位（適合上檔壓力位計算）
     */
    fun alignUp(price: Double): Double {
        if (price <= 0.0) return 0.0
        val tick = getTickSize(price)
        val tickBd = BigDecimal.valueOf(tick)
        val priceBd = BigDecimal.valueOf(price)

        val steps = priceBd.divide(tickBd, 0, RoundingMode.CEILING)
        val aligned = steps.multiply(tickBd).setScale(2, RoundingMode.HALF_UP)
        return aligned.toDouble()
    }

    /**
     * 四捨五入對齊至最近的真實可交易檔位（適合多空中樞價計算）
     */
    fun alignNearest(price: Double): Double {
        if (price <= 0.0) return 0.0
        val tick = getTickSize(price)
        val tickBd = BigDecimal.valueOf(tick)
        val priceBd = BigDecimal.valueOf(price)

        val steps = priceBd.divide(tickBd, 0, RoundingMode.HALF_UP)
        val aligned = steps.multiply(tickBd).setScale(2, RoundingMode.HALF_UP)
        return aligned.toDouble()
    }

    /**
     * 格式化價格為台股標準顯示字串
     */
    fun formatPrice(price: Double): String {
        return if (price >= 100.0) {
            String.format("%.1f", price)
        } else {
            String.format("%.2f", price)
        }
    }
}
