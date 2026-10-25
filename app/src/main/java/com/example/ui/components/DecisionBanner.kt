package com.example.ui.components

import androidx.compose.foundation.background
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
import com.example.domain.model.IpoStatusType
import com.example.domain.model.TickSizeAligner
import com.example.ui.theme.BannerGoldenGreen
import com.example.ui.theme.BannerGoldenGreenPill
import com.example.ui.theme.BannerInvertedRed
import com.example.ui.theme.BannerInvertedRedPill
import com.example.ui.theme.GoldMedal

/**
 * 核心決策 Banner（狀態橫幅大卡片）
 * 支援三種色彩情境：
 * 1. GOLDEN HONEYMOON: 森林綠背景 + 🏆 金色獎章
 * 2. SAFE (常態穩健行情): 皇家藍背景 + 🛡️ 常態穩健
 * 3. CRITICAL DANGER: 珊瑚紅背景 + ⚠️ 高危倒掛
 */
@Composable
fun DecisionBanner(
    record: IpoAuctionRecord,
    result: IpoDecisionResult,
    modifier: Modifier = Modifier
) {
    val statusType = record.statusType

    val (bannerBg, pillBg, statusBadgeTitle, statusSubtitle) = when (statusType) {
        IpoStatusType.GOLDEN -> {
            listOf(
                BannerGoldenGreen,
                BannerGoldenGreenPill,
                "🏆 達成黃金獲利公式",
                "「合格投標倍數大於 5 倍」+「公開申購價遠低於最低得標價」，具備安全且豐厚的蜜月行情！"
            )
        }
        IpoStatusType.SAFE -> {
            listOf(
                Color(0xFF2563EB), // 皇家藍
                Color(0xFF1D4ED8),
                "🛡️ 常態穩健行情",
                "具備基礎安全邊際，投標熱度處於常態健康水準，走勢預計於法人得標區間內穩定運行。"
            )
        }
        IpoStatusType.CRITICAL_DANGER -> {
            listOf(
                BannerInvertedRed,
                BannerInvertedRedPill,
                "⚠️ 高危倒掛警訊",
                "公開申購價高於或等於最低得標價，機構進場成本比抽籤散戶更便宜，留意上市首日倒貨套利破發風險！"
            )
        }
    }

    val supportStr = TickSizeAligner.formatPrice(result.corridor.alignedSupportPrice)
    val resistanceStr = TickSizeAligner.formatPrice(result.corridor.alignedResistancePrice)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("decision_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bannerBg as Color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 1. 頂部列：股票名稱與代號 + 狀態標章膠囊
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.stockCode,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // 右側狀態徽章
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusBadgeTitle as String,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (statusType == IpoStatusType.GOLDEN) GoldMedal else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 核心數值區塊：預期交易廊道
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background((pillBg as Color).copy(alpha = 0.85f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("banner_corridor_pill")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "預期交易廊道 (支撐 ~ 壓力)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$supportStr ~ $resistanceStr 元",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. 底部說明文字（滿版寬度橫向排版）
            Text(
                text = statusSubtitle as String,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = 20.sp
            )
        }
    }
}
