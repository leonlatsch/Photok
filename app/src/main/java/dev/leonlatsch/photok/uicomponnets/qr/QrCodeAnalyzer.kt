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

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import timber.log.Timber

/**
 * CameraX [ImageAnalysis.Analyzer] that decodes QR codes from camera frames using ZXing.
 *
 * Domain-agnostic: delivers a raw [String] to [onQrCodeDecoded] and knows nothing about
 * what that string represents. The caller decides what to do with it.
 *
 * Duplicate-frame prevention: [onQrCodeDecoded] is only called when the decoded text
 * differs from the last successfully decoded value. This avoids flooding the caller with
 * repeated callbacks while the same code is held in frame, but still fires again when a
 * different code is seen — allowing the user to retry after an invalid scan.
 */
class QrCodeAnalyzer(
    private val onQrCodeDecoded: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
            )
        )
    }

    @Volatile private var lastDecodedText: String? = null

    override fun analyze(image: ImageProxy) {
        try {
            val plane = image.planes[0]
            val bytes = ByteArray(plane.buffer.remaining())
            plane.buffer.get(bytes)

            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0, 0,
                image.width,
                image.height,
                false,
            )

            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decode(bitmap)

            if (result.text != lastDecodedText) {
                lastDecodedText = result.text
                onQrCodeDecoded(result.text)
            }
        } catch (_: NotFoundException) {
            // No QR code in this frame — expected, ignore silently.
        } catch (e: Exception) {
            Timber.e(e, "QR code analysis failed")
        } finally {
            image.close()
        }
    }
}
