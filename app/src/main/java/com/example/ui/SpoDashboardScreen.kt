package com.example.ui

import androidx.compose.foundation.background
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
import com.example.ui.theme.TextLightHint

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
                // 1. 現增頂部標題、搜尋列與快速切換 Chip
                SpoHeaderAndSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.performSearch() },
                    allRecords = uiState.allRecords,
                    activeRecord = uiState.currentRecord,
                    onSelectRecord = { viewModel.selectRecord(it) },
                    isSyncing = uiState.isSyncing,
                    onSync = { viewModel.syncTwseData() }
                )
            }

            item {
                // 2. 核心現增決策 Banner（狀態橫幅大卡片：ARBITRAGE / SAFE / MODERATE / DANGER）
                SpoDecisionBanner(result = uiState.decisionResult)
            }

            item {
                // 3. 現增四大核心指標卡片（折價率、每筆毛利、融券鎖利、稀釋率）
                SpoMetricsGrid(
                    record = uiState.currentRecord,
                    result = uiState.decisionResult
                )
            }

            item {
                // 4. 即時現股市價動態試算輸入器
                SpoInteractivePriceAdjuster(
                    currentMarketPrice = uiState.currentRecord.marketPrice,
                    customPriceText = uiState.customMarketPriceText,
                    onPriceChanged = { viewModel.onMarketPriceChanged(it) }
                )
            }

            item {
                // 5. 撥券日現股走勢三段情境推演（保守/基準/樂觀）
                SpoScenariosSection(result = uiState.decisionResult)
            }

            item {
                // 6. 關鍵時程與實戰筆記
                SpoKeyDatesAndScheduleSection(record = uiState.currentRecord)
            }

            item {
                // 7. 頁尾著作標記
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "台股現金增資（SPO）抽籤套利決策系統 v0.1.0 | 雙軌架構模式",
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
