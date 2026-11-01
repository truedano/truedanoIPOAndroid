package com.example.data.remote

import com.example.domain.model.IpoAuctionRecord

object TwseParser {

    /**
     * 解析 TWSE 競價拍賣日程表與開標公告資料
     *
     * @param lotteryDateMap 來自 publicForm 的股票抽籤日對照表 (股票代號 -> 抽籤日)
     * @param subPriceMap 來自 publicForm 的股票公開承銷價對照表 (股票代號 -> 承銷價)
     */
    fun parseResponse(
        response: TwseAuctionResponse,
        lotteryDateMap: Map<String, String> = emptyMap(),
        subPriceMap: Map<String, Double> = emptyMap()
    ): List<IpoAuctionRecord> {
        val rows = response.data ?: return emptyList()
        val fields = response.fields ?: emptyList()

        // 精準尋找欄位索引，嚴防誤取其他欄位
        val dateIdx = findExactOrContains(fields, listOf("開標日期", "日期")) ?: 1
        val nameIdx = findExactOrContains(fields, listOf("證券名稱", "股票名稱")) ?: 2
        val codeIdx = findExactOrContains(fields, listOf("證券代號", "股票代號", "代號")) ?: 3

        val listingDateIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("撥券日期") || clean.contains("上市日期") || clean.contains("上櫃日期")
        }.takeIf { it >= 0 } ?: 15

        val minBidPriceIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("最低投標價格") || clean.contains("最低投標價") || clean.contains("底標價")
        }.takeIf { it >= 0 } ?: 10

        val biddingStartDateIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("投標開始日") || clean.contains("投標開始")
        }.takeIf { it >= 0 } ?: 7

        val biddingEndDateIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("投標結束日") || clean.contains("投標結束") || clean.contains("投標截止")
        }.takeIf { it >= 0 } ?: 8

        // 競拍釋出張數
        val totalSharesIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            (clean.contains("競拍數量") || clean.contains("競價拍賣數量")) && !clean.contains("取消") && !clean.contains("每標單") && !clean.contains("最高")
        }.takeIf { it >= 0 } ?: 9

        // 合格投標數量：
        val validBidSharesIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("合格投標數量") || (clean.contains("合格") && clean.contains("數量"))
        }.takeIf { it >= 0 } ?: 20

        // 最低得標價格 (元)：
        val minPriceIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("最低得標價格") || clean.contains("最低得標價")
        }.takeIf { it >= 0 } ?: 21

        // 最高得標價格 (元)：
        val maxPriceIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("最高得標價格") || clean.contains("最高得標價")
        }.takeIf { it >= 0 } ?: 22

        // 得標加權平均價格 (元)：
        val avgPriceIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("加權平均價格") || clean.contains("加權平均價") || clean.contains("得標加權")
        }.takeIf { it >= 0 } ?: 23

        // 實際承銷價格 / 公開申購價格：
        val subPriceIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("實際承銷價格") || clean.contains("公開申購承銷價格") || clean.contains("承銷價格") || clean.contains("公開申購價格")
        }.takeIf { it >= 0 } ?: 24

        val records = mutableListOf<IpoAuctionRecord>()

        for (row in rows) {
            try {
                if (row.size <= maxOf(dateIdx, codeIdx, nameIdx)) continue

                val rawCode = row.getOrNull(codeIdx)?.trim().orEmpty()
                val rawName = row.getOrNull(nameIdx)?.trim().orEmpty()
                val rawDate = row.getOrNull(dateIdx)?.trim().orEmpty()

                if (rawCode.isBlank() || rawName.isBlank()) continue

                // 智慧過濾：只保留 4 碼普通股之 IPO，過濾可轉債（5 碼）、權證與其他衍生性金融商品
                if (rawCode.length != 4 || !rawCode.all { it.isDigit() }) continue

                val totalShares = parseLongSafe(row.getOrNull(totalSharesIdx))
                val validShares = parseLongSafe(row.getOrNull(validBidSharesIdx))
                val minPrice = parseDoubleSafe(row.getOrNull(minPriceIdx))
                val maxPrice = parseDoubleOrNull(row.getOrNull(maxPriceIdx))
                val avgPrice = parseDoubleSafe(row.getOrNull(avgPriceIdx))
                var subPrice = parseDoubleSafe(row.getOrNull(subPriceIdx))

                // 若競拍表中的承銷價為 0，嘗試由公開申購表補齊
                if (subPrice <= 0.0 && subPriceMap.containsKey(rawCode)) {
                    subPrice = subPriceMap[rawCode] ?: 0.0
                }

                val minBidPrice = parseDoubleOrNull(row.getOrNull(minBidPriceIdx))
                val rawBiddingStart = row.getOrNull(biddingStartDateIdx)?.trim().orEmpty()
                val rawBiddingEnd = row.getOrNull(biddingEndDateIdx)?.trim().orEmpty()
                val biddingStart = if (rawBiddingStart.isNotBlank()) normalizeTwseDate(rawBiddingStart) else null
                val biddingEnd = if (rawBiddingEnd.isNotBlank()) normalizeTwseDate(rawBiddingEnd) else null

                val isCompleted = minPrice > 0.0 && avgPrice > 0.0

                // 若尚未開標，且底標價與承銷價皆查無資料（可能為非普通股空白廢行），才略過
                if (!isCompleted && (minBidPrice == null || minBidPrice <= 0.0) && subPrice <= 0.0) continue

                val formattedDate = normalizeTwseDate(rawDate)

                val rawListingDate = row.getOrNull(listingDateIdx)?.trim().orEmpty()
                val formattedListingDate = if (rawListingDate.isNotBlank()) normalizeTwseDate(rawListingDate) else null
                val matchedLotteryDate = lotteryDateMap[rawCode]

                val noteText = if (!isCompleted) {
                    "【臺灣證交所競拍進行中】尚未開標（底標價：${minBidPrice ?: 0.0} 元，暫定承銷價：$subPrice 元，預計 $formattedDate 開標）"
                } else {
                    "臺灣證交所官方開標紀錄"
                }

                records.add(
                    IpoAuctionRecord(
                        stockCode = rawCode,
                        stockName = rawName,
                        auctionDate = formattedDate,
                        totalAuctionShares = if (totalShares > 0) totalShares else 1000L,
                        validBidShares = validShares,
                        minWinningPrice = minPrice,
                        avgWeightedPrice = if (avgPrice > 0.0) avgPrice else minPrice,
                        maxWinningPrice = maxPrice,
                        subscriptionPrice = subPrice,
                        emergingMarketPrice = null,
                        note = noteText,
                        isPreset = false,
                        listingDate = formattedListingDate,
                        lotteryDate = matchedLotteryDate,
                        minBidPrice = minBidPrice,
                        biddingStartDate = biddingStart,
                        biddingEndDate = biddingEnd
                    )
                )
            } catch (_: Exception) {
                // 忽略個別錯誤行
            }
        }

        return records
    }

    private fun findExactOrContains(fields: List<String>, keywords: List<String>): Int? {
        if (fields.isEmpty()) return null
        for (i in fields.indices) {
            val field = fields[i].replace(" ", "").replace("　", "")
            for (keyword in keywords) {
                if (field == keyword || field.contains(keyword)) {
                    return i
                }
            }
        }
        return null
    }

    fun parseDoubleSafe(value: String?): Double {
        if (value.isNullOrBlank()) return 0.0
        val clean = value.replace(",", "").replace(" ", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun parseDoubleOrNull(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        val clean = value.replace(",", "").replace(" ", "").trim()
        return clean.toDoubleOrNull()
    }

    fun parseLongSafe(value: String?): Long {
        if (value.isNullOrBlank()) return 0L
        val clean = value.replace(",", "").replace(" ", "").trim()
        return clean.toLongOrNull() ?: 0L
    }

    /**
     * 標準化民國年或西元年字串為 YYYY/MM/DD
     * 例如 "115/10/19" -> "2026/10/19", "20261019" -> "2026/10/19"
     */
    fun normalizeTwseDate(dateStr: String): String {
        val clean = dateStr.trim().replace("-", "/")
        val parts = clean.split("/")
        if (parts.size == 3) {
            val yearPart = parts[0].trim()
            val y = yearPart.toIntOrNull() ?: return clean
            val fullYear = if (y < 1911) y + 1911 else y
            val m = parts[1].trim().padStart(2, '0')
            val d = parts[2].trim().padStart(2, '0')
            return "$fullYear/$m/$d"
        }

        val digits = clean.filter { it.isDigit() }
        if (digits.length == 7) {
            val rocYear = digits.substring(0, 3).toIntOrNull() ?: return dateStr
            val fullYear = rocYear + 1911
            val m = digits.substring(3, 5)
            val d = digits.substring(5, 7)
            return "$fullYear/$m/$d"
        } else if (digits.length == 8) {
            val y = digits.substring(0, 4)
            val m = digits.substring(4, 6)
            val d = digits.substring(6, 8)
            return "$y/$m/$d"
        }

        return dateStr
    }
}
