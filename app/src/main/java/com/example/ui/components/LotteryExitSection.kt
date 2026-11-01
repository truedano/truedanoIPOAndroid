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
 * 專為手機直式最佳化之金融卡片佈局，分層清晰，永不擠壓碰撞。
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

        // 頂部時程與本金卡片 (淡藍底色、微深藍外框，分層佈局)
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
    val (boardTitle, boardSubtitle, titleColor) = when (statusType) {
        IpoStatusType.PENDING_AUCTION -> Triple(
            "【📢 競拍進行中！等待開標局】",
            "標案目前處於投標或待開標階段，預備抽籤本金，開標後將自動為您敲定法人得標安全邊際！",
            Color(0xFF0F766E)
        )
        IpoStatusType.GOLDEN -> Triple(
            "【🏆 強力申購！黃金蜜月局】",
            "超額競標逾 5 倍，且機構得標價遠高於抽籤價，高勝率蜜月首選標案！",
            Color(0xFF047857) // Emerald Green
        )
        IpoStatusType.SAFE -> Triple(
            "【🛡️ 安全申購！穩健防守局】",
            "具備基礎安全邊際，投標熱度正常，開盤預期守穩最低得標價，屬常態正期望值個案。",
            Color(0xFF1D4ED8) // Royal Blue
        )
        IpoStatusType.CRITICAL_DANGER -> Triple(
            "【⚠️ 高危警示！慎防破發倒貨局】",
            "公開申購價高於或等於最低得標價，機構法人持股成本比散戶更低，上市首日倒貨風險極高！",
            Color(0xFFB91C1C) // Crimson Red
        )
    }

    val bannerBorder = Color(0xFFBFDBFE) // Light blue border
    val bannerBg = Color(0xFFF0F7FF) // Very soft blue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bannerBg)
            .border(1.dp, bannerBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 第一層：申購定調標題與完整解說（滿版展開，拒絕直排擠壓）
            Column {
                Text(
                    text = boardTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = boardSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryMuted,
                    lineHeight = 20.sp
                )
            }

            // 第二層：單張準備本金醒目焦點盒
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "單張申購準備本金",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "含 20 元處理費 + 50 元中籤工本費",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLightHint,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "$${numberFormat.format(singleLotCost.toLong())} 元",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFDBEAFE))

            // 第三層：關鍵時程時序排版（清楚 3 步時序，不再水平撞車）
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "關鍵時程追蹤",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗓️ 開標日 : ${record.auctionDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1E40AF),
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "🚀 掛牌日 : ${record.listingDate ?: "開標後約7天"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7E22CE),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "⏳ 提醒：申購扣款日前夕 24:00 前，帳戶請備妥本金！",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
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
                    Spacer(modifier = Modifier.height(2.dp))
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
