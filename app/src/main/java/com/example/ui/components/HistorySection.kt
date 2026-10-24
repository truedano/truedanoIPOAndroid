package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.LightAppBackground
import com.example.ui.theme.LightCardBackground
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextLightHint
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

/**
 * 本地歷史記錄與回測驗證元件
 * 忠實復刻截圖 Image 3
 */
@Composable
fun HistorySection(
    records: List<IpoAuctionRecord>,
    activeRecord: IpoAuctionRecord,
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
        // 頂部標題與「清空所有紀錄」按鈕
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📑", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                Text(
                    text = "本地歷史記錄與回測驗證",
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
                Text(text = "🗑️ 清空所有紀錄", fontSize = 12.sp)
            }
        }

        // 提示列
        Text(
            text = "💡 點擊任一標案的【🔍 載入查看】按鈕，上方儀表板將立即切換展示該檔的完整籌碼拆解與抽籤指南。",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFD97706), // Warm amber
            fontSize = 12.sp
        )

        // 歷史列表
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

@Composable
private fun HistoryRecordItemCard(
    record: IpoAuctionRecord,
    isActive: Boolean,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val statusType = record.statusType
    val (statusLabel, statusColor) = when (statusType) {
        IpoStatusType.GOLDEN -> Pair("GOLDEN HONEYMOON 🏆 黃金公式", StatusGreen)
        IpoStatusType.SAFE -> Pair("SAFE", Color(0xFF2563EB))
        IpoStatusType.CRITICAL_DANGER -> Pair("CRITICAL DANGER", StatusRed)
    }

    val minStr = TickSizeAligner.formatPrice(record.minWinningPrice)
    val maxStr = TickSizeAligner.formatPrice(record.maxWinningPrice ?: (record.avgWeightedPrice * 1.25))
    val marginStr = "${if (record.safetyMargin > 0) "+" else ""}${String.format("%.2f", record.safetyMargin)} 元"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${record.stockCode}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isActive) Color(0xFF2563EB) else LightCardBorder
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 上半部：股票名稱、狀態標籤、摘要資訊
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${record.stockName} (${record.stockCode})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2563EB))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "👉 目前查看中",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "🗓️ 分析日期 : ${record.analysisDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLightHint,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "合格倍數: ${String.format("%.2f", record.bidRatio)}x | 安全邊際: $marginStr | 廊道: $minStr ~ $maxStr 元",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 下半部：操作按鈕
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onLoad,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimaryDark,
                        containerColor = if (isActive) Color(0xFFEFF6FF) else Color.Transparent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(LightCardBorder)
                    ),
                    modifier = Modifier.testTag("btn_load_${record.stockCode}")
                ) {
                    Text(text = "🔍 載入查看", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryMuted),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(LightCardBorder)
                    ),
                    modifier = Modifier.testTag("btn_delete_${record.stockCode}")
                ) {
                    Text(text = "🗑️ 刪除", fontSize = 12.sp)
                }
            }
        }
    }
}
