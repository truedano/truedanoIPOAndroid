package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SpoDecisionBanner
import com.example.ui.components.SpoHeaderAndSearchBar
import com.example.ui.components.SpoInteractivePriceAdjuster
import com.example.ui.components.SpoKeyDatesAndScheduleSection
import com.example.ui.components.SpoMetricsGrid
import com.example.ui.components.SpoScenariosSection
import com.example.ui.theme.LightAppBackground
import com.example.ui.theme.LightCardBackground
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.SearchButtonRed
import com.example.ui.theme.TextLightHint
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

@Composable
fun SpoDashboardScreen(
    viewModel: SpoDecisionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(LightAppBackground),
        containerColor = LightAppBackground,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // 1. 現增頂部標題、搜尋列與進行中標籤（嚴格過濾抽籤日未過期者）
                SpoHeaderAndSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.performSearch() },
                    allRecords = uiState.displayRecords,
                    activeRecord = uiState.currentRecord,
                    onSelectRecord = { viewModel.selectRecord(it) },
                    isSyncing = uiState.isSyncing,
                    onSync = { viewModel.syncTwseData() }
                )
            }

            val currentRecord = uiState.currentRecord
            val decisionResult = uiState.decisionResult

            if (currentRecord != null && decisionResult != null) {
                item {
                    // 2. 核心現增決策 Banner
                    SpoDecisionBanner(result = decisionResult)
                }

                item {
                    // 3. 現增四大核心指標卡片
                    SpoMetricsGrid(
                        record = currentRecord,
                        result = decisionResult
                    )
                }

                item {
                    // 4. 即時現股市價動態試算輸入器
                    SpoInteractivePriceAdjuster(
                        currentMarketPrice = currentRecord.marketPrice,
                        customPriceText = uiState.customMarketPriceText,
                        onPriceChanged = { viewModel.onMarketPriceChanged(it) }
                    )
                }

                item {
                    // 5. 撥券日現股走勢三段情境推演
                    SpoScenariosSection(result = decisionResult)
                }

                item {
                    // 6. 關鍵時程與實戰筆記
                    SpoKeyDatesAndScheduleSection(record = currentRecord)
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = SearchButtonRed,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "連線臺灣證交所同步最新增資抽籤資訊中...",
                                    fontSize = 14.sp,
                                    color = TextSecondaryMuted
                                )
                            } else {
                                Text(
                                    text = "📡 目前臺灣證券交易所無進行中的增資申購案",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "所有前期增資標案之抽籤日皆已結束。\n可點擊右上角或下方按鈕隨時獲取證交所最新發布之標案。",
                                    fontSize = 12.sp,
                                    color = TextSecondaryMuted,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.syncTwseData() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SearchButtonRed)
                                ) {
                                    Text("重新檢查證交所最新公告", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // 7. 頁尾
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "台股現金增資（SPO）即時抽籤決策系統 | TWSE 官方即時資料驅動",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLightHint,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
