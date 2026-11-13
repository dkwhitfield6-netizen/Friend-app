package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ScreenshotEntity
import com.example.data.repository.ScreenshotRepository
import com.example.service.RecognitionEngine
import com.example.service.ScanProgressState
import com.example.util.CitationExporter
import com.example.util.DashboardFilterState
import com.example.util.ExportFormat
import com.example.util.SearchFilterEngine
import com.example.util.SearchMatchResult
import com.example.util.SearchScope
import com.example.util.SortOrder
import com.example.util.TimestampPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScanUiState {
  data object Idle : ScanUiState
  data class Scanning(val progress: ScanProgressState) : ScanUiState
  data class Success(val record: ScreenshotEntity) : ScanUiState
  data class Error(val message: String) : ScanUiState
}

data class AnalyticsMetrics(
  val totalIndexed: Int = 0,
  val averageAccuracyRate: Double = 0.0,
  val highAccuracyCount: Int = 0,
  val platformBreakdown: Map<String, Int> = emptyMap()
)

class ScreenshotViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: ScreenshotRepository
  private val recognitionEngine: RecognitionEngine

  private val _filterState = MutableStateFlow(DashboardFilterState())
  val filterState: StateFlow<DashboardFilterState> = _filterState.asStateFlow()

  private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
  val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

  private val _selectedDetailItem = MutableStateFlow<ScreenshotEntity?>(null)
  val selectedDetailItem: StateFlow<ScreenshotEntity?> = _selectedDetailItem.asStateFlow()

  init {
    val db = AppDatabase.getDatabase(application)
    repository = ScreenshotRepository(db.screenshotDao())
    recognitionEngine = RecognitionEngine(application)

    viewModelScope.launch {
      // Seed starter indexed screenshots if db is fresh
      val currentCount = repository.count.first()
      if (currentCount == 0) {
        repository.seedInitialDataIfEmpty()
      }
    }
  }

  val totalRecordCount: StateFlow<Int> = repository.count.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = 0
  )

  val searchResults: StateFlow<List<SearchMatchResult>> = combine(
    repository.allScreenshots,
    _filterState
  ) { list, state ->
    SearchFilterEngine.filterAndSort(list, state)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val filteredRecords: StateFlow<List<ScreenshotEntity>> = searchResults
    .map { results -> results.map { it.entity } }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val analyticsMetrics: StateFlow<AnalyticsMetrics> = repository.allScreenshots.map { list ->
    if (list.isEmpty()) {
      AnalyticsMetrics()
    } else {
      val avg = list.map { it.confidenceScore }.average()
      val highCount = list.count { it.isHighAccuracy }
      val breakdown = list.groupBy { it.platform }.mapValues { it.value.size }
      AnalyticsMetrics(
        totalIndexed = list.size,
        averageAccuracyRate = (avg * 100.0).toInt() / 100.0,
        highAccuracyCount = highCount,
        platformBreakdown = breakdown
      )
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = AnalyticsMetrics()
  )

  fun onSearchQueryChange(query: String) {
    _filterState.update { current ->
      val nextSort = if (current.searchQuery.isBlank() && query.isNotBlank() && current.sortOrder == SortOrder.DATE_ADDED_NEWEST) {
        SortOrder.RELEVANCE
      } else if (query.isBlank() && current.sortOrder == SortOrder.RELEVANCE) {
        SortOrder.DATE_ADDED_NEWEST
      } else {
        current.sortOrder
      }
      current.copy(searchQuery = query, sortOrder = nextSort)
    }
  }

  fun onSearchScopeChange(scope: SearchScope) {
    _filterState.update { it.copy(searchScope = scope) }
  }

  fun onTogglePlatform(platform: String) {
    _filterState.update { current ->
      if (platform.equals("All", ignoreCase = true)) {
        current.copy(selectedPlatforms = emptySet())
      } else {
        val next = current.selectedPlatforms.toMutableSet()
        if (next.contains(platform)) {
          next.remove(platform)
        } else {
          next.add(platform)
        }
        current.copy(selectedPlatforms = next)
      }
    }
  }

  fun onClearPlatforms() {
    _filterState.update { it.copy(selectedPlatforms = emptySet()) }
  }

  fun onTimestampPresetSelected(preset: TimestampPreset) {
    _filterState.update {
      if (preset != TimestampPreset.CUSTOM) {
        it.copy(timestampPreset = preset, customStartMillis = null, customEndMillis = null)
      } else {
        it.copy(timestampPreset = preset)
      }
    }
  }

  fun onCustomDateRangeSelected(startMillis: Long?, endMillis: Long?) {
    _filterState.update {
      it.copy(
        timestampPreset = if (startMillis != null || endMillis != null) TimestampPreset.CUSTOM else TimestampPreset.ALL_TIME,
        customStartMillis = startMillis,
        customEndMillis = endMillis
      )
    }
  }

  fun onMinAccuracyChange(minPercent: Int) {
    _filterState.update { it.copy(minAccuracyPercent = minPercent) }
  }

  fun onSortOrderChange(order: SortOrder) {
    _filterState.update { it.copy(sortOrder = order) }
  }

  fun resetAllFilters() {
    _filterState.value = DashboardFilterState()
  }

  fun selectDetailItem(item: ScreenshotEntity?) {
    _selectedDetailItem.value = item
  }

  fun resetScanState() {
    _scanState.value = ScanUiState.Idle
  }

  fun scanScreenshot(uri: Uri) {
    viewModelScope.launch {
      _scanState.value = ScanUiState.Scanning(
        ScanProgressState("Initializing image analysis...", 0, 5, 0.05)
      )
      try {
        val result = recognitionEngine.analyzeScreenshot(uri) { progress ->
          _scanState.value = ScanUiState.Scanning(progress)
        }
        val insertedId = repository.insert(result)
        val savedEntity = result.copy(id = insertedId)
        _scanState.value = ScanUiState.Success(savedEntity)
      } catch (e: Exception) {
        _scanState.value = ScanUiState.Error(e.localizedMessage ?: "Failed to analyze screenshot.")
      }
    }
  }

  fun deleteRecord(id: Long) {
    viewModelScope.launch {
      repository.deleteById(id)
      if (_selectedDetailItem.value?.id == id) {
        _selectedDetailItem.value = null
      }
    }
  }

  fun exportData(context: Context, format: ExportFormat) {
    val items = filteredRecords.value
    CitationExporter.shareExportData(context, format, items)
  }
}
