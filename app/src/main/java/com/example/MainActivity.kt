package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AuraFloatingBottomNav
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.reader.ReaderScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tools.ToolsScreen
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraEmerald
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraRose
import com.example.ui.theme.AuraTheme
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.PdfViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

  private val viewModel: PdfViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val systemDark = isSystemInDarkTheme()
      var isDarkTheme by remember { mutableStateOf(systemDark) }

      AuraTheme(darkTheme = isDarkTheme) {
        val activeDocument by viewModel.activeDocument.collectAsState()
        val currentTab by viewModel.currentTab.collectAsState()
        val notification by viewModel.notification.collectAsState()

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
          if (activeDocument != null) {
            ReaderScreen(
              viewModel = viewModel,
              document = activeDocument!!
            )
          } else {
            Scaffold(
              modifier = Modifier.fillMaxSize(),
              containerColor = MaterialTheme.colorScheme.background,
              bottomBar = {
                AuraFloatingBottomNav(
                  currentTab = currentTab,
                  onTabSelected = { tab -> viewModel.setTab(tab) }
                )
              }
            ) { paddingValues ->
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(paddingValues)
              ) {
                Crossfade(
                  targetState = currentTab,
                  label = "tab_crossfade"
                ) { tab ->
                  when (tab) {
                    NavigationTab.HOME -> HomeScreen(viewModel = viewModel)
                    NavigationTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                    NavigationTab.TOOLS -> ToolsScreen(viewModel = viewModel)
                    NavigationTab.SETTINGS -> SettingsScreen(
                      viewModel = viewModel,
                      currentThemeDark = isDarkTheme,
                      onToggleTheme = { isDarkTheme = it }
                    )
                  }
                }
              }
            }
          }

          // Top floating notification toast
          AnimatedVisibility(
            visible = notification != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
          ) {
            notification?.let { notif ->
              LaunchedEffect(notif.id) {
                delay(3200)
                viewModel.dismissNotification()
              }

              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (notif.isError) AuraRose else Color(0xEE0F172A),
                shadowElevation = 8.dp,
                modifier = Modifier
                  .statusBarsPadding()
                  .padding(horizontal = 24.dp, vertical = 12.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .clickable { viewModel.dismissNotification() }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = if (notif.isError) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (notif.isError) Color.White else AuraEmerald,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = notif.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
