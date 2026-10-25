package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SpoDecisionResult
import com.example.domain.model.SpoLotteryRecord
import com.example.domain.model.SpoRiskGrade
import com.example.ui.theme.BannerGoldenGreen
import com.example.ui.theme.BannerInvertedRed
import com.example.ui.theme.LightCardBackground
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.SearchButtonRed
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextLightHint
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpoHeaderAndSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    allRecords: List<SpoLotteryRecord>,
    activeRecord: SpoLotteryRecord,
    onSelectRecord: (SpoLotteryRecord) -> Unit,
    isSyncing: Boolean = false,
    onSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "台股現金增資（SPO）公開抽籤套利決策系統",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimaryDark,
                    fontSize = 19.sp
                )
                Text(
                    text = "市價折價安全墊 • 融券放空鎖利評估 • 撥券日防護期試算",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(
                onClick = onSync,
                enabled = !isSyncing
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = SearchButtonRed
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "同步證交所最新公告",
                        tint = TextSecondaryMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("輸入股票代號 (如 2884)", color = TextLightHint, fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        onSearch()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SearchButtonRed,
                    unfocusedBorderColor = LightCardBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSearch()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SearchButtonRed),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "查詢")
                Spacer(modifier = Modifier.width(4.dp))
                Text("分析", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 快速切換 Chips
        Text(
            text = "⚡ 快速切換實戰案例：",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondaryMuted,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            allRecords.forEach { record ->
                val isSelected = record.stockCode == activeRecord.stockCode
                val chipBg = if (isSelected) TextPrimaryDark else Color.White
                val chipTextColor = if (isSelected) Color.White else TextPrimaryDark
                val borderColor = if (isSelected) TextPrimaryDark else LightCardBorder

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable { onSelectRecord(record) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${record.stockCode} ${record.stockName}",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = chipTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun SpoDecisionBanner(
    result: SpoDecisionResult,
    modifier: Modifier = Modifier
) {
    val (bannerColor, icon) = when (result.grade) {
        SpoRiskGrade.ARBITRAGE_GOLDEN -> BannerGoldenGreen to Icons.Default.Lock
        SpoRiskGrade.SAFE_BUFFER -> Color(0xFF1565C0) to Icons.Default.CheckCircle
        SpoRiskGrade.MODERATE -> StatusAmber to Icons.Default.Info
        SpoRiskGrade.HIGH_DANGER -> BannerInvertedRed to Icons.Default.Warning
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bannerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = result.grade.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                    Text(
                        text = result.grade.badgeTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = result.actionRecommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SpoMetricsGrid(
    record: SpoLotteryRecord,
    result: SpoDecisionResult,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "現價折價率",
                value = "${String.format("%.1f", result.discountRatio)}%",
                sub = "現價 ${record.marketPrice} vs 承銷 ${record.subscriptionPrice}",
                highlightColor = if (result.discountRatio >= 20.0) StatusGreen else if (result.discountRatio >= 10.0) StatusAmber else StatusRed,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "每筆預估價差毛利",
                value = "NT$ ${String.format("%,d", record.spreadAmountPerLot.roundToInt())}",
                sub = "可申購 ${record.sharesPerLot} 張（${record.sharesPerLot * 1000} 股）",
                highlightColor = if (record.spreadAmountPerLot > 10000) StatusGreen else TextPrimaryDark,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "融券鎖利可行性",
                value = if (result.isArbitrageViable) "可融券鎖利 🔒" else "無融券避險 ❌",
                sub = if (result.isArbitrageViable) "額度充裕，可放空避險" else "需承擔 7 天現貨波動",
                highlightColor = if (result.isArbitrageViable) StatusGreen else StatusAmber,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "增資股本稀釋率",
                value = if (record.dilutionRate != null) "${String.format("%.1f", record.dilutionRate)}%" else "未提供",
                sub = if (result.dilutionWarning) "🚨 稀釋 > 15% 賣壓大" else "稀釋溫和可控",
                highlightColor = if (result.dilutionWarning) StatusRed else StatusGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    sub: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = title, fontSize = 12.sp, color = TextSecondaryMuted, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = highlightColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = sub, fontSize = 11.sp, color = TextLightHint)
        }
    }
}

@Composable
fun SpoInteractivePriceAdjuster(
    currentMarketPrice: Double,
    customPriceText: String,
    onPriceChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🎛️ 即時現股市價試算（可手動輸入）",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "若現股盤中或收盤變動，輸入最新價格即時試算最新折價率與撥券淨利：",
                fontSize = 11.sp,
                color = TextSecondaryMuted,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customPriceText,
                    onValueChange = onPriceChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("當前現股市價 (元)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SearchButtonRed,
                        unfocusedBorderColor = LightCardBorder
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "目前市價: ${currentMarketPrice} 元",
                        fontSize = 12.sp,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SpoScenariosSection(
    result: SpoDecisionResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 撥券日現股走勢三段情境推演",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                fontSize = 16.sp
            )
            Text(
                text = "扣除 0.3% 證所稅、券商手續費與抽籤 70 元處理工本費後之預估每筆淨賺：",
                fontSize = 11.sp,
                color = TextSecondaryMuted,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            result.scenarios.forEach { scenario ->
                val isProfit = scenario.netProfitPerLot > 0
                val color = if (isProfit) StatusGreen else StatusRed

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scenario.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "預估撥券價: ${scenario.projectedExitPrice} 元",
                            fontSize = 12.sp,
                            color = TextSecondaryMuted
                        )
                    }

                    Text(
                        text = scenario.assumptionDesc,
                        fontSize = 11.sp,
                        color = TextLightHint,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "淨報酬率: ${String.format("%+.1f", scenario.netReturnRatio)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = color
                        )
                        Text(
                            text = "預估單筆淨利: NT$ ${String.format("%,d", scenario.netProfitPerLot.roundToInt())} 元",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpoKeyDatesAndScheduleSection(
    record: SpoLotteryRecord,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🗓️ 關鍵時程表與申購時鐘",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateColumn(label = "申購起迄", date = "${record.startDate}\n~ ${record.endDate}")
                DateColumn(label = "抽籤日", date = record.lotteryDate)
                DateColumn(label = "撥券上市日", date = record.listingDate, highlight = true)
            }

            if (record.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "💡 實戰筆記：${record.note}",
                        fontSize = 12.sp,
                        color = TextPrimaryDark,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DateColumn(label: String, date: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = TextSecondaryMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = date,
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Black else FontWeight.SemiBold,
            color = if (highlight) SearchButtonRed else TextPrimaryDark
        )
    }
}
