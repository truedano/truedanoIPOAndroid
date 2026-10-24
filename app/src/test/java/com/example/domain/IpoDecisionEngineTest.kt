package com.example.domain

import com.example.domain.calculator.ExitProfitCalculator
import com.example.domain.calculator.IpoDecisionEngine
import com.example.domain.model.ExitScenarioType
import com.example.domain.model.IpoAuctionRecord
import com.example.domain.model.MarketHeatLevel
import com.example.domain.model.SafetyMarginStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IpoDecisionEngineTest {

    @Test
    fun testExitProfitCalculator_singleLot() {
        // 假設申購價 50 元，目標賣出價 65 元，1 張，無折讓
        // 本金 = 50 * 1000 + 70 = 50070 元
        val capital = ExitProfitCalculator.calculateInvestedCapital(50.0, 1)
        assertEquals(50070.0, capital, 0.01)

        val result = ExitProfitCalculator.calculateScenario(
            type = ExitScenarioType.BASE,
            subscriptionPrice = 50.0,
            targetPrice = 65.0,
            lots = 1,
            feeDiscount = 1.0
        )

        // 賣出總額 = 65,000 元
        assertEquals(65000.0, result.grossRevenue, 0.01)
        // 交易稅 0.3% = 65000 * 0.003 = 195 元
        assertEquals(195.0, result.securitiesTax, 0.01)
        // 手續費 0.1425% = 65000 * 0.001425 = 92.625 -> round 93 元
        assertEquals(93.0, result.brokerFee, 0.01)
        // 總扣除 = 195 + 93 = 288 元
        assertEquals(288.0, result.totalExitDeductions, 0.01)
        // 實拿收入 = 65000 - 288 = 64712 元
        assertEquals(64712.0, result.netProceeds, 0.01)
        // 淨利潤 = 64712 - 50070 = 14642 元
        assertEquals(14642.0, result.netProfit, 0.01)
        // ROI = 14642 / 50070 * 100% = 29.24%
        assertEquals(29.243, result.roiPercent, 0.01)
    }

    @Test
    fun testCase_7825_HeYa() {
        // 測試案例：7825 和亞智慧 (對齊真實基準數據)
        // 釋出總量: 1800 張, 合格投標量: 14325 張 (投標倍數 7.96x > 5.0 燙手)
        // 最低得標價: 70.60 元, 加權平均價: 78.50 元, 最高得標價: 90.00 元
        // 申購價: 48.88 元 (安全邊際 = 70.60 - 48.88 = +21.72 元 > 0 安全訊號)
        // 興櫃現價: 66.70 元 (< 最高得標價 90.00，無散戶泡沫)
        val record = IpoAuctionRecord(
            stockCode = "7825",
            stockName = "和亞智慧",
            auctionDate = "2024/09/10",
            totalAuctionShares = 1800,
            validBidShares = 14325,
            minWinningPrice = 70.60,
            avgWeightedPrice = 78.50,
            maxWinningPrice = 90.00,
            subscriptionPrice = 48.88,
            emergingMarketPrice = 66.70,
            isPreset = true
        )

        val result = IpoDecisionEngine.evaluate(record, lots = 1)

        // 1. 投標倍數為 7.96x，評級為 HOT
        assertEquals(7.958, record.bidRatio, 0.01)
        assertEquals(MarketHeatLevel.HOT, result.heatLevel)

        // 2. 安全邊際 = +21.72 元，安全訊號
        assertEquals(21.72, record.safetyMargin, 0.01)
        assertEquals(SafetyMarginStatus.SAFE, result.safetyMarginStatus)

        // 3. 黃金獲利公式符合
        assertTrue(result.goldenFormula.isGolden)

        // 4. 價格廊道對齊檢驗
        // 最低得標價 70.60 (在 50~100 級距，tick=0.10) -> alignedDown: 70.60
        assertEquals(70.60, result.corridor.alignedSupportPrice, 0.01)
        // 加權平均價 78.50 -> alignedNearest: 78.50
        assertEquals(78.50, result.corridor.alignedPivotPrice, 0.01)
        // 最高得標價 90.00 -> alignedUp: 90.00
        assertEquals(90.00, result.corridor.alignedResistancePrice, 0.01)

        // 興櫃泡沫檢驗 (66.70 <= 90.00，非泡沫)
        assertFalse(result.corridor.isEmergingBubble)

        // 5. 三段出場情境試算檢查
        assertEquals(3, result.exitScenarios.size)
        val conservative = result.exitScenarios[0]
        val base = result.exitScenarios[1]
        val optimistic = result.exitScenarios[2]

        assertTrue(conservative.netProfit > 0)
        assertTrue(base.netProfit > conservative.netProfit)
        assertTrue(optimistic.netProfit > base.netProfit)
    }

    @Test
    fun testCase_7856_HanCe_InvertedDanger() {
        // 測試案例：7856 漢測 (危險倒掛案例驗證)
        // 釋出總量: 3000 張, 合格投標量: 5400 張 (投標倍數 1.8 < 2.5 偏冷)
        // 最低得標價: 95.00 元, 加權平均價: 98.00 元, 最高得標價: 105.00 元
        // 公開申購承銷價: 100.00 元 (申購價 100 > 最低得標價 95，安全邊際 = -5.0 元 < 0 倒掛！)
        // 興櫃現價: 115.00 元 (> 最高得標價 105，散戶泡沫警訊！)
        val record = IpoAuctionRecord(
            stockCode = "7856",
            stockName = "漢測科技",
            auctionDate = "2024/11/05",
            totalAuctionShares = 3000,
            validBidShares = 5400,
            minWinningPrice = 95.00,
            avgWeightedPrice = 98.00,
            maxWinningPrice = 105.00,
            subscriptionPrice = 100.00,
            emergingMarketPrice = 115.00,
            isPreset = true
        )

        val result = IpoDecisionEngine.evaluate(record, lots = 1)

        // 1. 投標倍數 1.8，偏冷 COLD
        assertEquals(1.8, record.bidRatio, 0.001)
        assertEquals(MarketHeatLevel.COLD, result.heatLevel)

        // 2. 安全邊際 = -5.0 元，極度危險倒掛
        assertEquals(-5.0, record.safetyMargin, 0.001)
        assertEquals(SafetyMarginStatus.INVERTED_DANGER, result.safetyMarginStatus)

        // 3. 不符合黃金獲利公式
        assertFalse(result.goldenFormula.isGolden)

        // 4. 興櫃泡沫檢驗 (115.00 > 105.00，為散戶泡沫溢價)
        assertTrue(result.corridor.isEmergingBubble)

        // 5. 保守出場情境下（以機構底線 95 元賣出），抽籤戶 (成本 100 元) 產生實質虧損
        val conservative = result.exitScenarios[0]
        assertTrue(conservative.netProfit < 0)
    }
}
