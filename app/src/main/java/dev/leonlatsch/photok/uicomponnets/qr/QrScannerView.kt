/*
 *   Copyright 2020-2026 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.uicomponnets.qr

import android.Manifest
import android.util.Range
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import dev.leonlatsch.photok.ui.components.PermissionGate
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * Composable camera preview that decodes QR codes using CameraX + ZXing.
 *
 * Domain-agnostic: knows nothing about recovery phrases or any other business logic.
 * Fires [onQrCodeDecoded] exactly once with the raw string content of the first
 * successfully decoded QR code, then stops processing further frames.
 *
 * Handles the CAMERA permission request internally. If the permission has already
 * been granted, the camera starts immediately. Otherwise, it requests the permission
 * and shows a fallback button in case the user previously denied it.
 */
@Composable
fun QrScannerView(
    onQrCodeDecoded: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    PermissionGate(
        permission = Manifest.permission.CAMERA,
        rationaleText = "Camera permission is required to scan QR codes.",
        label = "Request Camera Permission",
        modifier = modifier,
    ) {
        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
        val analyzer = remember { QrCodeAnalyzer(onQrCodeDecoded) }
        var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

        DisposableEffect(Unit) {
            onDispose {
                cameraProvider?.unbindAll()
                cameraExecutor.shutdown()
            }
        }

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val future = ProcessCameraProvider.getInstance(ctx)
                    future.addListener({
                        runCatching {
                            val provider = future.get()
                            cameraProvider = provider

                            val preview = Preview.Builder()
                                .setTargetFrameRate(Range(30, 60))
                                .build()
                                .also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { it.setAnalyzer(cameraExecutor, analyzer) }

                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis,
                            )
                        }.onFailure { e ->
                            Timber.e(e, "Failed to bind camera use cases")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = modifier.clip(RoundedCornerShape(24.dp)),
        )
    }
}
