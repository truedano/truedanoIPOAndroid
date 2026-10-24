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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.domain.model.ExitScenarioResult
import com.example.domain.model.ExitScenarioType
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
import java.text.NumberFormat
import java.util.Locale

/**
 * 純抽籤戶實戰指南：單張淨賺試算與關鍵時程
 * 忠實復刻截圖 Image 2 上半部
 */
@Composable
fun LotteryExitSection(
    record: IpoAuctionRecord,
    scenarios: List<ExitScenarioResult>,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.TAIWAN)
    val singleLotCost = record.subscriptionPrice * 1000.0 + 70.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lottery_exit_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 區塊主標題
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "💳", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
            Text(
                text = "純抽籤戶實戰指南：單張淨賺試算與關鍵時程",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                fontSize = 18.sp
            )
        }

        // 頂部時程與本金卡片 (淡藍底色、微深藍外框)
        TopScheduleCard(
            record = record,
            singleLotCost = singleLotCost,
            numberFormat = numberFormat
        )

        // 三段出場試算橫幅卡片
        scenarios.forEach { scenario ->
            ScenarioTacticCard(
                scenario = scenario,
                numberFormat = numberFormat
            )
        }
    }
}

@Composable
private fun TopScheduleCard(
    record: IpoAuctionRecord,
    singleLotCost: Double,
    numberFormat: NumberFormat
) {
    val statusType = record.statusType
    val (boardTitle, boardSubtitle) = when (statusType) {
        IpoStatusType.GOLDEN -> Pair(
            "【🏆 強力申購！黃金蜜月局】",
            "超額競標逾 5 倍，且機構得標價遠高於抽籤價，高勝率蜜月首選標案！"
        )
        IpoStatusType.SAFE -> Pair(
            "【🛡️ 安全申購！穩健防守局】",
            "具備基礎安全邊際，投標熱度正常，開盤預期守穩最低得標價，屬常態正期望值個案。"
        )
        IpoStatusType.CRITICAL_DANGER -> Pair(
            "【⚠️ 高危警示！慎防破發倒貨局】",
            "公開申購價高於或等於最低得標價，機構法人持股成本比散戶更低，上市首日倒貨風險極高！"
        )
    }

    val bannerBorder = Color(0xFFBFDBFE) // Light blue border
    val bannerBg = Color(0xFFF0F7FF) // Very soft blue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bannerBg)
            .border(1.dp, bannerBorder, RoundedCornerShape(14.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = boardTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A) // Navy blue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = boardSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryMuted,
                        lineHeight = 18.sp
                    )
                }

                // 右側單張扣款本金卡片 (純白小卡)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, LightCardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "單張扣款準備本金 (含70元手續費)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryMuted,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$${numberFormat.format(singleLotCost.toLong())} 元",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimaryDark,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFDBEAFE))
            Spacer(modifier = Modifier.height(10.dp))

            // 關鍵時程 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗓️ 競拍開標日 : ${record.auctionDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1E40AF),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "⏳ 申購扣款日前夕 : 帳戶請備妥本金",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF15803D), // Green text
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "🚀 預計撥券掛牌日 : ${record.listingDate ?: "開標後 7~9 個營業日"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7E22CE), // Purple
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ScenarioTacticCard(
    scenario: ExitScenarioResult,
    numberFormat: NumberFormat
) {
    val isProfit = scenario.netProfit >= 0
    val profitColor = if (isProfit) {
        if (scenario.type == ExitScenarioType.OPTIMISTIC) Color(0xFFC026D3) else StatusGreen
    } else StatusRed

    val (titleLabel, tacticText) = when (scenario.type) {
        ExitScenarioType.CONSERVATIVE -> Pair(
            "下檔防守線 (最低得標價)",
            "法人集體保本防守底線。若開盤疲軟打回此價位，危險股應立即市價出清避險；黃金股可觀察承接量能。"
        )
        ExitScenarioType.BASE -> Pair(
            "合理中樞線 (得標加權均價)",
            "法人多空平衡成本線。開盤 8:30~9:00 試撮若高於此價，開盤後可順利在此區間順勢分批獲利了結。"
        )
        ExitScenarioType.OPTIMISTIC -> Pair(
            "上檔極限線 (最高得標價)",
            "法人短期出價接受度天花板。掛牌首五日若盤中強勢衝高至此價位附近，絕不留戀，立即全數掛單落袋為安！"
        )
    }

    val alignedPriceStr = TickSizeAligner.formatPrice(scenario.alignedTargetPrice)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tactic_card_${scenario.type.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(LightCardBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = titleLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "出場目標價: $alignedPriceStr 元",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isProfit) "+" else "-"}$${numberFormat.format(kotlin.math.abs(scenario.netProfit).toLong())} 元",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = profitColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "報酬率: ${if (scenario.roiPercent >= 0) "+" else ""}${String.format("%.1f", scenario.roiPercent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = profitColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 戰術指引 Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEFCE8)) // Light warm yellow
                    .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "💡", fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp, top = 2.dp))
                    Text(
                        text = "戰術 : $tacticText",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF854D0E), // Warm dark amber
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
