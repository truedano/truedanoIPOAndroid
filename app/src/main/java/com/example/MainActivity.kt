package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.ui.IpoDecisionViewModel
import com.example.ui.MainTabScreen
import com.example.ui.SpoDecisionViewModel
import com.example.ui.components.AppUpdateDialog
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val app by lazy { application as IpoApplication }

    private val ipoViewModel: IpoDecisionViewModel by viewModels {
        IpoDecisionViewModel.Factory(app.container.repository)
    }

    private val spoViewModel: SpoDecisionViewModel by viewModels {
        SpoDecisionViewModel.Factory(app.container.spoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 啟動時非阻塞式檢查 GitHub Releases 是否有新版本
        lifecycleScope.launch {
            app.container.updateManager.checkForUpdates()
        }

        setContent {
            MyApplicationTheme {
                val updateState by app.container.updateManager.uiState.collectAsState()

                MainTabScreen(
                    ipoViewModel = ipoViewModel,
                    spoViewModel = spoViewModel,
                    onCheckForUpdate = {
                        lifecycleScope.launch {
                            app.container.updateManager.checkForUpdates(isManual = true)
                        }
                    }
                )

                AppUpdateDialog(
                    state = updateState,
                    onStartDownload = { apkUrl ->
                        lifecycleScope.launch {
                            app.container.updateManager.downloadApk(apkUrl)
                        }
                    },
                    onInstall = { apkFile ->
                        app.container.updateManager.installApk(apkFile)
                    },
                    onDismiss = {
                        app.container.updateManager.dismissUpdate()
                    }
                )
            }
        }
    }
}
