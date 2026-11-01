package com.example.domain

import com.example.domain.calculator.IpoDecisionEngine
import com.example.domain.model.IpoAuctionRecord
import com.example.domain.model.IpoStatusType
import com.example.domain.model.MarketHeatLevel
import com.example.domain.model.SafetyMarginStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IpoPendingAuctionTest {

    @Test
    fun testPendingAuctionNotCompleted() {
        // 模擬 7848 騏億鑫：尚未開標，得標數據皆為 0
        val record = IpoAuctionRecord(
            stockCode = "7848",
            stockName = "騏億鑫",
            auctionDate = "2026/11/05",
            totalAuctionShares = 3060,
            validBidShares = 0,
            minWinningPrice = 0.0,
            avgWeightedPrice = 0.0,
            maxWinningPrice = null,
            subscriptionPrice = 252.0,
            minBidPrice = 210.0,
            biddingStartDate = "2026/10/30",
            biddingEndDate = "2026/11/03",
            lotteryDate = "2026/11/10",
            listingDate = "2026/11/16"
        )

        // 1. 檢驗開標狀態
        assertFalse("得標數據為0應判定為尚未開標完成", record.isAuctionCompleted)
        assertEquals(0.0, record.bidRatio, 0.001)
        assertEquals(0.0, record.safetyMargin, 0.001)
        assertEquals(IpoStatusType.PENDING_AUCTION, record.statusType)

        // 2. 檢驗進行中判定 (2026-11-01 處於投標與開標日前)
        val today = LocalDate.of(2026, 11, 1)
        assertTrue(record.isActive(today))

        // 3. 檢驗決策引擎運算
        val result = IpoDecisionEngine.evaluate(record)
        assertEquals(MarketHeatLevel.PENDING, result.heatLevel)
        assertEquals(SafetyMarginStatus.PENDING, result.safetyMarginStatus)
        // 支撐價應採用底標價 210.0 元
        assertEquals(210.0, result.corridor.alignedSupportPrice, 0.01)
        // 樞軸價應採用暫定承銷價 252.0 元
        assertEquals(252.0, result.corridor.alignedPivotPrice, 0.01)
        assertTrue(result.goldenFormula.summary.contains("尚未開標"))
    }
}
