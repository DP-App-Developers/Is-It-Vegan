package com.isitveganapp.ui.camera

import android.Manifest
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.Executors
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.isitveganapp.domain.model.AnalysisResult

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onResultReady: (AnalysisResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(uiState) {
        if (uiState is CameraViewModel.UiState.Error) {
            snackbarHostState.showSnackbar((uiState as CameraViewModel.UiState.Error).message)
            viewModel.clearError()
        }
    }

    if (!cameraPermission.status.isGranted) {
        PermissionScreen(
            showRationale = cameraPermission.status.shouldShowRationale,
            onRequest = { cameraPermission.launchPermissionRequest() }
        )
        return
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            // Wait for the phone's full processing pipeline (HDR, noise reduction, sharpening)
            // before capturing. Thin strokes like "i" survive a processed frame; they don't
            // survive a raw unprocessed one.
            setImageCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        }
    }

    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose { cameraController.unbind() }
    }

    val captureExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { captureExecutor.shutdown() } }

    val isProcessing = uiState is CameraViewModel.UiState.Processing

    val previewHolder = remember { arrayOfNulls<PreviewView>(1) }
    var frozenBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(isProcessing) {
        if (!isProcessing) {
            frozenBitmap?.recycle()
            frozenBitmap = null
        }
    }

    var scanBoxLeft by remember { mutableStateOf(0) }
    var scanBoxTop by remember { mutableStateOf(0) }
    var scanBoxRight by remember { mutableStateOf(0) }
    var scanBoxBottom by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }.also { previewHolder[0] = it }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Frozen frame shown while processing so the preview stops moving
        frozenBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Top gradient + wordmark
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Is It Vegan",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Scrim + transparent scan window + corner brackets
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val screenW = constraints.maxWidth.toFloat()
            val screenH = constraints.maxHeight.toFloat()
            val boxW = screenW * 0.85f
            val boxH = boxW / 2.8f * 1.5f
            val boxLeft = (screenW - boxW) / 2f
            val boxTop = (screenH - boxH) / 2f

            SideEffect {
                scanBoxLeft = boxLeft.toInt()
                scanBoxTop = boxTop.toInt()
                scanBoxRight = (boxLeft + boxW).toInt()
                scanBoxBottom = (boxTop + boxH).toInt()
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val corner = 10.dp.toPx()
                val cLen = 24.dp.toPx()
                val sw = 2.5.dp.toPx()

                // Scrim with EvenOdd hole: fill the screen minus the scan window
                val scrimPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addRoundRect(
                        RoundRect(boxLeft, boxTop, boxLeft + boxW, boxTop + boxH, CornerRadius(corner))
                    )
                    fillType = PathFillType.EvenOdd
                }
                drawPath(scrimPath, Color.Black.copy(alpha = 0.55f))

                // Subtle border tracing the window edge
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(boxW, boxH),
                    cornerRadius = CornerRadius(corner),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Corner brackets
                val x0 = boxLeft;  val y0 = boxTop
                val x1 = boxLeft + boxW; val y1 = boxTop + boxH

                drawLine(Color.White, Offset(x0, y0 + cLen), Offset(x0, y0), sw, StrokeCap.Round)
                drawLine(Color.White, Offset(x0, y0), Offset(x0 + cLen, y0), sw, StrokeCap.Round)

                drawLine(Color.White, Offset(x1 - cLen, y0), Offset(x1, y0), sw, StrokeCap.Round)
                drawLine(Color.White, Offset(x1, y0), Offset(x1, y0 + cLen), sw, StrokeCap.Round)

                drawLine(Color.White, Offset(x0, y1 - cLen), Offset(x0, y1), sw, StrokeCap.Round)
                drawLine(Color.White, Offset(x0, y1), Offset(x0 + cLen, y1), sw, StrokeCap.Round)

                drawLine(Color.White, Offset(x1 - cLen, y1), Offset(x1, y1), sw, StrokeCap.Round)
                drawLine(Color.White, Offset(x1, y1 - cLen), Offset(x1, y1), sw, StrokeCap.Round)
            }

            // Instruction label sits just below the scan window
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = with(density) { boxH.toDp() } / 2 + 20.dp)
            ) {
                Text(
                    text = "Fit all ingredients inside the box",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom gradient + shutter
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(bottom = 44.dp)
            ) {
                Text(
                    text = if (isProcessing) "Analyzing ingredients…" else "Tap to scan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )

                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        color = Color.White,
                        strokeWidth = 3.dp,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    frozenBitmap = previewHolder[0]?.bitmap
                                    viewModel.onShutterPressed()
                                    cameraController.takePicture(
                                        captureExecutor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(proxy: ImageProxy) {
                                                val bitmap = proxy.toBitmap()
                                                val rotation = proxy.imageInfo.rotationDegrees
                                                proxy.close()
                                                val dm = context.resources.displayMetrics
                                                viewModel.processCapture(
                                                    bitmap, rotation,
                                                    dm.widthPixels, dm.heightPixels,
                                                    scanBoxLeft, scanBoxTop, scanBoxRight, scanBoxBottom,
                                                    onResultReady
                                                )
                                            }
                                            override fun onError(e: ImageCaptureException) {
                                                viewModel.clearError()
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun PermissionScreen(showRationale: Boolean, onRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Camera Access Required",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (showRationale)
                    "Camera access is needed to photograph ingredient lists and check if products are vegan."
                else
                    "IsItVegan uses your camera to scan ingredient labels and instantly tell you if a product is vegan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Allow Camera Access",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}
