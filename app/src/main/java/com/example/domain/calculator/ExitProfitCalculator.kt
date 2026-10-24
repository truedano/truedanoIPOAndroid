package com.example.domain.calculator

import com.example.domain.model.ExitScenarioResult
import com.example.domain.model.ExitScenarioType
import com.example.domain.model.TickSizeAligner
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * 純抽籤戶實戰損益計算機
 *
 * 規則與稅費：
 * 1. 投入本金：申購價 × 1000 × 張數 + 申購處理費 20 元 + 中籤處理費 50 元 × 張數（單張標準為 70 元規費）。
 * 2. 證券交易稅：賣出總額 × 0.3%（0.003）。
 * 3. 券商賣出手續費：賣出總額 × 0.1425%（0.001425）× 手續費折讓率，最低法定基本 20 元。
 * 4. 實拿淨額 = 賣出總額 - 證券交易稅 - 賣出手續費。
 * 5. 淨利潤 = 實拿淨額 - 投入本金。
 * 6. 預估 ROI % = (淨利潤 / 投入本金) × 100%。
 */
object ExitProfitCalculator {

    const val REGULAR_APPLICATION_FEE = 20.0 // 投標/申購手續費
    const val LOTTERY_PROCESSING_FEE_PER_LOT = 50.0 // 中籤處理費每張 50 元
    const val TAX_RATE = 0.003 // 0.3%
    const val BROKER_FEE_RATE = 0.001425 // 0.1425%
    const val MIN_BROKER_FEE = 20.0 // 最小手續費

    /**
     * 計算單張/多張所需準備總本金
     */
    fun calculateInvestedCapital(subscriptionPrice: Double, lots: Int = 1): Double {
        val stockCapital = subscriptionPrice * 1000.0 * lots
        val fees = REGULAR_APPLICATION_FEE + (LOTTERY_PROCESSING_FEE_PER_LOT * lots)
        return stockCapital + fees
    }

    /**
     * 計算指定出場價位的損益結果
     */
    fun calculateScenario(
        type: ExitScenarioType,
        subscriptionPrice: Double,
        targetPrice: Double,
        lots: Int = 1,
        feeDiscount: Double = 1.0 // 預設 100% (無折讓)，可傳入 0.6 (6折)、0.28 (2.8折) 等
    ): ExitScenarioResult {
        // 對齊台股真實升降單位
        val alignedPrice = when (type) {
            ExitScenarioType.CONSERVATIVE -> TickSizeAligner.alignDown(targetPrice)
            ExitScenarioType.BASE -> TickSizeAligner.alignNearest(targetPrice)
            ExitScenarioType.OPTIMISTIC -> TickSizeAligner.alignUp(targetPrice)
        }

        val investedCapital = calculateInvestedCapital(subscriptionPrice, lots)
        val grossRevenue = alignedPrice * 1000.0 * lots

        // 交易稅 0.3% 四捨五入
        val tax = (grossRevenue * TAX_RATE).roundToLong().toDouble()

        // 手續費 0.1425% * 折讓，四捨五入後取與最低 20 元之最大值
        val rawBrokerFee = (grossRevenue * BROKER_FEE_RATE * feeDiscount).roundToLong().toDouble()
        val brokerFee = max(MIN_BROKER_FEE, rawBrokerFee)

        val totalExitDeductions = tax + brokerFee
        val netProceeds = grossRevenue - totalExitDeductions
        val netProfit = netProceeds - investedCapital
        val roiPercent = if (investedCapital > 0.0) {
            (netProfit / investedCapital) * 100.0
        } else 0.0

        return ExitScenarioResult(
            type = type,
            targetPrice = targetPrice,
            alignedTargetPrice = alignedPrice,
            lots = lots,
            grossRevenue = grossRevenue,
            securitiesTax = tax,
            brokerFee = brokerFee,
            totalExitDeductions = totalExitDeductions,
            netProceeds = netProceeds,
            investedCapital = investedCapital,
            netProfit = netProfit,
            roiPercent = roiPercent
        )
    }
}
