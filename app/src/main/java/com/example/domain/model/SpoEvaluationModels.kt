package com.example.domain.model

/**
 * 現金增資抽籤風險評級
 */
enum class SpoRiskGrade(val label: String, val badgeTitle: String) {
    ARBITRAGE_GOLDEN(
        label = "ARBITRAGE 🏆 融券鎖利套利",
        badgeTitle = "無懼波動！可融券或借券賣出鎖定價差之純套利等級"
    ),
    SAFE_BUFFER(
        label = "SAFE 🛡️ 深厚安全墊",
        badgeTitle = "折價率 >= 20%，具備抗回檔防護緩衝"
    ),
    MODERATE(
        label = "MODERATE ⚠️ 標準價差",
        badgeTitle = "折價率 10% ~ 20%，需承擔申購至撥券期之現貨波動"
    ),
    HIGH_DANGER(
        label = "CRITICAL DANGER 🛑 破底現套高危",
        badgeTitle = "折價率 < 10% 或股本稀釋過巨，極易在撥券日前跌破承銷價"
    )
}

/**
 * 撥券日現股走勢之三段出場情境試算
 */
data class SpoScenarioProjection(
    val name: String,
    val assumptionDesc: String,
    val projectedExitPrice: Double,
    val grossProfitPerLot: Double,
    val netProfitPerLot: Double,
    val netReturnRatio: Double
)

/**
 * SPO 現金增資綜合決策結果
 */
data class SpoDecisionResult(
    val record: SpoLotteryRecord,
    val grade: SpoRiskGrade,
    val discountRatio: Double,
    val spreadAmountPerLot: Double,
    val isArbitrageViable: Boolean,
    val dilutionWarning: Boolean,
    val exposureDays: Int,
    val summary: String,
    val actionRecommendation: String,
    val scenarios: List<SpoScenarioProjection>,
    val analysisPoints: List<String>
)
