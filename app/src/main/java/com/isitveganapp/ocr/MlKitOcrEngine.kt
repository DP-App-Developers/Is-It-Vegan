package com.isitveganapp.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class MlKitOcrEngine @Inject constructor() {

    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(bitmap: Bitmap, rotationDegrees: Int = 0): Result<String> =
        suspendCoroutine { cont ->
            val prepared = scaleBitmap(bitmap)
            val image = InputImage.fromBitmap(prepared, rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (prepared !== bitmap) prepared.recycle()
                    val cleaned = visionText.text
                        .replace("|", "I")
                    cont.resume(Result.success(cleaned))
                }
                .addOnFailureListener { e ->
                    if (prepared !== bitmap) prepared.recycle()
                    cont.resume(Result.failure(e))
                }
        }

    fun close() = recognizer.close()

    // ML Kit text recognition works best in the 1280–2048 px range. Passing a raw 12 MP
    // capture (~4000×3000) causes ML Kit to downsample internally using a low-quality
    // algorithm, which destroys thin strokes. Downscaling here with bilinear filtering
    // gives the model cleaner per-pixel data.
    private fun scaleBitmap(src: Bitmap): Bitmap {
        val maxDimension = 1920
        if (src.width <= maxDimension && src.height <= maxDimension) return src
        val scale = maxDimension.toFloat() / maxOf(src.width, src.height)
        return Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt(),
            (src.height * scale).toInt(),
            true  // bilinear filtering
        )
    }
}
