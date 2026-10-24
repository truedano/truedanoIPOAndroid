package com.example.domain.model

/**
 * Step 1 市場熱度評級
 */
enum class MarketHeatLevel(
    val title: String,
    val description: String,
    val minThreshold: Double,
    val maxThreshold: Double
) {
    HOT(
        title = "燙手 (超額追捧)",
        description = "投標倍數超過 5.0 倍，市場人氣鼎盛，蜜月暴衝潛力高",
        minThreshold = 5.0,
        maxThreshold = Double.MAX_VALUE
    ),
    NEUTRAL(
        title = "中性常態",
        description = "投標倍數介於 2.5 ~ 5.0 倍，市場反映溫和，屬正常承銷行情",
        minThreshold = 2.5,
        maxThreshold = 5.0
    ),
    COLD(
        title = "偏冷 (動能不足)",
        description = "投標倍數低於 2.5 倍，市場認購意願低迷，首日缺乏追價推力",
        minThreshold = 0.0,
        maxThreshold = 2.5
    );

    companion object {
        fun fromRatio(ratio: Double): MarketHeatLevel {
            return when {
                ratio > 5.0 -> HOT
                ratio >= 2.5 -> NEUTRAL
                else -> COLD
            }
        }
    }
}

/**
 * Step 2 安全邊際狀態
 */
enum class SafetyMarginStatus(
    val title: String,
    val isSafe: Boolean,
    val description: String
) {
    SAFE(
        title = "安全訊號",
        isSafe = true,
        description = "機構最低得標成本高於散戶公開申購價，機構具拉抬護盤動機"
    ),
    INVERTED_DANGER(
        title = "極度危險倒掛",
        isSafe = false,
        description = "公開申購價高於或等於機構最低得標價，機構成本比抽籤散戶還便宜，上市首日倒貨套利風險極高"
    );

    companion object {
        fun fromMargin(margin: Double): SafetyMarginStatus {
            return if (margin > 0.0) SAFE else INVERTED_DANGER
        }
    }
}

/**
 * Step 3 定錨真實期待值廊道（全部對齊台股 6 檔升降單位）
 */
data class PriceCorridor(
    val rawSupportPrice: Double,
    val alignedSupportPrice: Double,

    val rawPivotPrice: Double,
    val alignedPivotPrice: Double,

    val rawResistancePrice: Double,
    val alignedResistancePrice: Double,

    val emergingPrice: Double?,
    val isEmergingBubble: Boolean,
    val bubblePremiumRatio: Double?
)

/**
 * 黃金獲利公式判定結果
 */
data class GoldenFormulaEvaluation(
    val isGolden: Boolean,
    val score: Int, // 0 ~ 100
    val summary: String,
    val detailRationale: String
)

/**
 * 出場情境枚舉
 */
enum class ExitScenarioType(val label: String, val subtitle: String) {
    CONSERVATIVE(label = "保守情境", subtitle = "以最低得標價出場（機構防守底線）"),
    BASE(label = "基準情境", subtitle = "以加權平均價出場（多空中樞籌碼共識）"),
    OPTIMISTIC(label = "樂觀情境", subtitle = "以最高得標價出場（蜜月噴出天花板）")
}

/**
 * 出場情境財務試算結果
 */
data class ExitScenarioResult(
    val type: ExitScenarioType,
    val targetPrice: Double,
    val alignedTargetPrice: Double,
    val lots: Int, // 張數
    val grossRevenue: Double, // 總營收 = 賣出價 * 1000 * 張數
    val securitiesTax: Double, // 證券交易稅 0.3%
    val brokerFee: Double, // 賣出手續費 0.1425% * 折讓
    val totalExitDeductions: Double, // 稅 + 費
    val netProceeds: Double, // 實拿收入
    val investedCapital: Double, // 總投入本金
    val netProfit: Double, // 淨獲利 = 實拿收入 - 投入本金
    val roiPercent: Double // 預估 ROI %
)

/**
 * 完整 IPO 決策引擎運算輸出
 */
data class IpoDecisionResult(
    val record: IpoAuctionRecord,
    val heatLevel: MarketHeatLevel,
    val safetyMarginStatus: SafetyMarginStatus,
    val corridor: PriceCorridor,
    val goldenFormula: GoldenFormulaEvaluation,
    val exitScenarios: List<ExitScenarioResult>,
    val singleLotCost: Double
)
