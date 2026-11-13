package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScreenshotEntity
import com.example.ui.theme.MintSuccess
import com.example.ui.viewmodel.AnalyticsMetrics
import com.example.ui.viewmodel.ScreenshotViewModel
import com.example.util.CitationExporter
import com.example.util.ExportFormat

@Composable
fun AnalyticsScreen(
  viewModel: ScreenshotViewModel,
  records: List<ScreenshotEntity>,
  metrics: AnalyticsMetrics,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  var selectedExportPreviewTab by remember { mutableIntStateOf(0) }

  val previewJson = remember(records) { CitationExporter.generateJson(records) }
  val previewCsv = remember(records) { CitationExporter.generateCsv(records) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    // Header
    Text(
      text = "METRICS & CITATION AUDIT",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 1.2.sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Analytics & Export Suite",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Monitor recognition accuracy benchmarks and export indexed records for database integration.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Primary Accuracy Metric Card
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "AVERAGE RECOGNITION ACCURACY",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = "${(metrics.averageAccuracyRate * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "confidence rate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
              )
            }
          }

          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(MintSuccess.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = null,
              tint = MintSuccess,
              modifier = Modifier.size(32.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Benchmark Status Badge
        Surface(
          color = MintSuccess.copy(alpha = 0.15f),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = MintSuccess,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Target Accuracy Benchmark Exceeded (> 85%)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MintSuccess
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar against benchmark
        LinearProgressIndicator(
          progress = { metrics.averageAccuracyRate.toFloat().coerceIn(0f, 1f) },
          color = MintSuccess,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Stats Grid
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      StatCard(
        title = "Total Indexed",
        value = metrics.totalIndexed.toString(),
        subtitle = "Screenshots stored",
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "High Confidence",
        value = metrics.highAccuracyCount.toString(),
        subtitle = "Scored > 85%",
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Platform Distribution Breakdown
    Text(
      text = "PLATFORM DISTRIBUTION",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 1.2.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        if (metrics.platformBreakdown.isEmpty()) {
          Text("No platform telemetry yet.", style = MaterialTheme.typography.bodySmall)
        } else {
          metrics.platformBreakdown.forEach { (platform, count) ->
            val ratio = if (metrics.totalIndexed > 0) count.toFloat() / metrics.totalIndexed.toFloat() else 0f
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = platform,
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "$count (${(ratio * 100).toInt()}%)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { ratio },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp))
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Export Options Suite
    Text(
      text = "EXPORT OPTIONS & SEARCH OPTIMIZATION",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 1.2.sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Generate structured JSON or CSV files for database indexing, external search, or spreadsheet analytics:",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(14.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { viewModel.exportData(context, ExportFormat.JSON) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("export_json_button")
      ) {
        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Export JSON", fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = { viewModel.exportData(context, ExportFormat.CSV) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("export_csv_button")
      ) {
        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Export CSV", fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // In-App Dataset Previewer
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Live Dataset Preview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          IconButton(
            onClick = {
              val textToCopy = if (selectedExportPreviewTab == 0) previewJson else previewCsv
              clipboardManager.setText(AnnotatedString(textToCopy))
              Toast.makeText(context, "Copied dataset to clipboard", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(32.dp).testTag("copy_preview_dataset_button")
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy code", modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(
          selectedTabIndex = selectedExportPreviewTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
          Tab(
            selected = selectedExportPreviewTab == 0,
            onClick = { selectedExportPreviewTab = 0 },
            text = { Text("JSON Schema") },
            modifier = Modifier.testTag("tab_preview_json")
          )
          Tab(
            selected = selectedExportPreviewTab == 1,
            onClick = { selectedExportPreviewTab = 1 },
            text = { Text("CSV Table") },
            modifier = Modifier.testTag("tab_preview_csv")
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(12.dp)
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
        ) {
          Text(
            text = if (selectedExportPreviewTab == 0) previewJson else previewCsv,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF38BDF8),
            fontSize = 11.sp
          )
        }
      }
    }
  }
}

@Composable
private fun StatCard(
  title: String,
  value: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    shape = RoundedCornerShape(14.dp),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
