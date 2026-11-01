package com.example.data

import com.example.data.remote.TwseSubscriptionParser
import com.example.data.remote.TwseSubscriptionResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TwseSubscriptionParserTest {

    @Test
    fun testParseTwsePublicSubscriptionFormWithExpiryFilter() {
        val sampleFields = listOf(
            "序號", "抽籤日期", "證券名稱", "證券代號", "發行市場",
            "申購開始日", "申購結束日", "承銷股數", "實際承銷股數",
            "承銷價(元)", "實際承銷價(元)", "撥券日期(上市、上櫃日期)",
            "主辦券商", "申購股數", "總承銷金額(元)", "總合格件",
            "中籤率(%)", "取消公開申購抽籤"
        )

        val sampleData = listOf(
            // 進行中增資案件 1 (東森: 115/11/03 抽籤)
            listOf("6", "115/11/03", "東森", "2614", "上市增資", "115/10/28", "115/10/30", "10,626,000", "10,626,000", "12.8", "12.8", "115/11/11", "永豐金", "2,000", "136,012,800", "0", "0", ""),
            // 進行中增資案件 2 (普誠: 115/10/30 抽籤)
            listOf("7", "115/10/30", "普誠", "6129", "上櫃增資", "115/10/23", "115/10/28", "1,800,000", "1,800,000", "11.75", "11.75", "115/11/09", "福邦", "1,000", "21,150,000", "0", "0", ""),
            // 已過抽籤日之增資案件 (晶心科: 115/10/22 抽籤) - 應被自動排除！
            listOf("11", "115/10/22", "晶心科", "6533", "上市增資", "115/10/16", "115/10/20", "612,000", "612,000", "195", "195", "115/11/03", "富邦", "1,000", "119,340,000", "228,692", "0.26", ""),
            // 非增資案 (公債) - 應被排除
            listOf("1", "115/11/04", "115央債甲0", "A151JA", "中央登錄公債", "115/10/27", "115/11/02", "60,000", "60,000", "100", "100", "115/11/09", "中華郵政", "1,000", "6,000,000", "0", "0", ""),
            // 初上市 (IPO) - 在 SPO 模組中排除
            listOf("18", "115/10/19", "程曦資訊", "6898", "初上櫃", "115/10/13", "115/10/15", "657,000", "657,000", "60", "60", "115/10/23", "富邦", "1,000", "39,420,000", "108,403", "0.6", "")
        )

        val response = TwseSubscriptionResponse(
            stat = "OK",
            fields = sampleFields,
            data = sampleData
        )

        val priceMap = mapOf(
            "2614" to 14.5,
            "6129" to 13.2,
            "6533" to 220.0
        )

        // 假定今天日期為 2026/10/26 (民國 115 年 10 月 26 日)
        val today = LocalDate.of(2026, 10, 26)

        val records = TwseSubscriptionParser.parseResponse(
            response = response,
            priceMap = priceMap,
            filterExpiredLotteries = true,
            today = today
        )

        // 晶心科抽籤日 10/22 已過，應只有 東森(11/03) 與 普誠(10/30) 共 2 筆
        assertEquals(2, records.size)

        val dongSen = records.find { it.stockCode == "2614" }
        assertNotNull(dongSen)
        assertEquals("東森", dongSen!!.stockName)

        val puCheng = records.find { it.stockCode == "6129" }
        assertNotNull(puCheng)
        assertEquals("普誠", puCheng!!.stockName)

        val expiredJingXin = records.find { it.stockCode == "6533" }
        assertNull("已過抽籤日的晶心科應被嚴格過濾排除", expiredJingXin)
    }

    @Test
    fun testParseTwsePublicSubscriptionForm_FallbackToTentativePriceWhenUndecided() {
        val sampleFields = listOf(
            "序號", "抽籤日期", "證券名稱", "證券代號", "發行市場",
            "申購開始日", "申購結束日", "承銷股數", "實際承銷股數",
            "承銷價(元)", "實際承銷價(元)", "撥券日期(上市、上櫃日期)",
            "主辦券商", "申購股數", "總承銷金額(元)", "總合格件",
            "中籤率(%)", "取消公開申購抽籤"
        )

        // 模擬「實際承銷價(元)」為「未訂出」，但「承銷價(元)」為 252.0 的增資案件
        val sampleData = listOf(
            listOf("1", "115/11/10", "騏億鑫", "7848", "上市增資", "115/11/04", "115/11/06", "865,000", "865,000", "252", "未訂出", "115/11/16", "永豐金", "1,000", "未訂出", "0", "0", "")
        )

        val response = TwseSubscriptionResponse(
            stat = "OK",
            fields = sampleFields,
            data = sampleData
        )

        val today = LocalDate.of(2026, 11, 1)

        val records = TwseSubscriptionParser.parseResponse(
            response = response,
            filterExpiredLotteries = true,
            today = today
        )

        assertEquals(1, records.size)
        val record = records.first()
        assertEquals("7848", record.stockCode)
        // 應成功回退並抓取暫定承銷價 252.0 元
        assertEquals(252.0, record.subscriptionPrice, 0.001)
        org.junit.Assert.assertTrue(record.note.contains("暫定承銷價 252.0 元"))
    }
}
