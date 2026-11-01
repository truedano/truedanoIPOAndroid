package com.example.domain

import com.example.domain.model.SpoLotteryRecord
import com.example.domain.model.SpoStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SpoLotteryStageTest {

    @Test
    fun testSpoStageTransitions() {
        val record = SpoLotteryRecord(
            stockCode = "2614",
            stockName = "東森",
            subscriptionPrice = 12.8,
            marketPrice = 14.5,
            startDate = "2026-10-28",
            endDate = "2026-10-30",
            lotteryDate = "2026-11-03",
            listingDate = "2026-11-11",
            totalLots = 5313
        )

        // 1. 申購開始前 (2026-10-26) -> UPCOMING
        val beforeStart = LocalDate.of(2026, 10, 26)
        assertEquals(SpoStage.UPCOMING, record.getStage(beforeStart))
        assertTrue(record.isLotteryActive(beforeStart))
        assertEquals("⏳ 10/28 開放", record.getStatusBadge(beforeStart))

        // 2. 開放申購首日 (2026-10-28) -> OPEN
        val onStart = LocalDate.of(2026, 10, 28)
        assertEquals(SpoStage.OPEN, record.getStage(onStart))
        assertTrue(record.isLotteryActive(onStart))
        assertEquals("🔥 申購中(剩2天)", record.getStatusBadge(onStart))

        // 3. 申購截止日當天 (2026-10-30) -> OPEN (今日截止)
        val onEnd = LocalDate.of(2026, 10, 30)
        assertEquals(SpoStage.OPEN, record.getStage(onEnd))
        assertTrue(record.isLotteryActive(onEnd))
        assertEquals("🔥 今日截止", record.getStatusBadge(onEnd))

        // 4. 申購已截止，等待抽籤開獎中 (2026-10-31) -> BALLOTING
        val betweenEndAndLottery = LocalDate.of(2026, 10, 31)
        assertEquals(SpoStage.BALLOTING, record.getStage(betweenEndAndLottery))
        assertTrue(record.isLotteryActive(betweenEndAndLottery))
        assertEquals("⏳ 待開獎(11/3)", record.getStatusBadge(betweenEndAndLottery))

        // 5. 抽籤開獎當天 (2026-11-03) -> BALLOTING (今日抽籤)
        val onLottery = LocalDate.of(2026, 11, 3)
        assertEquals(SpoStage.BALLOTING, record.getStage(onLottery))
        assertTrue(record.isLotteryActive(onLottery))
        assertEquals("🎉 今日抽籤", record.getStatusBadge(onLottery))

        // 6. 抽籤日已過 (2026-11-04) -> CLOSED (已過期下架)
        val afterLottery = LocalDate.of(2026, 11, 4)
        assertEquals(SpoStage.CLOSED, record.getStage(afterLottery))
        assertFalse(record.isLotteryActive(afterLottery))
    }
}
