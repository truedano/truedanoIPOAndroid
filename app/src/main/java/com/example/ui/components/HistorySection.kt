package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.IpoAuctionRecord
import com.example.domain.model.IpoStatusType
import com.example.domain.model.TickSizeAligner
import com.example.ui.theme.LightCardBackground
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextLightHint
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

/**
 * 本地歷史記錄與回測驗證元件
 * 支援「🔥 進行中 / 未過期」與「全部記錄」智慧切換過濾，卡片點擊即可切換查看，避免按鈕寬度爆版。
 */
@Composable
fun HistorySection(
    records: List<IpoAuctionRecord>,
    activeRecord: IpoAuctionRecord,
    showActiveOnly: Boolean,
    onToggleFilter: (Boolean) -> Unit,
    onLoadRecord: (IpoAuctionRecord) -> Unit,
    onDeleteRecord: (IpoAuctionRecord) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. 頂部標題與「清空所有紀錄」按鈕
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📑", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                Text(
                    text = "競拍標案追蹤與紀錄",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    fontSize = 18.sp
                )
            }

            OutlinedButton(
                onClick = onClearAll,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryMuted),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LightCardBorder)
                ),
                modifier = Modifier.testTag("clear_all_history_button")
            ) {
                Text(text = "🗑️ 清空紀錄", fontSize = 12.sp)
            }
        }

        // 2. 智慧切換標籤：進行中（未過期） vs 全部
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterTabChip(
                label = "🔥 進行中 / 未過期",
                isSelected = showActiveOnly,
                onClick = { onToggleFilter(true) }
            )
            FilterTabChip(
                label = "📑 全部記錄",
                isSelected = !showActiveOnly,
                onClick = { onToggleFilter(false) }
            )
        }

        // 提示字
        Text(
            text = if (showActiveOnly) {
                "💡 點擊任一標案卡片，即可切換上方儀表板查看該檔分析；僅列出「尚未掛牌上市」之進行中標案。"
            } else {
                "💡 點擊任一標案卡片即可載入；包含過往已掛牌上市之歷史標案，供歷史回測參考。"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFD97706),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        // 3. 標案列表或空狀態
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightCardBackground)
                    .border(1.dp, LightCardBorder, RoundedCornerShape(12.dp))
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📭", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (showActiveOnly) "目前無進行中之標案，請點擊上方「全部記錄」或使用搜尋檢索" else "目前暫無任何歷史記錄",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            records.forEach { record ->
                val isActive = record.stockCode == activeRecord.stockCode
                HistoryRecordItemCard(
                    record = record,
                    isActive = isActive,
                    onLoad = { onLoadRecord(record) },
                    onDelete = { onDeleteRecord(record) }
                )
            }
        }
    }
}

@Composable
private fun FilterTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF2563EB) else LightCardBackground)
            .border(
                1.dp,
                if (isSelected) Color(0xFF2563EB) else LightCardBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextPrimaryDark
        )
    }
}

@Composable
private fun HistoryRecordItemCard(
    record: IpoAuctionRecord,
    isActive: Boolean,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val statusType = record.statusType
    val (statusLabel, statusColor, statusBg) = when (statusType) {
        IpoStatusType.GOLDEN -> Triple("🏆 黃金蜜月", StatusGreen, Color(0xFFECFDF5))
        IpoStatusType.SAFE -> Triple("🛡️ 常態穩健", Color(0xFF2563EB), Color(0xFFEFF6FF))
        IpoStatusType.CRITICAL_DANGER -> Triple("⚠️ 高危倒掛", StatusRed, Color(0xFFFEF2F2))
    }

    val minStr = TickSizeAligner.formatPrice(record.minWinningPrice)
    val maxStr = TickSizeAligner.formatPrice(record.maxWinningPrice ?: (record.avgWeightedPrice * 1.25))
    val marginStr = "${if (record.safetyMargin > 0) "+" else ""}${String.format("%.2f", record.safetyMargin)} 元"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad() }
            .testTag("history_item_${record.stockCode}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFF0F7FF) else LightCardBackground
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isActive) Color(0xFF2563EB) else LightCardBorder
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 第一層：股票名稱代號 + 狀態徽章 + 刪除按鈕
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = record.stockName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontSize = 16.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.stockCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2563EB))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "👉 檢視中",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 右側：狀態徽章與刪除按鈕
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = 11.sp
                        )
                    }

                    // 輕巧小垃圾桶刪除按鈕（絕不爆版）
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable { onDelete() }
                            .testTag("btn_delete_${record.stockCode}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🗑️", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 第二層：三項重要籌碼指標
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "合格倍數", fontSize = 11.sp, color = TextLightHint)
                    Text(
                        text = "${String.format("%.2f", record.bidRatio)}x",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(text = "安全邊際", fontSize = 11.sp, color = TextLightHint)
                    Text(
                        text = marginStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (record.safetyMargin > 0) StatusGreen else StatusRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text(text = "預期廊道", fontSize = 11.sp, color = TextLightHint)
                    Text(
                        text = "$minStr ~ $maxStr",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 第三層：日期資訊與切換操作提示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateInfo = if (!record.listingDate.isNullOrBlank()) {
                    "掛牌: ${record.listingDate}"
                } else {
                    "開標: ${record.auctionDate}"
                }

                Text(
                    text = "🗓️ $dateInfo",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLightHint,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )

                // 右側操作提示：點擊卡片即可切換
                if (isActive) {
                    Text(
                        text = "✓ 正載入中",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFF6FF))
                            .clickable { onLoad() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("btn_load_${record.stockCode}")
                    ) {
                        Text(
                            text = "載入分析 ›",
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
