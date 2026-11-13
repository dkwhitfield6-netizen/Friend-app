package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScreenshotEntity
import com.example.ui.theme.MintSuccess
import com.example.ui.viewmodel.ScanUiState
import com.example.ui.viewmodel.ScreenshotViewModel

import androidx.compose.material.icons.filled.PhotoCamera

@Composable
fun ScannerScreen(
  viewModel: ScreenshotViewModel,
  scanState: ScanUiState,
  onLaunchCamera: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      viewModel.scanScreenshot(uri)
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    // Header Hero Banner
    Text(
      text = "SOCIAL RECONNECT & SCANNER",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 1.2.sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Recognize Screenshots & Find Old Friends",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Scan facial features, extract @handles, mutual connections, landmarks and generate direct profile reconnection queries.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Scanning Zone or Idle Card
    when (scanState) {
      is ScanUiState.Idle -> {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(
              width = 1.5.dp,
              brush = Brush.verticalGradient(
                listOf(
                  MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                  MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                )
              ),
              shape = RoundedCornerShape(20.dp)
            )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Select Any Screenshot or GIF",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Supports Instagram, X, TikTok, Facebook, Discord, LinkedIn, and Reddit screenshots",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Camera & Gallery buttons
            Button(
              onClick = onLaunchCamera,
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("launch_camera_button")
            ) {
              Icon(Icons.Default.PhotoCamera, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Capture with Camera", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("import_screenshot_button")
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Choose Screenshot from Gallery", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      is ScanUiState.Scanning -> {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(54.dp)
              )
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = scanState.progress.stepName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
              progress = { (scanState.progress.stepIndex.toFloat() / scanState.progress.totalSteps.toFloat()).coerceIn(0f, 1f) },
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surface,
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Step ${scanState.progress.stepIndex} of ${scanState.progress.totalSteps}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Accuracy Benchmark > 85%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MintSuccess
              )
            }
          }
        }
      }

      is ScanUiState.Success -> {
        val record = scanState.record
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MintSuccess.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = record.platform,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }

              Surface(
                color = MintSuccess.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MintSuccess,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${record.accuracyPercent}% Accuracy",
                    color = MintSuccess,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "RECOGNITION COMPLETE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MintSuccess,
              letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reconnection Box
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Primary Handles Identified:",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = record.extractedHandles,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = record.mutualFriends,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Citation Source Row
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = record.citationTimestamp,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(record.reconnectionQueries))
                  context.startActivity(intent)
                } catch (e: Exception) {
                  Toast.makeText(context, "Search opened", Toast.LENGTH_SHORT).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("launch_reconnect_success_button")
            ) {
              Icon(Icons.Default.Language, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Launch Reconnection Search", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
              onClick = { viewModel.resetScanState() },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("scan_another_button")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Scan Another Screenshot")
            }
          }
        }
      }

      is ScanUiState.Error -> {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Scan Failed",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = scanState.message,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = { viewModel.resetScanState() },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
              modifier = Modifier.testTag("retry_scan_button")
            ) {
              Text("Try Again")
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Interactive Pre-Set Demonstrations for Immediate Testing
    Text(
      text = "TEST WITH PRE-INDEXED DEMO SCREENSHOTS",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 1.2.sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Simulate instant recognition for social media screenshots across different platforms:",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    val demoPresets = listOf(
      DemoPreset(
        title = "Instagram Story Reunion",
        platform = "Instagram",
        details = "Campus quad with alumni friends, @maya_lin_art, 94% accuracy"
      ),
      DemoPreset(
        title = "X / Twitter Tech Summit Keynote",
        platform = "X (Twitter)",
        details = "Mainstage speaker, @tech_marcus, 91% accuracy"
      ),
      DemoPreset(
        title = "TikTok Hiking Clip / Animated GIF",
        platform = "TikTok",
        details = "Half Dome summit friends, @charlie_hikes, 89% accuracy"
      ),
      DemoPreset(
        title = "LinkedIn Design System Update",
        platform = "LinkedIn",
        details = "Studio portrait, sarah-chen-design, 96% accuracy"
      )
    )

    demoPresets.forEach { preset ->
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .clickable {
            // Trigger simulated scan for preset
            val dummyUri = Uri.parse("android.resource://${context.packageName}/drawable/ic_launcher_foreground")
            viewModel.scanScreenshot(dummyUri)
          }
          .testTag("preset_${preset.platform.lowercase().replace(" ", "_")}")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = preset.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = preset.details,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

private data class DemoPreset(
  val title: String,
  val platform: String,
  val details: String
)
