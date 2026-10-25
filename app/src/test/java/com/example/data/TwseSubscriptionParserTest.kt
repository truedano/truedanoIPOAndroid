package com.example.data

import com.example.data.remote.TwseSubscriptionParser
import com.example.data.remote.TwseSubscriptionResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TwseSubscriptionParserTest {

    @Test
    fun testParseTwsePublicSubscriptionForm() {
        val sampleFields = listOf(
            "序號", "抽籤日期", "證券名稱", "證券代號", "發行市場",
            "申購開始日", "申購結束日", "承銷股數", "實際承銷股數",
            "承銷價(元)", "實際承銷價(元)", "撥券日期(上市、上櫃日期)",
            "主辦券商", "申購股數", "總承銷金額(元)", "總合格件",
            "中籤率(%)", "取消公開申購抽籤"
        )

        val sampleData = listOf(
            // 增資案件 1 (東森)
            listOf("6", "115/11/03", "東森", "2614", "上市增資", "115/10/28", "115/10/30", "10,626,000", "10,626,000", "12.8", "12.8", "115/11/11", "永豐金", "2,000", "136,012,800", "0", "0", ""),
            // 增資案件 2 (普誠)
            listOf("7", "115/10/30", "普誠", "6129", "上櫃增資", "115/10/23", "115/10/28", "1,800,000", "1,800,000", "11.75", "11.75", "115/11/09", "福邦", "1,000", "21,150,000", "0", "0", ""),
            // 非增資案 (公債) - 應被過濾
            listOf("1", "115/11/04", "115央債甲0", "A151JA", "中央登錄公債", "115/10/27", "115/11/02", "60,000", "60,000", "100", "100", "115/11/09", "中華郵政", "1,000", "6,000,000", "0", "0", ""),
            // 初上市 (IPO) - 在 SPO 模組中過濾
            listOf("18", "115/10/19", "程曦資訊", "6898", "初上櫃", "115/10/13", "115/10/15", "657,000", "657,000", "60", "60", "115/10/23", "富邦", "1,000", "39,420,000", "108,403", "0.6", "")
        )

        val response = TwseSubscriptionResponse(
            stat = "OK",
            fields = sampleFields,
            data = sampleData
        )

        val priceMap = mapOf(
            "2614" to 14.5,
            "6129" to 13.2
        )

        val records = TwseSubscriptionParser.parseResponse(response, priceMap)

        assertEquals(2, records.size)

        val dongSen = records.find { it.stockCode == "2614" }
        assertNotNull(dongSen)
        assertEquals("東森", dongSen!!.stockName)
        assertEquals(12.8, dongSen.subscriptionPrice, 0.01)
        assertEquals(14.5, dongSen.marketPrice, 0.01)
        assertEquals(2, dongSen.sharesPerLot)
        assertEquals(5313L, dongSen.totalLots)
        assertTrue(dongSen.discountRatio > 0)

        val puCheng = records.find { it.stockCode == "6129" }
        assertNotNull(puCheng)
        assertEquals("普誠", puCheng!!.stockName)
        assertEquals(11.75, puCheng.subscriptionPrice, 0.01)
        assertEquals(13.2, puCheng.marketPrice, 0.01)
        assertEquals(1, puCheng.sharesPerLot)
    }
}
