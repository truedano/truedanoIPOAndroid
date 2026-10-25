package com.example.domain.calculator

import com.example.domain.model.SpoDecisionResult
import com.example.domain.model.SpoLotteryRecord
import com.example.domain.model.SpoRiskGrade
import com.example.domain.model.SpoScenarioProjection
import com.example.domain.model.TickSizeAligner
import kotlin.math.roundToInt

/**
 * 台灣已上市櫃現金增資（SPO）公開抽籤決策推演引擎
 */
object SpoDecisionEngine {

    private const val STOCK_TRANSACTION_TAX = 0.003    // 證券交易稅 0.3%
    private const val BROKERAGE_FEE_RATE = 0.001425    // 券商手續費 0.1425%
    private const val LOTTERY_HANDLING_FEE = 20.0      // 申購處理費 20 元
    private const val WINNING_FEE = 50.0               // 中籤處理費 50 元

    /**
     * 執行現金增資決策推演
     */
    fun evaluate(
        record: SpoLotteryRecord,
        lots: Int = 1,
        feeDiscount: Double = 1.0
    ): SpoDecisionResult {
        val discountRatio = record.discountRatio
        val isArbitrage = record.shortSaleAvailable && (record.shortSaleQuota == null || record.shortSaleQuota > 50L)
        val dilutionWarning = (record.dilutionRate ?: 0.0) >= 15.0

        // 1. 判定風險等級
        val grade = when {
            isArbitrage && discountRatio >= 8.0 -> SpoRiskGrade.ARBITRAGE_GOLDEN
            discountRatio >= 20.0 && !dilutionWarning -> SpoRiskGrade.SAFE_BUFFER
            discountRatio >= 10.0 -> SpoRiskGrade.MODERATE
            else -> SpoRiskGrade.HIGH_DANGER
        }

        // 2. 三段出場情境試算（依據撥券日現股市價變動假設）
        val scenarios = calculateScenarios(record, lots, feeDiscount)

        // 3. 核心分析重點
        val points = mutableListOf<String>()
        points.add("折價空間：現價 ${record.marketPrice} 元 vs 承銷價 ${record.subscriptionPrice} 元，折價率 ${String.format("%.1f", discountRatio)}%")
        points.add("每張預估毛利：約 NT$ ${String.format("%,d", record.spreadAmountPerLot.roundToInt())} 元")

        if (isArbitrage) {
            points.add("融券避險可行：目前支援融券放空，可於申購或抽中時借券/融券鎖利，消除約 7 交易日的下行風險！")
        } else {
            points.add("波動暴露期：申購至撥券約需 6~8 個交易日，無融券保護，需直接承受現貨下行風險。")
        }

        if (record.dilutionRate != null) {
            if (dilutionWarning) {
                points.add("🚨 股本稀釋警告：增資規模達 ${String.format("%.1f", record.dilutionRate)}%，撥券日將有顯著籌碼出籠倒貨賣壓！")
            } else {
                points.add("股本稀釋度：增資比例 ${String.format("%.1f", record.dilutionRate)}%，屬可控範圍。")
            }
        }

        // 4. 產生摘要與行動建議
        val (summary, actionRec) = when (grade) {
            SpoRiskGrade.ARBITRAGE_GOLDEN -> {
                "【頂級套利】折價超過 8% 且具備融券避險額度" to
                "強烈建議參與！若持有融券配額，可在中籤通知確認後立即以現股市價進行融券放空，於撥券日以現增股票現券償還，無風險鎖定價差。"
            }
            SpoRiskGrade.SAFE_BUFFER -> {
                "【安全防護足】折價幅度高達 ${String.format("%.1f", discountRatio)}%，具備強烈安全墊" to
                "建議積極申購。超過 20% 的折價率即使在撥券日前遭遇大盤急跌回檔，仍有充足安全防護空間不易跌破承銷價。"
            }
            SpoRiskGrade.MODERATE -> {
                "【標準價差】折價幅度 ${String.format("%.1f", discountRatio)}%，需關注現股支撐" to
                "謹慎申購。折價在 10%~20% 之間，若個股近期處於高檔震盪或即將遭遇季線反壓，撥券日利潤容易被波動侵蝕。"
            }
            SpoRiskGrade.HIGH_DANGER -> {
                "【高度危險】折價僅 ${String.format("%.1f", discountRatio)}% 或稀釋壓力過大" to
                "不建議參與！折價不足 10%，在撥券等待期內（約 7 天）只要現貨回檔 1~2 根黑 K 就可能跌破承銷價，極易面臨「抽中現套」的窘境。"
            }
        }

        return SpoDecisionResult(
            record = record,
            grade = grade,
            discountRatio = discountRatio,
            spreadAmountPerLot = record.spreadAmountPerLot * lots,
            isArbitrageViable = isArbitrage,
            dilutionWarning = dilutionWarning,
            exposureDays = record.estimatedExposureDays,
            summary = summary,
            actionRecommendation = actionRec,
            scenarios = scenarios,
            analysisPoints = points
        )
    }

    private fun calculateScenarios(
        record: SpoLotteryRecord,
        lots: Int,
        feeDiscount: Double
    ): List<SpoScenarioProjection> {
        val totalShares = lots * record.sharesPerLot * 1000
        val costBasis = record.subscriptionPrice * totalShares + LOTTERY_HANDLING_FEE + WINNING_FEE

        // 三種撥券日假設：
        // 1. 保守情境：現價回跌 8% (或回測承銷價附近)
        val conservativePrice = TickSizeAligner.alignDown(record.marketPrice * 0.92)
        // 2. 基準情境：維持目前現股市價
        val basePrice = TickSizeAligner.alignNearest(record.marketPrice)
        // 3. 樂觀情境：撥券前現股上漲 5%
        val optimisticPrice = TickSizeAligner.alignUp(record.marketPrice * 1.05)

        val calcProjection = { name: String, desc: String, exitPrice: Double ->
            val sellRevenue = exitPrice * totalShares
            val sellBrokerFee = (sellRevenue * BROKERAGE_FEE_RATE * feeDiscount).coerceAtLeast(20.0)
            val sellTax = sellRevenue * STOCK_TRANSACTION_TAX
            val netRevenue = sellRevenue - sellBrokerFee - sellTax
            val netProfit = netRevenue - costBasis
            val grossProfit = (exitPrice - record.subscriptionPrice) * totalShares
            val returnRatio = if (costBasis > 0) (netProfit / costBasis) * 100.0 else 0.0

            SpoScenarioProjection(
                name = name,
                assumptionDesc = desc,
                projectedExitPrice = exitPrice,
                grossProfitPerLot = grossProfit / lots,
                netProfitPerLot = netProfit / lots,
                netReturnRatio = returnRatio
            )
        }

        return listOf(
            calcProjection(
                "保守防守情境",
                "撥券日前現股遭遇大盤回檔下跌 8%",
                conservativePrice
            ),
            calcProjection(
                "基準平盤情境",
                "撥券當日現股維持目前市價（${record.marketPrice} 元）",
                basePrice
            ),
            calcProjection(
                "樂觀推升情境",
                "除權息前或營收利多推升現股上漲 5%",
                optimisticPrice
            )
        )
    }
}
