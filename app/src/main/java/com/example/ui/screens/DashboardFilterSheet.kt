package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.DashboardFilterState
import com.example.util.SearchFilterEngine
import com.example.util.SortOrder
import com.example.util.TimestampPreset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvancedFiltersBottomSheet(
  filterState: DashboardFilterState,
  resultCount: Int,
  onTogglePlatform: (String) -> Unit,
  onClearPlatforms: () -> Unit,
  onSelectTimestampPreset: (TimestampPreset) -> Unit,
  onOpenCustomDatePicker: () -> Unit,
  onSelectMinAccuracy: (Int) -> Unit,
  onSelectSortOrder: (SortOrder) -> Unit,
  onResetAll: () -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("advanced_filters_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp)
        .navigationBarsPadding()
    ) {
      // Sheet Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "ADVANCED FILTERS & SORT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
          )
          Text(
            text = "Refine Search Results",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_filters_sheet_button")
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close filters")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 1. Timestamp Range
      Text(
        text = "TIMESTAMP RANGE",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TimestampPreset.entries.forEach { preset ->
          val isSelected = filterState.timestampPreset == preset
          val label = if (preset == TimestampPreset.CUSTOM && isSelected) {
            SearchFilterEngine.formatDateRangeLabel(
              filterState.customStartMillis,
              filterState.customEndMillis
            )
          } else {
            preset.label
          }
          FilterChip(
            selected = isSelected,
            onClick = {
              if (preset == TimestampPreset.CUSTOM) {
                onOpenCustomDatePicker()
              } else {
                onSelectTimestampPreset(preset)
              }
            },
            leadingIcon = if (preset == TimestampPreset.CUSTOM) {
              {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
              }
            } else null,
            label = {
              Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("timestamp_preset_${preset.name.lowercase()}")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
      Spacer(modifier = Modifier.height(16.dp))

      // 2. Social Media Platforms (Multi-select)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SOCIAL MEDIA PLATFORMS",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 1.sp
        )
        if (filterState.selectedPlatforms.isNotEmpty()) {
          TextButton(onClick = onClearPlatforms) {
            Text("Select All", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
      Spacer(modifier = Modifier.height(6.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = filterState.selectedPlatforms.isEmpty(),
          onClick = onClearPlatforms,
          label = {
            Text(
              text = "All Platforms",
              fontWeight = if (filterState.selectedPlatforms.isEmpty()) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("sheet_platform_all")
        )

        SearchFilterEngine.supportedPlatforms.forEach { platform ->
          val isSelected = filterState.selectedPlatforms.any { it.equals(platform, ignoreCase = true) }
          FilterChip(
            selected = isSelected,
            onClick = { onTogglePlatform(platform) },
            leadingIcon = if (isSelected) {
              {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
              }
            } else null,
            label = {
              Text(
                text = platform,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("sheet_platform_${platform.lowercase().replace(" ", "_")}")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
      Spacer(modifier = Modifier.height(16.dp))

      // 3. Minimum Accuracy Score
      Text(
        text = "MINIMUM ACCURACY SCORE",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      val accuracyOptions = listOf(0 to "Any Score", 85 to "85%+ (High)", 90 to "90%+", 95 to "95%+")
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        accuracyOptions.forEach { (threshold, label) ->
          val isSelected = filterState.minAccuracyPercent == threshold
          FilterChip(
            selected = isSelected,
            onClick = { onSelectMinAccuracy(threshold) },
            label = {
              Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("accuracy_filter_$threshold")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
      Spacer(modifier = Modifier.height(16.dp))

      // 4. Sort Results By
      Text(
        text = "SORT RESULTS BY",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SortOrder.entries.forEach { order ->
          val isSelected = filterState.sortOrder == order
          FilterChip(
            selected = isSelected,
            onClick = { onSelectSortOrder(order) },
            label = {
              Text(
                text = order.label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("sheet_sort_${order.name.lowercase()}")
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Bottom Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = onResetAll,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("sheet_reset_filters_button")
        ) {
          Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Reset All")
        }

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1.4f)
            .height(48.dp)
            .testTag("sheet_apply_filters_button")
        ) {
          Text("Show $resultCount Results", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimestampRangePickerDialog(
  initialStartMillis: Long?,
  initialEndMillis: Long?,
  onConfirm: (Long?, Long?) -> Unit,
  onDismiss: () -> Unit
) {
  val dateRangePickerState = rememberDateRangePickerState(
    initialSelectedStartDateMillis = initialStartMillis,
    initialSelectedEndDateMillis = initialEndMillis
  )

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm(
            dateRangePickerState.selectedStartDateMillis,
            dateRangePickerState.selectedEndDateMillis
          )
          onDismiss()
        },
        modifier = Modifier.testTag("confirm_date_range_button")
      ) {
        Text("Apply Range", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_date_range_button")
      ) {
        Text("Cancel")
      }
    },
    modifier = Modifier.testTag("timestamp_range_picker_dialog")
  ) {
    DateRangePicker(
      state = dateRangePickerState,
      title = {
        Text(
          text = "Select Timestamp Range",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 24.dp, top = 16.dp)
        )
      },
      headline = {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
          Text(
            text = SearchFilterEngine.formatDateRangeLabel(
              dateRangePickerState.selectedStartDateMillis,
              dateRangePickerState.selectedEndDateMillis
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      },
      showModeToggle = false,
      modifier = Modifier
        .fillMaxWidth()
        .height(440.dp)
    )
  }
}
