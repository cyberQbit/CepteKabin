package com.cyberqbit.ceptekabin.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object PhotoValidationUtil {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError>
    ) {
        val hasWarnings: Boolean get() = errors.any { it.severity == Severity.WARNING }
    }

    data class ValidationError(
        val type: ErrorType,
        val message: String,
        val severity: Severity = Severity.ERROR
    )

    enum class ErrorType {
        WRONG_ORIENTATION, TOO_MANY_FACES, NO_FACE_DETECTED,
        TOO_DARK, TOO_BRIGHT, LOW_RESOLUTION, LOAD_FAILED
    }

    enum class Severity { ERROR, WARNING }

    suspend fun validate(context: Context, uri: Uri): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        val bitmap = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) { null }

        if (bitmap == null) {
            return ValidationResult(false, listOf(
                ValidationError(ErrorType.LOAD_FAILED, "Fotoğraf yüklenemedi. Lütfen farklı bir fotoğraf deneyin.")
            ))
        }

        if (bitmap.width < 400 || bitmap.height < 600) {
            errors.add(ValidationError(ErrorType.LOW_RESOLUTION,
                "Fotoğraf çözünürlüğü çok düşük. En az 400x600 piksel olmalı."))
        }

        val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
        if (aspectRatio < 1.2f) {
            errors.add(ValidationError(ErrorType.WRONG_ORIENTATION,
                "Lütfen dikey (tam boy) bir fotoğraf seçin. Yatay fotoğraflar sanal deneme için uygun değil."))
        }

        val brightness = calculateBrightness(bitmap)
        if (brightness < 40) {
            errors.add(ValidationError(ErrorType.TOO_DARK,
                "Fotoğraf çok karanlık. Daha aydınlık bir ortamda çekilmiş fotoğraf tercih edin.", Severity.WARNING))
        }
        if (brightness > 220) {
            errors.add(ValidationError(ErrorType.TOO_BRIGHT,
                "Fotoğraf çok parlak/aşırı pozlanmış. Daha dengeli ışıkta çekilmiş fotoğraf tercih edin.", Severity.WARNING))
        }

        val faceCount = detectFaces(context, uri)
        if (faceCount == 0) {
            errors.add(ValidationError(ErrorType.NO_FACE_DETECTED,
                "Fotoğrafta yüz tespit edilemedi. Tam boy, ayakta duran ve yüzünüzün görünen bir fotoğraf seçin.", Severity.WARNING))
        }
        if (faceCount > 1) {
            errors.add(ValidationError(ErrorType.TOO_MANY_FACES,
                "Fotoğrafta $faceCount kişi tespit edildi. Lütfen sadece kendinizin olduğu bir fotoğraf seçin."))
        }

        bitmap.recycle()
        val hardErrors = errors.filter { it.severity == Severity.ERROR }
        return ValidationResult(isValid = hardErrors.isEmpty(), errors = errors)
    }

    private fun calculateBrightness(bitmap: Bitmap): Int {
        var totalBrightness = 0L
        var pixelCount = 0
        val step = 10
        for (y in 0 until bitmap.height step step) {
            for (x in 0 until bitmap.width step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                totalBrightness += (0.299 * r + 0.587 * g + 0.114 * b).toLong()
                pixelCount++
            }
        }
        return if (pixelCount > 0) (totalBrightness / pixelCount).toInt() else 128
    }

    private suspend fun detectFaces(context: Context, uri: Uri): Int =
        suspendCancellableCoroutine { cont ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                val options = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .build()
                val detector = FaceDetection.getClient(options)
                detector.process(image)
                    .addOnSuccessListener { faces -> cont.resume(faces.size) }
                    .addOnFailureListener { cont.resume(-1) }
            } catch (e: Exception) { cont.resume(-1) }
        }
}
