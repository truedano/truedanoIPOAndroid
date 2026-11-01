package com.example.domain

import com.example.domain.model.IpoAuctionRecord
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IpoAuctionActiveTest {

    @Test
    fun testIpoLotteryDecisionActiveRange() {
        val record = IpoAuctionRecord(
            stockCode = "7825",
            stockName = "和亞智慧",
            auctionDate = "2026-10-15",
            totalAuctionShares = 1800,
            validBidShares = 14325,
            minWinningPrice = 70.60,
            avgWeightedPrice = 78.50,
            maxWinningPrice = 90.00,
            subscriptionPrice = 48.88,
            lotteryDate = "2026-10-24",
            listingDate = "2026-11-05"
        )

        // 1. 開標後至抽籤前 (2026-10-20) -> 有效進行中
        val beforeLottery = LocalDate.of(2026, 10, 20)
        assertTrue(record.isActive(beforeLottery))

        // 2. 抽籤當天 (2026-10-24) -> 有效進行中
        val onLottery = LocalDate.of(2026, 10, 24)
        assertTrue(record.isActive(onLottery))

        // 3. 抽籤日次日 (2026-10-25) -> 抽籤決策已結束，應自動移除
        val afterLottery = LocalDate.of(2026, 10, 25)
        assertFalse(record.isActive(afterLottery))

        // 4. 掛牌日 (2026-11-05) -> 即使在掛牌日，抽籤決策早已結束，不應顯示
        val onListing = LocalDate.of(2026, 11, 5)
        assertFalse(record.isActive(onListing))
    }
}
