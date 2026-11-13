package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AnalyticsScreen
import androidx.compose.runtime.mutableStateOf
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DetailSheet
import com.example.ui.screens.ScannerScreen
import com.example.ui.viewmodel.ScreenshotViewModel

enum class NavigationTab(val label: String) {
  SCANNER("Scan"),
  DASHBOARD("Dashboard"),
  ANALYTICS("Analytics")
}

@Composable
fun AppNavigation(viewModel: ScreenshotViewModel) {
  var selectedTab by rememberSaveable { mutableIntStateOf(0) }
  var isCameraOpen by rememberSaveable { mutableStateOf(false) }

  val records by viewModel.filteredRecords.collectAsStateWithLifecycle()
  val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
  val totalCount by viewModel.totalRecordCount.collectAsStateWithLifecycle()
  val filterState by viewModel.filterState.collectAsStateWithLifecycle()
  val scanState by viewModel.scanState.collectAsStateWithLifecycle()
  val metrics by viewModel.analyticsMetrics.collectAsStateWithLifecycle()
  val selectedDetailItem by viewModel.selectedDetailItem.collectAsStateWithLifecycle()

  // BackHandler: If camera is open, back closes camera; else if on non-home tab, back returns to Scanner tab
  BackHandler(enabled = isCameraOpen) {
    isCameraOpen = false
  }

  BackHandler(enabled = !isCameraOpen && selectedTab != 0) {
    selectedTab = 0
  }

  if (isCameraOpen) {
    CameraCaptureScreen(
      onImageCaptured = { uri ->
        viewModel.scanScreenshot(uri)
        isCameraOpen = false
      },
      onDismiss = { isCameraOpen = false }
    )
    return
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("app_bottom_nav_bar")
      ) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 0) Icons.Filled.DocumentScanner else Icons.Outlined.DocumentScanner,
              contentDescription = "Scan Screenshot"
            )
          },
          label = {
            Text(
              text = "Scan",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.testTag("nav_item_scan")
        )

        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 1) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
              contentDescription = "Dashboard Library"
            )
          },
          label = {
            Text(
              text = "Dashboard",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.testTag("nav_item_dashboard")
        )

        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 2) Icons.Filled.Assessment else Icons.Outlined.Assessment,
              contentDescription = "Analytics & Export"
            )
          },
          label = {
            Text(
              text = "Analytics",
              fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.testTag("nav_item_analytics")
        )
      }
    }
  ) { innerPadding ->
    when (selectedTab) {
      0 -> ScannerScreen(
        viewModel = viewModel,
        scanState = scanState,
        onLaunchCamera = { isCameraOpen = true },
        modifier = Modifier.padding(innerPadding)
      )
      1 -> DashboardScreen(
        viewModel = viewModel,
        searchResults = searchResults,
        totalCount = totalCount,
        filterState = filterState,
        onItemClick = { item -> viewModel.selectDetailItem(item) },
        modifier = Modifier.padding(innerPadding)
      )
      2 -> AnalyticsScreen(
        viewModel = viewModel,
        records = records,
        metrics = metrics,
        modifier = Modifier.padding(innerPadding)
      )
    }

    // Modal detail bottom sheet for inspecting citation and reconnection details
    selectedDetailItem?.let { item ->
      DetailSheet(
        item = item,
        onDismiss = { viewModel.selectDetailItem(null) },
        onDelete = { id -> viewModel.deleteRecord(id) }
      )
    }
  }
}
