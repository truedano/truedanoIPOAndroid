package com.example.data.remote

import com.example.domain.model.SpoLotteryRecord

object TwseSubscriptionParser {

    /**
     * 解析 TWSE 公開申購抽籤日程表資料 (publicForm 18 欄標準格式)
     *
     * 欄位定義：
     * [0] 序號
     * [1] 抽籤日期
     * [2] 證券名稱
     * [3] 證券代號
     * [4] 發行市場 (如: 上市增資, 上櫃增資, 初上市, 初上櫃, 創新板增資 等)
     * [5] 申購開始日
     * [6] 申購結束日
     * [7] 承銷股數
     * [8] 實際承銷股數
     * [9] 承銷價(元)
     * [10] 實際承銷價(元)
     * [11] 撥券日期(上市、上櫃日期)
     * [12] 主辦券商
     * [13] 申購股數 (例如 1,000 或 2,000)
     * [14] 總承銷金額(元)
     * [15] 總合格件
     * [16] 中籤率(%)
     * [17] 取消公開申購抽籤
     */
    fun parseResponse(
        response: TwseSubscriptionResponse,
        priceMap: Map<String, Double> = emptyMap()
    ): List<SpoLotteryRecord> {
        val rows = response.data ?: return emptyList()
        val fields = response.fields ?: emptyList()

        // 智慧匹配欄位索引
        val lotteryDateIdx = findIndex(fields, listOf("抽籤日期", "抽籤日")) ?: 1
        val nameIdx = findIndex(fields, listOf("證券名稱", "股票名稱")) ?: 2
        val codeIdx = findIndex(fields, listOf("證券代號", "股票代號", "代號")) ?: 3
        val marketTypeIdx = findIndex(fields, listOf("發行市場", "市場別", "性質")) ?: 4
        val startDateIdx = findIndex(fields, listOf("申購開始日", "申購開始")) ?: 5
        val endDateIdx = findIndex(fields, listOf("申購結束日", "申購截止", "申購結束")) ?: 6
        val totalSharesIdx = findIndex(fields, listOf("實際承銷股數", "承銷股數")) ?: 8
        val subPriceIdx = findIndex(fields, listOf("實際承銷價", "實際承銷價格", "承銷價", "承銷價格")) ?: 10
        val listingDateIdx = findIndex(fields, listOf("撥券日期", "上市日期", "上櫃日期")) ?: 11
        val lotSharesIdx = findIndex(fields, listOf("申購股數")) ?: 13

        val records = mutableListOf<SpoLotteryRecord>()

        for (row in rows) {
            try {
                if (row.size <= maxOf(codeIdx, nameIdx, subPriceIdx)) continue

                val rawCode = row.getOrNull(codeIdx)?.trim().orEmpty()
                val rawName = row.getOrNull(nameIdx)?.trim().orEmpty()
                val rawMarketType = row.getOrNull(marketTypeIdx)?.trim().orEmpty()

                if (rawCode.isBlank() || rawName.isBlank()) continue

                // 智慧過濾：只保留 4 碼普通股
                if (rawCode.length != 4 || !rawCode.all { it.isDigit() }) continue

                // 判斷是否為增資案（上市增資、上櫃增資、現增）
                val isSPO = rawMarketType.contains("增資") || rawMarketType.contains("現增")
                if (!isSPO) continue

                val rawLotteryDate = row.getOrNull(lotteryDateIdx)?.trim().orEmpty()
                val rawStartDate = row.getOrNull(startDateIdx)?.trim().orEmpty()
                val rawEndDate = row.getOrNull(endDateIdx)?.trim().orEmpty()
                val rawListingDate = row.getOrNull(listingDateIdx)?.trim().orEmpty()

                val subPrice = TwseParser.parseDoubleSafe(row.getOrNull(subPriceIdx))
                if (subPrice <= 0.0) continue

                val totalShares = TwseParser.parseLongSafe(row.getOrNull(totalSharesIdx))
                val sharesPerSub = TwseParser.parseLongSafe(row.getOrNull(lotSharesIdx)).coerceAtLeast(1000L)
                val sharesPerLot = (sharesPerSub / 1000L).toInt().coerceAtLeast(1)
                val totalLots = if (sharesPerSub > 0) totalShares / sharesPerSub else totalShares / 1000L

                // 取得現股市價（若即時報價查無，預設為承銷價 * 1.15 做為基準預估，並標記需使用者自訂確認）
                val marketPrice = priceMap[rawCode] ?: (subPrice * 1.15)

                val note = buildString {
                    append("【臺灣證交所官方現增申購案】市場類別：$rawMarketType")
                    if (priceMap.containsKey(rawCode)) {
                        append("，已同步當前最新收盤市價 $marketPrice 元")
                    } else {
                        append("，現股市價為預估基準，可於下方手動調整最新市價")
                    }
                }

                records.add(
                    SpoLotteryRecord(
                        stockCode = rawCode,
                        stockName = rawName,
                        subscriptionPrice = subPrice,
                        marketPrice = marketPrice,
                        startDate = TwseParser.normalizeTwseDate(rawStartDate),
                        endDate = TwseParser.normalizeTwseDate(rawEndDate),
                        lotteryDate = TwseParser.normalizeTwseDate(rawLotteryDate),
                        listingDate = TwseParser.normalizeTwseDate(rawListingDate),
                        totalLots = totalLots,
                        sharesPerLot = sharesPerLot,
                        shortSaleAvailable = false, // 官方申購表未附融券額度，預設保守設為 false，可手動查驗
                        shortSaleQuota = null,
                        dilutionRate = null,
                        note = note,
                        isPreset = false
                    )
                )
            } catch (_: Exception) {
                // 忽略個別行解析錯誤
            }
        }

        return records
    }

    private fun findIndex(fields: List<String>, keywords: List<String>): Int? {
        if (fields.isEmpty()) return null
        for (i in fields.indices) {
            val clean = fields[i].replace(" ", "").replace("　", "")
            for (keyword in keywords) {
                if (clean.contains(keyword)) return i
            }
        }
        return null
    }
}
