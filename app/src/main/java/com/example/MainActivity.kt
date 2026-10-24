package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.IpoDashboardScreen
import com.example.ui.IpoDecisionViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: IpoDecisionViewModel by viewModels {
        val app = application as IpoApplication
        IpoDecisionViewModel.Factory(app.container.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                IpoDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
