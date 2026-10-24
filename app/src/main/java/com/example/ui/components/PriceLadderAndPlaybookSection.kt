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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.domain.model.IpoDecisionResult
import com.example.domain.model.TickSizeAligner
import com.example.ui.theme.LightAppBackground
import com.example.ui.theme.LightCardBackground
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.Step3PurpleText
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

/**
 * 法人籌碼定錨價格階梯 (Price Ladder) & 實戰操作指引 (Playbook)
 * 忠實復刻截圖 Image 2 下半部
 */
@Composable
fun PriceLadderAndPlaybookSection(
    record: IpoAuctionRecord,
    result: IpoDecisionResult,
    modifier: Modifier = Modifier
) {
    val corridor = result.corridor

    val emergingStr = corridor.emergingPrice?.let { TickSizeAligner.formatPrice(it) } ?: "無興櫃"
    val maxPriceStr = TickSizeAligner.formatPrice(corridor.alignedResistancePrice)
    val avgPriceStr = TickSizeAligner.formatPrice(corridor.alignedPivotPrice)
    val minPriceStr = TickSizeAligner.formatPrice(corridor.alignedSupportPrice)
    val subPriceStr = TickSizeAligner.formatPrice(record.subscriptionPrice)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("price_ladder_playbook_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 法人籌碼定錨價格階梯 (Price Ladder)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📊", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                Text(
                    text = "法人籌碼定錨價格階梯 (Price Ladder)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightCardBackground),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LightCardBorder)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 虛假定錨點 (興櫃均價)
                    LadderRung(
                        title = "虛假定錨點 (興櫃均價)",
                        subtitle = "興櫃成交參考",
                        priceStr = "$emergingStr 元",
                        barColor = Color(0xFF94A3B8),
                        priceColor = TextSecondaryMuted,
                        isDashed = true
                    )

                    LadderConnectorText("▼ 法人接受度上限天花板 ▼")

                    // 上檔壓力點 (最高得標價格)
                    LadderRung(
                        title = "上檔壓力點 (最高得標價格)",
                        subtitle = "短期內法人圈出價接受度上限",
                        priceStr = "$maxPriceStr 元",
                        barColor = Step3PurpleText,
                        priceColor = Step3PurpleText
                    )

                    LadderConnectorText("▲ 法人集體多空共識中樞 ▼")

                    // 多空平衡中樞 (得標加權均價)
                    LadderRung(
                        title = "多空平衡中樞 (得標加權均價)",
                        subtitle = "全市場得標法人之加權持股成本",
                        priceStr = "$avgPriceStr 元",
                        barColor = Color(0xFF2563EB),
                        priceColor = Color(0xFF2563EB)
                    )

                    LadderConnectorText("▲ 上市初期開盤防守底線 ▲")

                    // 下檔支撐點 (最低得標價格)
                    LadderRung(
                        title = "下檔支撐點 (最低得標價格)",
                        subtitle = "低於此價所有得標機構全面虧損，具強烈防守意願",
                        priceStr = "$minPriceStr 元",
                        barColor = StatusGreen,
                        priceColor = StatusGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 散戶抽籤價 (公開申購價)
                    LadderRung(
                        title = "散戶抽籤價 (公開申購價)",
                        subtitle = "中籤散戶之原始取得成本",
                        priceStr = "$subPriceStr 元",
                        barColor = Color(0xFF0F172A),
                        priceColor = TextPrimaryDark
                    )
                }
            }
        }

        // 2. 實戰操作指引 (Playbook)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎯", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                Text(
                    text = "實戰操作指引 (Playbook)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightCardBackground),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LightCardBorder)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "抽籤戶 vs 空手戶操作方針",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 抽籤中籤者戰術
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎉", fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
                                Text(
                                    text = "抽籤中籤者戰術：",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "建議觀察開盤價是否站穩最低得標價 ($minPriceStr 元)，並於加權均價 ($avgPriceStr 元) 至最高得標價 ($maxPriceStr 元) 區間順勢調節。",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryDark,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 次級市場空手者戰術
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "👀", fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
                                Text(
                                    text = "次級市場空手者戰術：",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "首日絕不追高！若盤中受散戶倒貨打壓，拉回至最低得標價 ($minPriceStr 元) 至得標加權均價 ($avgPriceStr 元) 區間且有支撐時，方為機構法人合理之進場安全區。",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryMuted,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LadderRung(
    title: String,
    subtitle: String,
    priceStr: String,
    barColor: Color,
    priceColor: Color,
    isDashed: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LightAppBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = priceStr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = priceColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun LadderConnectorText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            fontSize = 10.sp
        )
    }
}
