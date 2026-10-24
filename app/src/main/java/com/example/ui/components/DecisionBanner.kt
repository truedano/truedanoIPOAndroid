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

    val (bannerBg, pillBg, statusTitle, statusSubtitle) = when (statusType) {
        IpoStatusType.GOLDEN -> {
            listOf(
                BannerGoldenGreen,
                BannerGoldenGreenPill,
                "🏆 達成黃金獲利公式 (GOLDEN HONEYMOON)",
                "「合格投標倍數大於 5 倍」+「公開申購價遠低於最低得標價」= 安全且豐厚的蜜月行情！"
            )
        }
        IpoStatusType.SAFE -> {
            listOf(
                Color(0xFF2563EB), // 皇家藍
                Color(0xFF1D4ED8),
                "🛡️ 常態穩健行情 (SAFE)",
                "具備基礎安全邊際，投標熱度處於常態健康水準，走勢預計於法人得標區間內穩定運行。"
            )
        }
        IpoStatusType.CRITICAL_DANGER -> {
            listOf(
                BannerInvertedRed,
                BannerInvertedRedPill,
                "⚠️ 高危倒掛警訊 (CRITICAL DANGER)",
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
        colors = CardDefaults.cardColors(containerColor = bannerBg as Color)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左側：股票名稱代號與狀態標章
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${record.stockName} (${record.stockCode})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = statusTitle as String,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (statusType == IpoStatusType.GOLDEN) GoldMedal else Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = statusSubtitle as String,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.95f),
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // 右側：浮動藥丸形狀凸顯「預期交易廊道」
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background((pillBg as Color).copy(alpha = 0.85f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("banner_corridor_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "預期交易廊道",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$supportStr ~ $resistanceStr 元",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
