package com.example.data.local

import com.example.domain.model.SpoLotteryRecord

object PresetSpoCases {
    val ALL_PRESETS = listOf(
        // 2884 玉山金 (頂級套利 ARBITRAGE_GOLDEN - 金控融券鎖利)
        SpoLotteryRecord(
            stockCode = "2884",
            stockName = "玉山金",
            subscriptionPrice = 20.00,
            marketPrice = 25.80,
            startDate = "2026/10/18",
            endDate = "2026/10/22",
            lotteryDate = "2026/10/24",
            listingDate = "2026/11/03",
            totalLots = 38000,
            sharesPerLot = 2, // 一次可申購 2 張
            shortSaleAvailable = true,
            shortSaleQuota = 12000L,
            dilutionRate = 5.8,
            note = "【頂級套利案例】金控大規模現金增資，折價率達 22.5%，且市場融券額度充裕。中籤後可即時融券放空鎖定價差，於撥券日以現券償還，完成無波動風險套利！",
            isPreset = true,
            analysisDate = "2026-10-26"
        ),
        // 2646 星宇航空 (深厚安全墊 SAFE_BUFFER - 高折價人氣抽籤)
        SpoLotteryRecord(
            stockCode = "2646",
            stockName = "星宇航空",
            subscriptionPrice = 20.00,
            marketPrice = 28.50,
            startDate = "2026/10/15",
            endDate = "2026/10/17",
            lotteryDate = "2026/10/21",
            listingDate = "2026/10/29",
            totalLots = 79900,
            sharesPerLot = 2,
            shortSaleAvailable = false,
            dilutionRate = 8.5,
            note = "【深厚安全墊案例】折價幅度高達 29.8%，每股價差 8.5 元（每筆申購2張預估毛利 17,000 元）。即使無融券鎖利，充沛的安全墊亦能承受申購期現貨震盪。",
            isPreset = true,
            analysisDate = "2026-10-26"
        ),
        // 6770 力積電 (高危險破底 CRITICAL_DANGER - 負面警示教學案例)
        SpoLotteryRecord(
            stockCode = "6770",
            stockName = "力積電",
            subscriptionPrice = 25.00,
            marketPrice = 27.20,
            startDate = "2026/10/05",
            endDate = "2026/10/07",
            lotteryDate = "2026/10/09",
            listingDate = "2026/10/19",
            totalLots = 15000,
            sharesPerLot = 1,
            shortSaleAvailable = false,
            dilutionRate = 6.2,
            note = "【破底現套反面教材】折價幅度僅 8.1%（價差僅 2.2 元），且不可融券避險。撥券等待期內（約 7 交易日）現股隨半導體族群回跌 9%，撥券日直接跌破承銷價倒賠！",
            isPreset = true,
            analysisDate = "2026-10-26"
        ),
        // 6919 康霈* (股本大稀釋賣壓警戒 MODERATE)
        SpoLotteryRecord(
            stockCode = "6919",
            stockName = "康霈*",
            subscriptionPrice = 430.00,
            marketPrice = 520.00,
            startDate = "2026/10/12",
            endDate = "2026/10/14",
            lotteryDate = "2026/10/16",
            listingDate = "2026/10/26",
            totalLots = 1400,
            sharesPerLot = 1,
            shortSaleAvailable = false,
            dilutionRate = 17.2, // 稀釋率 > 15% 觸發賣壓警告
            note = "【高稀釋高單價案例】折價率 17.3%，單張價差達 90,000 元。但增資股數佔原流通股本達 17.2%，撥券當日將承受龐大獲利了結倒貨賣壓，建議撥券開盤即刻沖銷。",
            isPreset = true,
            analysisDate = "2026-10-26"
        )
    )
}
