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
import com.example.domain.model.IpoAuctionRecord
import com.example.domain.model.IpoDecisionResult
import com.example.domain.model.MarketHeatLevel
import com.example.domain.model.SafetyMarginStatus
import com.example.domain.model.TickSizeAligner
import com.example.ui.theme.LightCardBackground
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.Step1BlueBg
import com.example.ui.theme.Step1BlueText
import com.example.ui.theme.Step2GreenBg
import com.example.ui.theme.Step2GreenText
import com.example.ui.theme.Step3PurpleBg
import com.example.ui.theme.Step3PurpleText
import com.example.ui.theme.TextLightHint
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted
import java.text.NumberFormat
import java.util.Locale

/**
 * 結構化籌碼拆解三大步驟卡片區（清晰白底圓角現代金融卡片）
 */
@Composable
fun StructuredStepCards(
    record: IpoAuctionRecord,
    result: IpoDecisionResult,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.TAIWAN)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 卡片 1【第一步：市場熱度】
        Step1HeatCard(
            ratio = record.bidRatio,
            heatLevel = result.heatLevel,
            totalShares = record.totalAuctionShares,
            validShares = record.validBidShares,
            numberFormat = numberFormat
        )

        // 卡片 2【第二步：安全邊際】
        Step2SafetyMarginCard(
            margin = record.safetyMargin,
            marginStatus = result.safetyMarginStatus,
            minPrice = record.minWinningPrice,
            subPrice = record.subscriptionPrice
        )

        // 卡片 3【第三步：法人真實期待值】
        Step3PriceCorridorCard(
            supportPrice = result.corridor.alignedSupportPrice,
            resistancePrice = result.corridor.alignedResistancePrice,
            emergingPrice = record.emergingMarketPrice,
            isBubble = result.corridor.isEmergingBubble
        )
    }
}

@Composable
private fun Step1HeatCard(
    ratio: Double,
    heatLevel: MarketHeatLevel,
    totalShares: Long,
    validShares: Long,
    numberFormat: NumberFormat
) {
    val statusColor = when (heatLevel) {
        MarketHeatLevel.PENDING -> Color(0xFF0F766E)
        MarketHeatLevel.HOT -> StatusGreen
        MarketHeatLevel.NEUTRAL -> TextPrimaryDark
        MarketHeatLevel.COLD -> StatusRed
    }

    val explanation = when (heatLevel) {
        MarketHeatLevel.PENDING -> "標案目前處於投標或待開標階段，待開標日結算揭曉合格投標張數與市場熱度倍數。"
        MarketHeatLevel.HOT -> "資金高度追捧，市場籌碼供不應求。法人大戶投標熱絡，上市首日的蜜月行情非常值得期待！"
        MarketHeatLevel.NEUTRAL -> "投標倍數處於中性常態區間，法人進場意願平穩，上市初期依大盤氛圍尋求價格中樞。"
        MarketHeatLevel.COLD -> "投標動能明顯偏冷，法人參標意願低迷，上市首日買盤缺乏追價意願，需嚴控風險。"
    }

    BaseStepCard(
        pillLabel = "第一步：市場熱度",
        pillBg = Step1BlueBg,
        pillTextColor = Step1BlueText,
        subtitle = "合格投標量 / 競拍總量",
        mainValue = if (heatLevel == MarketHeatLevel.PENDING) "待開標" else "${String.format("%.2f", ratio)}x",
        mainValueColor = TextPrimaryDark,
        statusText = heatLevel.title,
        statusColor = statusColor,
        explanation = explanation,
        footerText = "釋出: ${numberFormat.format(totalShares)} 張 | 合格標: ${numberFormat.format(validShares)} 張",
        testTag = "step_1_heat_card"
    )
}

@Composable
private fun Step2SafetyMarginCard(
    margin: Double,
    marginStatus: SafetyMarginStatus,
    minPrice: Double,
    subPrice: Double
) {
    val isPending = marginStatus == SafetyMarginStatus.PENDING
    val isSafe = marginStatus.isSafe
    val statusColor = if (isPending) Color(0xFF0F766E) else if (isSafe) StatusGreen else StatusRed
    val formattedMargin = if (isPending) "待定價" else "${if (margin > 0) "+" else ""}${String.format("%.2f", margin)} 元"

    val explanation = if (isPending) {
        "競價拍賣進行中，底標價為 ${TickSizeAligner.formatPrice(minPrice)} 元，暫定公開申購價為 ${TickSizeAligner.formatPrice(subPrice)} 元。開標後將自動為您比對機構法人進場成本與抽籤價差。"
    } else if (isSafe) {
        "所有競拍得標法人的成本（最低 ${TickSizeAligner.formatPrice(minPrice)} 元）都高於你的抽籤價（${TickSizeAligner.formatPrice(subPrice)} 元）。抽籤者具備極佳的持股安全邊際，首日賺取價差機率極高。"
    } else {
        "公開申購價（${TickSizeAligner.formatPrice(subPrice)} 元）高於或等於機構得標價（最低 ${TickSizeAligner.formatPrice(minPrice)} 元）。機構法人持倉比抽籤散戶更便宜，上市首日極易引發法人拋售倒貨套利！"
    }

    BaseStepCard(
        pillLabel = "第二步：安全邊際",
        pillBg = Step2GreenBg,
        pillTextColor = Step2GreenText,
        subtitle = "最低得標價 vs 公開申購價",
        mainValue = formattedMargin,
        mainValueColor = statusColor,
        statusText = if (isPending) "待開標定錨 (尚未開標)" else if (isSafe) "安全訊號 (具安全邊際)" else "極度危險倒掛 (無安全邊際)",
        statusColor = statusColor,
        explanation = explanation,
        footerText = "抽籤申購價: ${TickSizeAligner.formatPrice(subPrice)} 元 | 最低得標價: ${TickSizeAligner.formatPrice(minPrice)} 元",
        testTag = "step_2_margin_card"
    )
}

@Composable
private fun Step3PriceCorridorCard(
    supportPrice: Double,
    resistancePrice: Double,
    emergingPrice: Double?,
    isBubble: Boolean
) {
    val supportStr = TickSizeAligner.formatPrice(supportPrice)
    val resistanceStr = TickSizeAligner.formatPrice(resistancePrice)
    val mainInterval = "$supportStr ~ $resistanceStr 元"

    val explanation = "最高得標價 ($resistanceStr 元) 為法人短期出價接受度之極限，股價將在最低得標價 ($supportStr 元) 與最高得標價之間尋求平衡。"

    val footer = if (emergingPrice != null) {
        val emStr = TickSizeAligner.formatPrice(emergingPrice)
        if (isBubble) "興櫃參考均價: $emStr 元 (散戶泡沫溢價警訊！)"
        else "興櫃參考均價: $emStr 元 (未脫鉤)"
    } else {
        "下檔支撐點: $supportStr 元 | 上檔天花板: $resistanceStr 元"
    }

    BaseStepCard(
        pillLabel = "第三步：法人真實期待值",
        pillBg = Step3PurpleBg,
        pillTextColor = Step3PurpleText,
        subtitle = "真實價格發現區間",
        mainValue = mainInterval,
        mainValueColor = TextPrimaryDark,
        statusText = "下檔支撐 → 上檔天花板",
        statusColor = Step3PurpleText,
        explanation = explanation,
        footerText = footer,
        testTag = "step_3_corridor_card"
    )
}

@Composable
private fun BaseStepCard(
    pillLabel: String,
    pillBg: Color,
    pillTextColor: Color,
    subtitle: String,
    mainValue: String,
    mainValueColor: Color,
    statusText: String,
    statusColor: Color,
    explanation: String,
    footerText: String,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(LightCardBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 上方膠囊標籤
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillBg)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = pillLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = pillTextColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 副標題
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 大字醒目顯示
            Text(
                text = mainValue,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = mainValueColor,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 狀態標籤
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 說明文字
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = LightCardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // 底部輔助微字
            Text(
                text = footerText,
                style = MaterialTheme.typography.labelSmall,
                color = TextLightHint,
                fontSize = 11.sp
            )
        }
    }
}
