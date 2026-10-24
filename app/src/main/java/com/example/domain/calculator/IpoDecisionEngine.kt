package com.example.domain.calculator

import com.example.domain.model.ExitScenarioType
import com.example.domain.model.GoldenFormulaEvaluation
import com.example.domain.model.IpoAuctionRecord
import com.example.domain.model.IpoDecisionResult
import com.example.domain.model.MarketHeatLevel
import com.example.domain.model.PriceCorridor
import com.example.domain.model.SafetyMarginStatus
import com.example.domain.model.TickSizeAligner

/**
 * 台股 IPO 競拍蜜月行情預判決策引擎核心
 */
object IpoDecisionEngine {

    /**
     * 執行完整定錨推演與損益決策
     */
    fun evaluate(
        record: IpoAuctionRecord,
        lots: Int = 1,
        feeDiscount: Double = 1.0
    ): IpoDecisionResult {
        // Step 1: 檢視市場熱度
        val heatLevel = MarketHeatLevel.fromRatio(record.bidRatio)

        // Step 2: 比對安全邊際
        val safetyMarginStatus = SafetyMarginStatus.fromMargin(record.safetyMargin)

        // Step 3: 定錨真實期待值廊道 (強制對齊 6 檔 Tick Size)
        val rawSupport = record.minWinningPrice
        val alignedSupport = TickSizeAligner.alignDown(rawSupport)

        val rawPivot = record.avgWeightedPrice
        val alignedPivot = TickSizeAligner.alignNearest(rawPivot)

        val rawResistance = record.maxWinningPrice ?: (record.avgWeightedPrice * 1.25)
        val alignedResistance = TickSizeAligner.alignUp(rawResistance)

        // 興櫃泡沫檢驗：若興櫃價格 > 最高得標價，標記為散戶泡沫溢價警訊
        val isEmergingBubble = if (record.emergingMarketPrice != null) {
            record.emergingMarketPrice > rawResistance
        } else false

        val bubblePremiumRatio = if (record.emergingMarketPrice != null && rawResistance > 0.0) {
            ((record.emergingMarketPrice - rawResistance) / rawResistance) * 100.0
        } else null

        val corridor = PriceCorridor(
            rawSupportPrice = rawSupport,
            alignedSupportPrice = alignedSupport,
            rawPivotPrice = rawPivot,
            alignedPivotPrice = alignedPivot,
            rawResistancePrice = rawResistance,
            alignedResistancePrice = alignedResistance,
            emergingPrice = record.emergingMarketPrice,
            isEmergingBubble = isEmergingBubble,
            bubblePremiumRatio = bubblePremiumRatio
        )

        // 黃金獲利公式判定：
        // 條件：合格投標倍數 >= 5.0 且 公開申購價 < 最低得標價（安全邊際 > 0）
        val isRatioGolden = record.bidRatio >= 5.0
        val isMarginGolden = record.safetyMargin > 0.0
        val isGolden = isRatioGolden && isMarginGolden

        val score = calculateScore(record.bidRatio, record.safetyMargin, isEmergingBubble)
        val (summary, rationale) = generateRationale(
            isGolden,
            isRatioGolden,
            isMarginGolden,
            record.bidRatio,
            record.safetyMargin,
            isEmergingBubble
        )

        val goldenFormula = GoldenFormulaEvaluation(
            isGolden = isGolden,
            score = score,
            summary = summary,
            detailRationale = rationale
        )

        // 三段出場情境試算（保守/最低得標價、基準/加權均價、樂觀/最高得標價）
        val conservative = ExitProfitCalculator.calculateScenario(
            type = ExitScenarioType.CONSERVATIVE,
            subscriptionPrice = record.subscriptionPrice,
            targetPrice = alignedSupport,
            lots = lots,
            feeDiscount = feeDiscount
        )

        val base = ExitProfitCalculator.calculateScenario(
            type = ExitScenarioType.BASE,
            subscriptionPrice = record.subscriptionPrice,
            targetPrice = alignedPivot,
            lots = lots,
            feeDiscount = feeDiscount
        )

        val optimistic = ExitProfitCalculator.calculateScenario(
            type = ExitScenarioType.OPTIMISTIC,
            subscriptionPrice = record.subscriptionPrice,
            targetPrice = alignedResistance,
            lots = lots,
            feeDiscount = feeDiscount
        )

        val singleLotCost = ExitProfitCalculator.calculateInvestedCapital(record.subscriptionPrice, 1)

        return IpoDecisionResult(
            record = record,
            heatLevel = heatLevel,
            safetyMarginStatus = safetyMarginStatus,
            corridor = corridor,
            goldenFormula = goldenFormula,
            exitScenarios = listOf(conservative, base, optimistic),
            singleLotCost = singleLotCost
        )
    }

    private fun calculateScore(
        bidRatio: Double,
        safetyMargin: Double,
        isEmergingBubble: Boolean
    ): Int {
        var baseScore = 50

        // 投標倍數加減分 (上限 30 分)
        baseScore += when {
            bidRatio >= 8.0 -> 30
            bidRatio >= 5.0 -> 25
            bidRatio >= 3.5 -> 15
            bidRatio >= 2.5 -> 5
            bidRatio >= 1.5 -> -15
            else -> -30
        }

        // 安全邊際加減分 (上限 25 分)
        baseScore += when {
            safetyMargin > 20.0 -> 25
            safetyMargin > 10.0 -> 20
            safetyMargin > 0.0 -> 10
            safetyMargin == 0.0 -> -15
            else -> -35
        }

        // 興櫃泡沫風險扣分
        if (isEmergingBubble) {
            baseScore -= 10
        }

        return baseScore.coerceIn(0, 100)
    }

    private fun generateRationale(
        isGolden: Boolean,
        isRatioGolden: Boolean,
        isMarginGolden: Boolean,
        ratio: Double,
        margin: Double,
        isEmergingBubble: Boolean
    ): Pair<String, String> {
        val summary: String
        val rationale = StringBuilder()

        if (isGolden) {
            summary = "符合黃金獲利公式！法人籌碼護航，蜜月噴發機率極高"
            rationale.append("【強烈正面訊號】合格投標倍數達 ${String.format("%.2f", ratio)} 倍（>= 5.0 倍），展現法人資金強烈追捧。")
            rationale.append(" 同時安全邊際達 +${String.format("%.2f", margin)} 元，機構最低進場成本顯著高於公開申購價，機構具強烈護盤與拉抬動機。")
        } else {
            summary = when {
                !isMarginGolden -> "警告：安全邊際嚴重倒掛！極度高風險"
                !isRatioGolden -> "市場投標動能未達標（倍數 < 5.0），動能一般"
                else -> "未完全符合黃金獲利準則，請嚴控部位"
            }

            if (!isMarginGolden) {
                rationale.append("【高風險倒掛警訊】公開申購價 >= 機構最低得標價（安全邊際 ${String.format("%.2f", margin)} 元）。這代表機構法人以比散戶抽籤更便宜的成本取得籌碼，上市首日有強烈倒貨套利誘因！")
            } else if (!isRatioGolden) {
                rationale.append("【動能考量】合格投標倍數僅 ${String.format("%.2f", ratio)} 倍，未達到 5.0 倍超額追捧門檻，代表市場追價熱度屬常態或偏冷，首日開高後可能缺乏連續買盤支撐。")
            }
        }

        if (isEmergingBubble) {
            rationale.append("\n【興櫃泡沫警訊】開標當前之興櫃價格已超越機構最高得標價，顯示散戶在興櫃市場存在不理性的追高定錨溢價，轉上市後興櫃定錨效應將迅速破滅回歸機構成本廊道！")
        }

        return Pair(summary, rationale.toString())
    }
}
