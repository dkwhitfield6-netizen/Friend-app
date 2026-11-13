package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScreenshotEntity
import com.example.ui.theme.MintSuccess
import com.example.ui.viewmodel.ScreenshotViewModel
import com.example.util.DashboardFilterState
import com.example.util.SearchFilterEngine
import com.example.util.SearchMatchResult
import com.example.util.SearchScope
import com.example.util.SortOrder
import com.example.util.TimestampPreset

@Composable
fun DashboardScreen(
  viewModel: ScreenshotViewModel,
  searchResults: List<SearchMatchResult>,
  totalCount: Int,
  filterState: DashboardFilterState,
  onItemClick: (ScreenshotEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isFiltersSheetOpen by rememberSaveable { mutableStateOf(false) }
  var isDateRangePickerOpen by rememberSaveable { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    // Title, Result Count & Advanced Filters Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "INDEXED SCREENSHOTS",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 1.2.sp
        )
        Text(
          text = "Citation & Metadata Library",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(12.dp)
        ) {
          val countText = if (filterState.hasAnyActiveFilterOrQuery && totalCount > 0) {
            "${searchResults.size} / $totalCount"
          } else {
            "${searchResults.size} items"
          }
          Text(
            text = countText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("dashboard_result_count")
          )
        }

        // Advanced Filters Trigger with Active Count Badge
        Surface(
          color = if (filterState.activeFilterCount > 0) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .clickable { isFiltersSheetOpen = true }
            .testTag("open_advanced_filters_button")
        ) {
          BadgedBox(
            badge = {
              if (filterState.activeFilterCount > 0) {
                Badge(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                  Text("${filterState.activeFilterCount}")
                }
              }
            },
            modifier = Modifier.padding(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Advanced Filters",
              tint = if (filterState.activeFilterCount > 0) {
                MaterialTheme.colorScheme.onPrimaryContainer
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Scoped Search Input
    OutlinedTextField(
      value = filterState.searchQuery,
      onValueChange = { viewModel.onSearchQueryChange(it) },
      placeholder = {
        Text(
          text = filterState.searchScope.placeholder,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      },
      trailingIcon = {
        if (filterState.searchQuery.isNotEmpty()) {
          IconButton(
            onClick = { viewModel.onSearchQueryChange("") },
            modifier = Modifier.testTag("clear_search_button")
          ) {
            Icon(Icons.Default.Clear, contentDescription = "Clear search")
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("dashboard_search_input")
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Search Scope Chips: All, Name/Handle, Landmarks, Keywords, Platform
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(SearchScope.entries) { scope ->
        val isSelected = filterState.searchScope == scope
        val scopeIcon = when (scope) {
          SearchScope.ALL -> Icons.Default.Search
          SearchScope.NAME -> Icons.Default.PersonSearch
          SearchScope.LANDMARKS -> Icons.Default.LocationOn
          SearchScope.KEYWORDS -> Icons.Default.Tag
          SearchScope.PLATFORM -> Icons.Default.Language
        }
        Surface(
          color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .clickable { viewModel.onSearchScopeChange(scope) }
            .testTag("search_scope_${scope.name.lowercase()}")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = scopeIcon,
              contentDescription = null,
              tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = scope.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Quick Filter Strip: Timestamp Range + Multi-Select Social Media Platform Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        val isTimestampActive = filterState.timestampPreset != TimestampPreset.ALL_TIME
        val timestampLabel = if (filterState.timestampPreset == TimestampPreset.CUSTOM) {
          SearchFilterEngine.formatDateRangeLabel(
            filterState.customStartMillis,
            filterState.customEndMillis
          )
        } else {
          filterState.timestampPreset.label
        }
        FilterChip(
          selected = isTimestampActive,
          onClick = { isDateRangePickerOpen = true },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          },
          label = {
            Text(
              text = timestampLabel,
              fontWeight = if (isTimestampActive) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("quick_timestamp_chip")
        )
      }

      item {
        val isAllSelected = filterState.selectedPlatforms.isEmpty()
        FilterChip(
          selected = isAllSelected,
          onClick = { viewModel.onClearPlatforms() },
          label = { Text("All Platforms", fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("filter_chip_all")
        )
      }

      items(SearchFilterEngine.supportedPlatforms) { platform ->
        val isSelected = filterState.selectedPlatforms.any { it.equals(platform, ignoreCase = true) }
        FilterChip(
          selected = isSelected,
          onClick = { viewModel.onTogglePlatform(platform) },
          leadingIcon = if (isSelected) {
            {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            }
          } else null,
          label = { Text(platform, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("filter_chip_${platform.lowercase().replace(" ", "_")}")
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Sort Bar: Relevance, Date Added (Newest/Oldest toggle), Accuracy Score
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.FilterList,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Sort:",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
          SortChip(
            title = "Relevance",
            isSelected = filterState.sortOrder == SortOrder.RELEVANCE,
            onClick = { viewModel.onSortOrderChange(SortOrder.RELEVANCE) },
            testTag = "sort_chip_relevance"
          )
        }
        item {
          val isDateSelected = filterState.sortOrder == SortOrder.DATE_ADDED_NEWEST ||
            filterState.sortOrder == SortOrder.DATE_ADDED_OLDEST
          val dateTitle = if (filterState.sortOrder == SortOrder.DATE_ADDED_OLDEST) "Oldest" else "Date Added"
          SortChip(
            title = dateTitle,
            isSelected = isDateSelected,
            trailingIcon = if (isDateSelected) {
              if (filterState.sortOrder == SortOrder.DATE_ADDED_OLDEST) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
            } else null,
            onClick = {
              val next = if (filterState.sortOrder == SortOrder.DATE_ADDED_NEWEST) {
                SortOrder.DATE_ADDED_OLDEST
              } else {
                SortOrder.DATE_ADDED_NEWEST
              }
              viewModel.onSortOrderChange(next)
            },
            testTag = "sort_chip_date_added"
          )
        }
        item {
          SortChip(
            title = "Accuracy",
            isSelected = filterState.sortOrder == SortOrder.ACCURACY_SCORE,
            onClick = { viewModel.onSortOrderChange(SortOrder.ACCURACY_SCORE) },
            testTag = "sort_chip_accuracy"
          )
        }
      }
    }

    // Active Filter Summary Banner (Reset shortcut)
    AnimatedVisibility(visible = filterState.hasAnyActiveFilterOrQuery) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val summaryParts = buildList {
          if (filterState.searchQuery.isNotBlank()) add("\"${filterState.searchQuery}\" (${filterState.searchScope.label})")
          if (filterState.selectedPlatforms.isNotEmpty()) add(filterState.selectedPlatforms.joinToString(", "))
          if (filterState.timestampPreset != TimestampPreset.ALL_TIME) {
            add(
              if (filterState.timestampPreset == TimestampPreset.CUSTOM) {
                SearchFilterEngine.formatDateRangeLabel(filterState.customStartMillis, filterState.customEndMillis)
              } else {
                filterState.timestampPreset.label
              }
            )
          }
          if (filterState.minAccuracyPercent > 0) add("≥${filterState.minAccuracyPercent}%")
        }

        Text(
          text = "Active: ${summaryParts.joinToString(" • ")}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .clickable { viewModel.resetAllFilters() }
            .testTag("dashboard_reset_filters_chip")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.RestartAlt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "Reset",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Content List or Empty State
    if (searchResults.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No indexed screenshots found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Try broadening your search scope, date range, or platform filters",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (filterState.hasAnyActiveFilterOrQuery) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
              onClick = { viewModel.resetAllFilters() },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("empty_state_reset_filters_button")
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Clear All Filters")
            }
          }
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("dashboard_records_list")
      ) {
        items(searchResults, key = { it.entity.id }) { matchResult ->
          IndexedItemCard(
            matchResult = matchResult,
            onClick = { onItemClick(matchResult.entity) },
            onLaunchSearch = {
              try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(matchResult.entity.reconnectionQueries))
                context.startActivity(intent)
              } catch (e: Exception) {
                Toast.makeText(context, "Search opened", Toast.LENGTH_SHORT).show()
              }
            }
          )
        }
      }
    }
  }

  if (isFiltersSheetOpen) {
    AdvancedFiltersBottomSheet(
      filterState = filterState,
      resultCount = searchResults.size,
      onTogglePlatform = { viewModel.onTogglePlatform(it) },
      onClearPlatforms = { viewModel.onClearPlatforms() },
      onSelectTimestampPreset = { viewModel.onTimestampPresetSelected(it) },
      onOpenCustomDatePicker = {
        isFiltersSheetOpen = false
        isDateRangePickerOpen = true
      },
      onSelectMinAccuracy = { viewModel.onMinAccuracyChange(it) },
      onSelectSortOrder = { viewModel.onSortOrderChange(it) },
      onResetAll = { viewModel.resetAllFilters() },
      onDismiss = { isFiltersSheetOpen = false }
    )
  }

  if (isDateRangePickerOpen) {
    TimestampRangePickerDialog(
      initialStartMillis = filterState.customStartMillis,
      initialEndMillis = filterState.customEndMillis,
      onConfirm = { start, end -> viewModel.onCustomDateRangeSelected(start, end) },
      onDismiss = { isDateRangePickerOpen = false }
    )
  }
}

@Composable
private fun SortChip(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
  testTag: String
) {
  Surface(
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
      )
      if (trailingIcon != null) {
        Spacer(modifier = Modifier.width(3.dp))
        Icon(
          imageVector = trailingIcon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(12.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IndexedItemCard(
  matchResult: SearchMatchResult,
  onClick: () -> Unit,
  onLaunchSearch: () -> Unit
) {
  val item = matchResult.entity
  val extractedKeywords = remember(item.id, item.captionText, item.landmarksAndBackground) {
    SearchFilterEngine.extractImageKeywords(item).take(3)
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("screenshot_item_card_${item.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = item.platform,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = item.postType,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          color = if (item.isHighAccuracy) MintSuccess.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = if (item.isHighAccuracy) MintSuccess else Color.Gray,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "${item.accuracyPercent}%",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (item.isHighAccuracy) MintSuccess else Color.Gray
            )
          }
        }
      }

      if (matchResult.matchedFields.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = "Matched in: ${matchResult.matchedFields.joinToString(", ")}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = item.extractedHandles,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = item.mutualFriends,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.Top) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier
            .size(15.dp)
            .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = item.landmarksAndBackground,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      if (extractedKeywords.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          extractedKeywords.forEach { keyword ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = keyword,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = item.citationTimestamp.take(24),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.clickable { onLaunchSearch() }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Reconnect",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Inspect details",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
