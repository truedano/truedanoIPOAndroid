package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LightAppBackground
import com.example.ui.theme.SearchButtonRed
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

enum class MainTab(val title: String, val subtitle: String) {
    IPO("🎯 IPO 競拍蜜月", "初次上市櫃 • 大戶底價"),
    SPO("💰 SPO 現增抽籤", "折價安全墊 • 融券鎖利")
}

@Composable
fun MainTabScreen(
    ipoViewModel: IpoDecisionViewModel,
    spoViewModel: SpoDecisionViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = MainTab.entries

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightAppBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // 頂部專業切換 TabRow
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = TextPrimaryDark,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = SearchButtonRed,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) SearchButtonRed else TextPrimaryDark,
                                fontSize = 14.sp
                            )
                            Text(
                                text = tab.subtitle,
                                color = if (isSelected) SearchButtonRed.copy(alpha = 0.8f) else TextSecondaryMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                )
            }
        }

        // 分頁主內容
        when (tabs[selectedTabIndex]) {
            MainTab.IPO -> {
                IpoDashboardScreen(
                    viewModel = ipoViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            MainTab.SPO -> {
                SpoDashboardScreen(
                    viewModel = spoViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
