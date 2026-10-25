package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DecisionBanner
import com.example.ui.components.IpoHeaderAndSearchBar
import com.example.ui.components.LotteryExitSection
import com.example.ui.components.PriceLadderAndPlaybookSection
import com.example.ui.components.StructuredStepCards
import com.example.ui.theme.LightAppBackground
import com.example.ui.theme.LightCardBackground
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.SearchButtonRed
import com.example.ui.theme.TextLightHint
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

@Composable
fun IpoDashboardScreen(
    viewModel: IpoDecisionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // 1. 頂部標題、搜尋列與進行中 IPO 快速 Chips
                IpoHeaderAndSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.performSearch() },
                    isSearching = uiState.isSearching,
                    allRecords = uiState.displayRecords,
                    activeRecord = uiState.currentRecord,
                    onSelectRecord = { viewModel.selectRecord(it) },
                    onSync = { viewModel.syncTwseData() }
                )
            }

            val currentRecord = uiState.currentRecord
            val decisionResult = uiState.decisionResult

            if (currentRecord != null && decisionResult != null) {
                item {
                    // 2. 核心決策 Banner
                    DecisionBanner(
                        record = currentRecord,
                        result = decisionResult
                    )
                }

                item {
                    // 3. 區塊標題：📋 結構化籌碼拆解三大步驟
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("section_title_steps"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📋", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(
                            text = "結構化籌碼拆解三大步驟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. 三大步驟白底圓角現代金融卡片
                    StructuredStepCards(
                        record = currentRecord,
                        result = decisionResult
                    )
                }

                item {
                    // 5. 純抽籤戶實戰指南：單張淨賺試算與關鍵時程
                    LotteryExitSection(
                        record = currentRecord,
                        scenarios = decisionResult.exitScenarios
                    )
                }

                item {
                    // 6. 法人籌碼定錨價格階梯 (Price Ladder) & 實戰操作指引 (Playbook)
                    PriceLadderAndPlaybookSection(
                        record = currentRecord,
                        result = decisionResult
                    )
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
                            if (uiState.isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = SearchButtonRed,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "連線臺灣證交所同步最新競拍資訊中...",
                                    fontSize = 14.sp,
                                    color = TextSecondaryMuted
                                )
                            } else {
                                Text(
                                    text = "📡 目前臺灣證券交易所無進行中的初上市櫃競拍",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "所有前期初上市櫃標案之蜜月期皆已結束。\n可點擊右上角或下方按鈕隨時獲取證交所最新發布之競拍開標公告。",
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
                                    Text("重新檢查證交所最新競拍公告", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // 7. 頁尾著作標記
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "台股 IPO 競拍真實蜜月行情預判系統 v0.2.0 | TWSE 官方即時資料驅動",
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
