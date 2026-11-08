package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainPickingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PickingViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val pickingViewModel: PickingViewModel = viewModel()
      val uiState by pickingViewModel.uiState.collectAsStateWithLifecycle()
      MyApplicationTheme(darkTheme = uiState.isDarkMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainPickingScreen(viewModel = pickingViewModel)
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

