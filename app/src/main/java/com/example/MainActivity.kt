package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.IpoDecisionViewModel
import com.example.ui.MainTabScreen
import com.example.ui.SpoDecisionViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val ipoViewModel: IpoDecisionViewModel by viewModels {
        val app = application as IpoApplication
        IpoDecisionViewModel.Factory(app.container.repository)
    }

    private val spoViewModel: SpoDecisionViewModel by viewModels {
        val app = application as IpoApplication
        SpoDecisionViewModel.Factory(app.container.spoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainTabScreen(
                    ipoViewModel = ipoViewModel,
                    spoViewModel = spoViewModel
                )
            }
        }
    }
}
