package com.isitveganapp.ocr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@Singleton
class MlKitOcrEngine @Inject constructor() {

    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(bitmap: Bitmap, rotationDegrees: Int = 0): Result<String> =
        withContext(Dispatchers.Default) {
            if (isBlurry(bitmap)) {
                return@withContext Result.failure(Exception("Image too blurry — move closer to the label and hold steady"))
            }
            val prepared = preprocessBitmap(bitmap)
            suspendCancellableCoroutine { cont ->
                val image = InputImage.fromBitmap(prepared, rotationDegrees)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        if (prepared !== bitmap) prepared.recycle()
                        cont.resume(Result.success(visionText.text.let(::fixOcrConfusions)))
                    }
                    .addOnFailureListener { e ->
                        if (prepared !== bitmap) prepared.recycle()
                        cont.resume(Result.failure(e))
                    }
            }
        }

    fun close() = recognizer.close()

    // Laplacian variance: apply a 4-neighbour Laplacian kernel to a small grayscale
    // thumbnail and compute the variance of the response. Sharp images have strong edges
    // (high variance); blurry images have soft edges (low variance).
    // We work on a 200×150 downsample so this runs in < 10 ms on any modern device.
    private fun isBlurry(bitmap: Bitmap): Boolean {
        val w = 200
        val h = 150
        val small = Bitmap.createScaledBitmap(bitmap, w, h, true)
        val pixels = IntArray(w * h)
        small.getPixels(pixels, 0, w, 0, 0, w, h)
        small.recycle()

        val gray = IntArray(w * h) { i ->
            val p = pixels[i]
            (((p shr 16) and 0xFF) * 299 + ((p shr 8) and 0xFF) * 587 + (p and 0xFF) * 114) / 1000
        }

        var lapSum = 0L
        var lapSumSq = 0L
        val count = (w - 2) * (h - 2)

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val lap = gray[(y - 1) * w + x] + gray[(y + 1) * w + x] +
                        gray[y * w + (x - 1)] + gray[y * w + (x + 1)] -
                        4 * gray[y * w + x]
                lapSum += lap
                lapSumSq += lap.toLong() * lap
            }
        }

        val mean = lapSum.toDouble() / count
        val variance = lapSumSq.toDouble() / count - mean * mean
        return variance < BLUR_THRESHOLD
    }

    // Converts to grayscale and boosts contrast before handing to ML Kit.
    // ML Kit's text model is trained on high-contrast monochrome inputs; stripping colour
    // noise and stretching luminance contrast sharpens thin strokes on faded or shiny labels.
    private fun preprocessBitmap(src: Bitmap): Bitmap {
        val scaled = scaleBitmap(src)
        val output = Bitmap.createBitmap(scaled.width, scaled.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // pixel' = contrast*(pixel - 128) + 128; offset keeps mid-grey anchored.
        val contrast = 1.4f
        val offset = 128f * (1f - contrast)
        val matrix = ColorMatrix().apply { setSaturation(0f) }
        matrix.postConcat(ColorMatrix(floatArrayOf(
            contrast, 0f,       0f,       0f, offset,
            0f,       contrast, 0f,       0f, offset,
            0f,       0f,       contrast, 0f, offset,
            0f,       0f,       0f,       1f, 0f
        )))

        canvas.drawBitmap(scaled, 0f, 0f, Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        })
        if (scaled !== src) scaled.recycle()
        return output
    }

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
            true
        )
    }

    // Corrects the most frequent character-level OCR confusions on food labels.
    // Each rule is narrow enough to be safe: it only fires where the substitution
    // is unambiguous in context.
    private fun fixOcrConfusions(text: String): String = text
        .replace("|", "I")
        // Digit 0 between two letters is virtually always the letter O (e.g. "c0rn", "coc0a").
        .replace(Regex("(?<=[A-Za-z])0(?=[A-Za-z])"), "o")
        // Digit 1 between two letters is virtually always lowercase L (e.g. "m1lk", "va1illa").
        .replace(Regex("(?<=[A-Za-z])1(?=[A-Za-z])"), "l")

    companion object {
        // Sharp food-label photos typically score 500+. Below 100 the text is too soft
        // for reliable OCR. Tune this constant if real-world scans trip it incorrectly.
        private const val BLUR_THRESHOLD = 100.0
    }
}
