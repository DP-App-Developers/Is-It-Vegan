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
            val image = InputImage.fromBitmap(bitmap, rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val cleaned = visionText.text
                        .replace("|", "I")  // common OCR mistake: capital I → pipe
                    cont.resume(Result.success(cleaned))
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }

    fun close() = recognizer.close()
}
