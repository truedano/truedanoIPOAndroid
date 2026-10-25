package com.example.data.remote

import com.example.domain.model.SpoLotteryRecord
import java.time.LocalDate

object TwseSubscriptionParser {

    /**
     * 解析 TWSE 公開申購抽籤日程表資料 (publicForm 18 欄標準格式)
     *
     * @param filterExpiredLotteries 若為 true，則只保留「抽籤日尚未過期（抽籤日 >= 今天）」的案件
     */
    fun parseResponse(
        response: TwseSubscriptionResponse,
        priceMap: Map<String, Double> = emptyMap(),
        filterExpiredLotteries: Boolean = true,
        today: LocalDate = LocalDate.now()
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

                val formattedLotteryDate = TwseParser.normalizeTwseDate(rawLotteryDate)

                // 抽籤日過期過濾：若抽籤日早於今天，直接排除
                if (filterExpiredLotteries) {
                    val parsedLottery = parseDate(formattedLotteryDate)
                    if (parsedLottery != null && today.isAfter(parsedLottery)) {
                        continue
                    }
                }

                val subPrice = TwseParser.parseDoubleSafe(row.getOrNull(subPriceIdx))
                if (subPrice <= 0.0) continue

                val totalShares = TwseParser.parseLongSafe(row.getOrNull(totalSharesIdx))
                val sharesPerSub = TwseParser.parseLongSafe(row.getOrNull(lotSharesIdx)).coerceAtLeast(1000L)
                val sharesPerLot = (sharesPerSub / 1000L).toInt().coerceAtLeast(1)
                val totalLots = if (sharesPerSub > 0) totalShares / sharesPerSub else totalShares / 1000L

                // 取得現股市價（若查無則以承銷價 * 1.15 為基準預估）
                val marketPrice = priceMap[rawCode] ?: (subPrice * 1.15)

                val note = buildString {
                    append("【臺灣證交所官方現增申購案】市場類別：$rawMarketType")
                    if (priceMap.containsKey(rawCode)) {
                        append("，已同步最新現股市價 $marketPrice 元")
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
                        lotteryDate = formattedLotteryDate,
                        listingDate = TwseParser.normalizeTwseDate(rawListingDate),
                        totalLots = totalLots,
                        sharesPerLot = sharesPerLot,
                        shortSaleAvailable = false,
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

    private fun parseDate(formattedDate: String): LocalDate? {
        return try {
            val parts = formattedDate.replace("-", "/").split("/")
            if (parts.size == 3) {
                LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } else null
        } catch (_: Exception) {
            null
        }
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
