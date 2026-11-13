package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSheet(
  item: ScreenshotEntity,
  onDismiss: () -> Unit,
  onDelete: (Long) -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("detail_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .navigationBarsPadding()
    ) {
      // Header with close button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = item.platform,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              color = if (item.isHighAccuracy) MintSuccess.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = if (item.isHighAccuracy) MintSuccess else Color.Gray,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${item.accuracyPercent}% Accuracy",
                  color = if (item.isHighAccuracy) MintSuccess else Color.Gray,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = item.postType,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_detail_sheet_button")
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close sheet")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Reconnect Action Banner
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Contact Reconnect Engine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Cross-reference public profiles & find contacts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.reconnectionQueries))
                context.startActivity(intent)
              } catch (e: Exception) {
                Toast.makeText(context, "Could not open search: ${e.message}", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reconnect_search_button")
          ) {
            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Launch Reconnection Search", fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Citation Provenance Card
      Text(
        text = "CITATION & PROVENANCE",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Timestamp:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = item.citationTimestamp,
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
              try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.citationSourceUrl))
                context.startActivity(intent)
              } catch (e: Exception) {
                clipboardManager.setText(AnnotatedString(item.citationSourceUrl))
                Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
              }
            }
          ) {
            Icon(
              imageVector = Icons.Default.OpenInBrowser,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = item.citationSourceUrl,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // People & Facial Landmarks
      Text(
        text = "PEOPLE & FACIAL CUES",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Face,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = item.peopleSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Handles & Mutual Friends
      Text(
        text = "HANDLES & MUTUAL FRIENDS",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.People,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Handles:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = item.extractedHandles,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = item.mutualFriends,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Landmarks & Background
      Text(
        text = "LANDMARKS & SCENE BACKGROUND",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = item.landmarksAndBackground,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Delete Button
      OutlinedButton(
        onClick = {
          onDelete(item.id)
          onDismiss()
        },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("delete_record_button")
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Delete Indexed Screenshot")
      }
    }
  }
}
