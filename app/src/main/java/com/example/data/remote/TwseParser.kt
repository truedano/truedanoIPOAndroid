package com.example.data.remote

import com.example.domain.model.IpoAuctionRecord

object TwseParser {

    /**
     * 解析 TWSE 競價拍賣日程表與開標公告資料
     *
     * 對應台灣證券交易所 API 欄位 (26 欄標準格式)：
     * [0] 序號
     * [1] 開標日期
     * [2] 證券名稱
     * [3] 證券代號
     * [4] 發行市場
     * [5] 發行性質
     * [6] 競拍方式
     * [7] 投標開始日
     * [8] 投標結束日
     * [9] 競拍數量(張) / 競拍數量
     * [10] 最低投標價格(元)
     * [11] 最低每標單投標數量(張)
     * [12] 最高投(得)標數量(張)
     * [13] 保證金成數(%)
     * [14] 每一投標單投標處理費(元)
     * [15] 撥券日期(上市、上櫃日期)
     * [16] 主辦券商
     * [17] 得標總金額(元)
     * [18] 得標手續費率(%)
     * [19] 總合格件
     * [20] 合格投標數量(張) / 合格投標數量
     * [21] 最低得標價格(元)
     * [22] 最高得標價格(元)
     * [23] 得標加權平均價格(元)
     * [24] 實際承銷價格(元) / 公開申購承銷價格
     * [25] 取消競價拍賣(流標或取消)
     */
    fun parseResponse(response: TwseAuctionResponse): List<IpoAuctionRecord> {
        val rows = response.data ?: return emptyList()
        val fields = response.fields ?: emptyList()

        // 精準尋找欄位索引，嚴防誤取其他欄位
        val dateIdx = findExactOrContains(fields, listOf("開標日期", "日期")) ?: 1
        val nameIdx = findExactOrContains(fields, listOf("證券名稱", "股票名稱")) ?: 2
        val codeIdx = findExactOrContains(fields, listOf("證券代號", "股票代號", "代號")) ?: 3

        // 競拍釋出張數：必須精準匹配「競拍數量」或「競價拍賣數量」，嚴禁匹配「取消競價拍賣」或「最高投(得)標數量」
        val totalSharesIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            (clean.contains("競拍數量") || clean.contains("競價拍賣數量")) && !clean.contains("取消") && !clean.contains("每標單") && !clean.contains("最高")
        }.takeIf { it >= 0 } ?: 9

        // 合格投標數量：
        val validBidSharesIdx = fields.indexOfFirst {
            val clean = it.replace(" ", "")
            clean.contains("合格投標數量") || (clean.contains("合格") && clean.contains("數量"))
        }.takeIf { it >= 0 } ?: 20

        // 最低得標價格 (元)：嚴禁匹配「最低投標價格」
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

                val totalShares = parseLongSafe(row.getOrNull(totalSharesIdx))
                val validShares = parseLongSafe(row.getOrNull(validBidSharesIdx))
                val minPrice = parseDoubleSafe(row.getOrNull(minPriceIdx))
                val maxPrice = parseDoubleOrNull(row.getOrNull(maxPriceIdx))
                val avgPrice = parseDoubleSafe(row.getOrNull(avgPriceIdx))
                val subPrice = parseDoubleSafe(row.getOrNull(subPriceIdx))

                // 若尚未開標（得標金額與價格均為 0），略過未開標項目
                if (minPrice <= 0.0 && avgPrice <= 0.0) continue

                val formattedDate = normalizeTwseDate(rawDate)

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
                        note = "臺灣證交所官方開標紀錄",
                        isPreset = false
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
        val num = clean.toDoubleOrNull() ?: return null
        return if (num > 0.0) num else null
    }

    fun parseLongSafe(value: String?): Long {
        if (value.isNullOrBlank()) return 0L
        val clean = value.replace(",", "").replace(" ", "").trim()
        val d = clean.toDoubleOrNull() ?: 0.0
        return d.toLong()
    }

    fun normalizeTwseDate(rawDate: String): String {
        val parts = rawDate.split("/", "-")
        if (parts.size == 3) {
            val rocYear = parts[0].toIntOrNull()
            if (rocYear != null && rocYear < 1900) {
                val adYear = rocYear + 1911
                return "$adYear/${parts[1].padStart(2, '0')}/${parts[2].padStart(2, '0')}"
            }
        }
        return rawDate
    }
}
