package com.example.data.local

import com.example.domain.model.IpoAuctionRecord

object PresetCases {
    val ALL_PRESETS = listOf(
        // 6898 程曦資訊 (目前預設展示檔位 - 常態穩健行情 SAFE)
        IpoAuctionRecord(
            stockCode = "6898",
            stockName = "程曦資訊",
            auctionDate = "2026/10/14",
            totalAuctionShares = 2632,
            validBidShares = 9770, // 3.71x 中性常態
            minWinningPrice = 63.00,
            avgWeightedPrice = 68.23,
            maxWinningPrice = 82.00,
            subscriptionPrice = 60.00, // 安全邊際 +3.0 元
            emergingMarketPrice = 73.00, // 興櫃未脫鉤
            note = "【常態穩健行情 SAFE】具備基礎安全邊際，投標熱度處於常態健康水準，走勢預計於法人得標區間內穩定運行。",
            isPreset = true,
            listingDate = "2026/10/23",
            analysisDate = "2026-10-24"
        ),
        // 7825 和亞智慧 (黃金公式 GOLDEN HONEYMOON)
        IpoAuctionRecord(
            stockCode = "7825",
            stockName = "和亞智慧",
            auctionDate = "2026/10/10",
            totalAuctionShares = 1800,
            validBidShares = 14325, // 7.96x 燙手
            minWinningPrice = 70.60,
            avgWeightedPrice = 78.50,
            maxWinningPrice = 90.00,
            subscriptionPrice = 48.88, // 安全邊際 +21.72 元
            emergingMarketPrice = 66.70, // 興櫃未脫鉤
            note = "【達成黃金獲利公式】法人超額競拍達 7.96 倍，最低得標價高於抽籤價 21.72 元，安全且豐厚之蜜月行情！",
            isPreset = true,
            listingDate = "2026/10/20",
            analysisDate = "2026-10-24"
        ),
        // 6825 和暢科技 (高危倒掛 CRITICAL DANGER)
        IpoAuctionRecord(
            stockCode = "6825",
            stockName = "和暢科技",
            auctionDate = "2026/10/19",
            totalAuctionShares = 3568,
            validBidShares = 12507, // 3.51x
            minWinningPrice = 20.32,
            avgWeightedPrice = 22.81,
            maxWinningPrice = 31.80,
            subscriptionPrice = 22.81, // 安全邊際 -2.49 元 倒掛
            emergingMarketPrice = 26.50,
            note = "【高危倒掛警訊 CRITICAL DANGER】公開申購價高於或等於最低得標價，機構進場成本比抽籤散戶更便宜，留意上市首日倒貨套利破發風險！",
            isPreset = true,
            listingDate = "2026/10/30",
            analysisDate = "2026-10-24"
        ),
        // 7710 東擎科技 (高危倒掛 CRITICAL DANGER)
        IpoAuctionRecord(
            stockCode = "7710",
            stockName = "東擎科技",
            auctionDate = "2026/10/15",
            totalAuctionShares = 4402,
            validBidShares = 7967, // 1.81x
            minWinningPrice = 129.50,
            avgWeightedPrice = 154.20,
            maxWinningPrice = 183.00,
            subscriptionPrice = 137.90, // 安全邊際 -8.4 元 倒掛
            emergingMarketPrice = 165.00,
            note = "【高危倒掛警訊】公開申購價 137.9 元高於最低得標價 129.5 元，法人成本大幅低於抽籤散戶。",
            isPreset = true,
            listingDate = "2026/10/27",
            analysisDate = "2026-10-24"
        ),
        // 7867 特力屋 (高危倒掛 CRITICAL DANGER)
        IpoAuctionRecord(
            stockCode = "7867",
            stockName = "特力屋",
            auctionDate = "2026/10/19",
            totalAuctionShares = 7344,
            validBidShares = 17824, // 2.43x
            minWinningPrice = 38.60,
            avgWeightedPrice = 44.06,
            maxWinningPrice = 60.00,
            subscriptionPrice = 44.06, // 安全邊際 -5.46 元 倒掛
            emergingMarketPrice = 48.00,
            note = "【高危倒掛警訊】最低得標價 38.6 元低於申購承銷價 44.06 元，安全邊際嚴重倒掛。",
            isPreset = true,
            listingDate = "2026/10/30",
            analysisDate = "2026-10-24"
        )
    )
}
