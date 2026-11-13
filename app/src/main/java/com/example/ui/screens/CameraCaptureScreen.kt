package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File

private const val TAG = "CameraCaptureScreen"

@Composable
fun CameraCaptureScreen(
  onImageCaptured: (Uri) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
    if (!isGranted) {
      Toast.makeText(context, "Camera permission is required to capture screenshots", Toast.LENGTH_SHORT).show()
    }
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  if (hasCameraPermission) {
    CameraViewfinder(
      onImageCaptured = onImageCaptured,
      onDismiss = onDismiss
    )
  } else {
    CameraPermissionRationale(
      onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
      onDismiss = onDismiss
    )
  }
}

@Composable
private fun CameraViewfinder(
  onImageCaptured: (Uri) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
  var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
  var isCapturing by remember { mutableStateOf(false) }

  val imageCapture = remember {
    ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
      .setFlashMode(flashMode)
      .build()
  }

  val previewView = remember {
    PreviewView(context).apply {
      implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }
  }

  LaunchedEffect(lensFacing, flashMode) {
    imageCapture.flashMode = flashMode
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
      try {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
          it.surfaceProvider = previewView.surfaceProvider
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
          lifecycleOwner,
          lensFacing,
          preview,
          imageCapture
        )
      } catch (e: Exception) {
        Log.e(TAG, "Failed to bind CameraX lifecycle", e)
      }
    }, ContextCompat.getMainExecutor(context))
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // Camera Preview Surface
    AndroidView(
      factory = { previewView },
      modifier = Modifier.fillMaxSize()
    )

    // Viewfinder Reticle / Screenshot Framing Guidelines
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 96.dp)
        .border(
          width = 2.dp,
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
          shape = RoundedCornerShape(24.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Text(
          text = "FRAME SOCIAL SCREENSHOT OR FACE",
          color = MaterialTheme.colorScheme.primary,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Text(
          text = "Auto-align post, @handles & facial cues",
          color = Color.White.copy(alpha = 0.85f),
          style = MaterialTheme.typography.bodySmall
        )
      }
    }

    // Top Controls Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onDismiss,
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.5f))
          .testTag("camera_close_button")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close Camera",
          tint = Color.White
        )
      }

      Surface(
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(20.dp)
      ) {
        Text(
          text = "ReConnect Lens",
          color = Color.White,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Flash toggle
        IconButton(
          onClick = {
            flashMode = when (flashMode) {
              ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
              ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
              else -> ImageCapture.FLASH_MODE_AUTO
            }
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .testTag("camera_flash_toggle")
        ) {
          Icon(
            imageVector = when (flashMode) {
              ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
              ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
              else -> Icons.Default.FlashAuto
            },
            contentDescription = "Toggle Flash",
            tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) MaterialTheme.colorScheme.primary else Color.White
          )
        }

        // Camera flip toggle
        IconButton(
          onClick = {
            lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
              CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
              CameraSelector.DEFAULT_BACK_CAMERA
            }
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .testTag("camera_flip_lens")
        ) {
          Icon(
            imageVector = Icons.Default.Cameraswitch,
            contentDescription = "Switch Camera Lens",
            tint = Color.White
          )
        }
      }
    }

    // Bottom Controls & Shutter Bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (isCapturing) {
        CircularProgressIndicator(
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .size(64.dp)
            .padding(8.dp)
        )
      } else {
        // Main Capture Shutter Button
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.3f))
            .clickable {
              isCapturing = true
              val photoFile = try {
                File.createTempFile("screenshot_capture_", ".jpg", context.cacheDir)
              } catch (e: Exception) {
                isCapturing = false
                Toast.makeText(context, "Could not create image file", Toast.LENGTH_SHORT).show()
                return@clickable
              }

              val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
              imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                  override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    isCapturing = false
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                  }

                  override fun onError(exception: ImageCaptureException) {
                    isCapturing = false
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(context, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                  }
                }
              )
            }
            .padding(6.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
              .testTag("camera_shutter_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = "Take Photo",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(32.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Tap to capture & process",
        color = Color.White.copy(alpha = 0.8f),
        style = MaterialTheme.typography.labelSmall
      )
    }
  }
}

@Composable
private fun CameraPermissionRationale(
  onRequestPermission: () -> Unit,
  onDismiss: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Camera,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Camera Permission Needed",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "ReConnect uses the camera to capture screenshots or screen displays directly for facial recognition, landmark analysis, and citation extraction.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = onRequestPermission,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("request_camera_permission_button")
      ) {
        Text("Allow Camera Access", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(8.dp))

      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.textButtonColors(),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Cancel")
      }
    }
  }
}
