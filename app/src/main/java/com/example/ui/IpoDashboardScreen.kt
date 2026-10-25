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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.HistorySection
import com.example.ui.components.IpoHeaderAndSearchBar
import com.example.ui.components.LotteryExitSection
import com.example.ui.components.PriceLadderAndPlaybookSection
import com.example.ui.components.StructuredStepCards
import com.example.ui.theme.LightAppBackground
import com.example.ui.theme.TextLightHint
import com.example.ui.theme.TextPrimaryDark

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
                // 1. 頂部標題與「一鍵智能查詢」輸入區 (Image 1 頂部)
                IpoHeaderAndSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.performSearch() },
                    isSearching = uiState.isSearching
                )
            }

            item {
                // 2. 核心決策 Banner（狀態橫幅大卡片：藍/綠/紅 + 預期交易廊道藥丸）
                DecisionBanner(
                    record = uiState.currentRecord,
                    result = uiState.decisionResult
                )
            }

            item {
                // 3. 區塊標題：📋 結構化籌碼拆解三大步驟 (Image 1 下半部)
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
                    record = uiState.currentRecord,
                    result = uiState.decisionResult
                )
            }

            item {
                // 5. 純抽籤戶實戰指南：單張淨賺試算與關鍵時程 (Image 2 上半部)
                LotteryExitSection(
                    record = uiState.currentRecord,
                    scenarios = uiState.decisionResult.exitScenarios
                )
            }

            item {
                // 6. 法人籌碼定錨價格階梯 (Price Ladder) & 實戰操作指引 (Playbook) (Image 2 下半部)
                PriceLadderAndPlaybookSection(
                    record = uiState.currentRecord,
                    result = uiState.decisionResult
                )
            }

            item {
                // 7. 本地歷史記錄與回測驗證 (支援進行中/未過期過濾與最佳化排版)
                HistorySection(
                    records = uiState.displayRecords,
                    activeRecord = uiState.currentRecord,
                    showActiveOnly = uiState.showActiveOnly,
                    onToggleFilter = { viewModel.toggleActiveFilter(it) },
                    onLoadRecord = { viewModel.selectRecord(it) },
                    onDeleteRecord = { viewModel.deleteRecord(it) },
                    onClearAll = { viewModel.clearAllHistory() }
                )
            }

            item {
                // 8. 頁尾著作標記 (Image 3 底部)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "台股 IPO 競拍真實蜜月行情預判系統 v0.1.0 | Clean Architecture 決策引擎",
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
