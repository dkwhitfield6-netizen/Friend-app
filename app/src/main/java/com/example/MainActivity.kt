package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.components.AppNavigation
import com.example.ui.theme.ReConnectTheme
import com.example.ui.viewmodel.ScreenshotViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: ScreenshotViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      ReConnectTheme {
        AppNavigation(viewModel = viewModel)
      }
    }
  }
}
