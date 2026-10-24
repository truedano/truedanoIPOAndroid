package com.example.domain

import com.example.domain.model.TickSizeAligner
import org.junit.Assert.assertEquals
import org.junit.Test

class TickSizeAlignerTest {

    @Test
    fun testTier1_under10() {
        // 級距 < 10 元：升降單位 0.01 元
        val price = 8.536
        assertEquals(0.01, TickSizeAligner.getTickSize(price), 0.0001)
        assertEquals(8.53, TickSizeAligner.alignDown(price), 0.0001)
        assertEquals(8.54, TickSizeAligner.alignUp(price), 0.0001)
        assertEquals(8.54, TickSizeAligner.alignNearest(price), 0.0001)
    }

    @Test
    fun testTier2_10to50() {
        // 級距 10 ~ 50 元：升降單位 0.05 元
        val price = 36.43
        assertEquals(0.05, TickSizeAligner.getTickSize(price), 0.0001)
        assertEquals(36.40, TickSizeAligner.alignDown(price), 0.0001)
        assertEquals(36.45, TickSizeAligner.alignUp(price), 0.0001)
        assertEquals(36.45, TickSizeAligner.alignNearest(price), 0.0001)
    }

    @Test
    fun testTier3_50to100() {
        // 級距 50 ~ 100 元：升降單位 0.10 元
        val price = 78.46
        assertEquals(0.10, TickSizeAligner.getTickSize(price), 0.0001)
        assertEquals(78.40, TickSizeAligner.alignDown(price), 0.0001)
        assertEquals(78.50, TickSizeAligner.alignUp(price), 0.0001)
        assertEquals(78.50, TickSizeAligner.alignNearest(price), 0.0001)
    }

    @Test
    fun testTier4_100to500() {
        // 級距 100 ~ 500 元：升降單位 0.50 元
        val price = 245.35
        assertEquals(0.50, TickSizeAligner.getTickSize(price), 0.0001)
        assertEquals(245.00, TickSizeAligner.alignDown(price), 0.0001)
        assertEquals(245.50, TickSizeAligner.alignUp(price), 0.0001)
        assertEquals(245.50, TickSizeAligner.alignNearest(price), 0.0001)
    }

    @Test
    fun testTier5_500to1000() {
        // 級距 500 ~ 1000 元：升降單位 1.00 元
        val price = 688.4
        assertEquals(1.00, TickSizeAligner.getTickSize(price), 0.0001)
        assertEquals(688.00, TickSizeAligner.alignDown(price), 0.0001)
        assertEquals(689.00, TickSizeAligner.alignUp(price), 0.0001)
        assertEquals(688.00, TickSizeAligner.alignNearest(price), 0.0001)
    }

    @Test
    fun testTier6_above1000() {
        // 級距 >= 1000 元：升降單位 5.00 元
        val price = 1243.6
        assertEquals(5.00, TickSizeAligner.getTickSize(price), 0.0001)
        assertEquals(1240.00, TickSizeAligner.alignDown(price), 0.0001)
        assertEquals(1245.00, TickSizeAligner.alignUp(price), 0.0001)
        assertEquals(1245.00, TickSizeAligner.alignNearest(price), 0.0001)
    }
}
