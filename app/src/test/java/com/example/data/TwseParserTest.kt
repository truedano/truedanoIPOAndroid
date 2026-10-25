package com.example.data

import com.example.data.remote.TwseAuctionResponse
import com.example.data.remote.TwseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TwseParserTest {

    @Test
    fun testParseTwseResponseRealFormat_6825_HeChang() {
        val fields = listOf(
            "序號", "開標日期", "證券名稱", "證券代號", "發行市場", "發行性質", "競拍方式",
            "投標開始日", "投標結束日", "競拍數量(張)", "最低投標價格(元)", "最低每標單投標數量(張)",
            "最高投(得)標數量(張)", "保證金成數(%)", "每一投標單投標處理費(元)", "撥券日期(上市、上櫃日期)",
            "主辦券商", "得標總金額(元)", "得標手續費率(%)", "總合格件", "合格投標數量(張)",
            "最低得標價格(元)", "最高得標價格(元)", "得標加權平均價格(元)", "實際承銷價格(元)", "取消競價拍賣(流標或取消)"
        )

        // 台灣證交所 6825 和暢科技 真實開標數據列
        val dataRow = listOf(
            "4", "2026/10/19", "和暢科技", "6825", "櫃檯買賣", "初上櫃", "美國標",
            "2026/10/13", "2026/10/15", "3,568", "20", "1", "461", "50", "400", "2026/10/30",
            "宏遠", "81,382,310", "5", "359", "12,507", "20.32", "31.8", "22.81", "22.8100", ""
        )

        val response = TwseAuctionResponse(
            stat = "OK",
            fields = fields,
            data = listOf(dataRow)
        )

        val lotteryDateMap = mapOf("6825" to "2026/10/22")
        val records = TwseParser.parseResponse(response, lotteryDateMap = lotteryDateMap)
        assertEquals(1, records.size)

        val record = records[0]
        assertEquals("6825", record.stockCode)
        assertEquals("和暢科技", record.stockName)
        assertEquals("2026/10/19", record.auctionDate)
        assertEquals("2026/10/30", record.listingDate)
        assertEquals("2026/10/22", record.lotteryDate)

        // 競拍釋出張數必須為 3568 張
        assertEquals(3568L, record.totalAuctionShares)
        // 合格投標張數必須為 12507 張
        assertEquals(12507L, record.validBidShares)

        // 最低得標價 20.32 元
        assertEquals(20.32, record.minWinningPrice, 0.001)
        // 最高得標價 31.8 元
        assertEquals(31.8, record.maxWinningPrice ?: 0.0, 0.001)
        // 加權平均價 22.81 元
        assertEquals(22.81, record.avgWeightedPrice, 0.001)
        // 實際承銷價 22.81 元
        assertEquals(22.81, record.subscriptionPrice, 0.001)

        // 檢驗 timingBadge
        assertEquals("2026/10/22 抽籤", record.timingBadge)
    }
}
