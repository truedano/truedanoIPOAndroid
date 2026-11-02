package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.LightAppBackground
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.SearchButtonRed
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

@Composable
fun AboutAppDialog(
    onDismiss: () -> Unit,
    onCheckUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "軟體資訊",
                tint = SearchButtonRed,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "台股 IPO 競拍決策",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "基於 TWSE 真實開標數據與升降單位對齊的蜜月決策引擎",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 版號資訊卡片
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, LightCardBorder, RoundedCornerShape(12.dp)),
                    color = LightAppBackground
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "當前版本",
                                fontSize = 11.sp,
                                color = TextSecondaryMuted
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "建置號 (Build)",
                                fontSize = 11.sp,
                                color = TextSecondaryMuted
                            )
                            Text(
                                text = "${BuildConfig.VERSION_CODE}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SearchButtonRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 手動檢查更新按鈕
                Button(
                    onClick = {
                        onDismiss()
                        onCheckUpdate()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SearchButtonRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "檢查更新",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "手動檢查更新",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "所有開標、抽籤與市場數據均同步自臺灣證券交易所 (TWSE)，僅供輔助投資決策參考。",
                    fontSize = 11.sp,
                    color = TextSecondaryMuted,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("關閉")
            }
        }
    )
}
