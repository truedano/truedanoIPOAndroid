package com.example.domain

import com.example.domain.calculator.SpoDecisionEngine
import com.example.domain.model.SpoLotteryRecord
import com.example.domain.model.SpoRiskGrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpoDecisionEngineTest {

    @Test
    fun testArbitrageGoldenWhenShortSaleAvailableAndGoodDiscount() {
        // 金融股現增，折價 15%，可融券鎖利
        val record = SpoLotteryRecord(
            stockCode = "2884",
            stockName = "玉山金",
            subscriptionPrice = 20.0,
            marketPrice = 25.0, // 折價率 = 5 / 25 = 20%
            startDate = "2026-10-20",
            endDate = "2026-10-22",
            lotteryDate = "2026-10-24",
            listingDate = "2026-11-02",
            totalLots = 30000,
            shortSaleAvailable = true,
            shortSaleQuota = 5000L,
            dilutionRate = 5.2
        )

        val result = SpoDecisionEngine.evaluate(record)

        assertEquals(SpoRiskGrade.ARBITRAGE_GOLDEN, result.grade)
        assertTrue(result.isArbitrageViable)
        assertFalse(result.dilutionWarning)
        assertEquals(20.0, result.discountRatio, 0.01)
        assertEquals(5000.0, result.spreadAmountPerLot, 0.01)
    }

    @Test
    fun testSafeBufferWhenHighDiscountWithoutShortSale() {
        // 科技股高折價 28%，但不可融券
        val record = SpoLotteryRecord(
            stockCode = "2454",
            stockName = "聯發科子公司",
            subscriptionPrice = 72.0,
            marketPrice = 100.0, // 折價率 = 28%
            startDate = "2026-10-20",
            endDate = "2026-10-22",
            lotteryDate = "2026-10-24",
            listingDate = "2026-11-02",
            totalLots = 5000,
            shortSaleAvailable = false,
            dilutionRate = 4.0
        )

        val result = SpoDecisionEngine.evaluate(record)

        assertEquals(SpoRiskGrade.SAFE_BUFFER, result.grade)
        assertFalse(result.isArbitrageViable)
        assertEquals(28.0, result.discountRatio, 0.01)
        assertEquals(28000.0, result.spreadAmountPerLot, 0.01)
    }

    @Test
    fun testHighDangerWhenLowDiscount() {
        // 低折價 7.5%，無融券
        val record = SpoLotteryRecord(
            stockCode = "9999",
            stockName = "高危現增",
            subscriptionPrice = 37.0,
            marketPrice = 40.0, // 折價率 = 3 / 40 = 7.5%
            startDate = "2026-10-20",
            endDate = "2026-10-22",
            lotteryDate = "2026-10-24",
            listingDate = "2026-11-02",
            totalLots = 10000,
            shortSaleAvailable = false
        )

        val result = SpoDecisionEngine.evaluate(record)

        assertEquals(SpoRiskGrade.HIGH_DANGER, result.grade)
        assertEquals(7.5, result.discountRatio, 0.01)
    }

    @Test
    fun testDilutionWarningTriggered() {
        // 稀釋率達 18%
        val record = SpoLotteryRecord(
            stockCode = "1234",
            stockName = "大稀釋現增",
            subscriptionPrice = 20.0,
            marketPrice = 26.0,
            startDate = "2026-10-20",
            endDate = "2026-10-22",
            lotteryDate = "2026-10-24",
            listingDate = "2026-11-02",
            totalLots = 50000,
            dilutionRate = 18.5
        )

        val result = SpoDecisionEngine.evaluate(record)

        assertTrue(result.dilutionWarning)
        // 由於稀釋率過巨，降級不給 SAFE_BUFFER
        assertEquals(SpoRiskGrade.MODERATE, result.grade)
    }
}
