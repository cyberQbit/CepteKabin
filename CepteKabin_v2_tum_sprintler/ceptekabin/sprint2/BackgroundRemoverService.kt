package com.cyberqbit.ceptekabin.data.remote.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Arka plan kaldırma servisi.
 *
 * Strateji (ücretsiz → ücretli sırasıyla):
 *  1. remove.bg ücretsiz API (50 önizleme/ay, 500×500 max)
 *  2. PhotoRoom ücretsiz katman
 *
 * Kullanıcı kendi API anahtarını DataStore'a kaydedebilir.
 * Anahtar yoksa → null döner ve UI orijinal görseli kullanır.
 */
class BackgroundRemoverService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val REMOVE_BG_URL  = "https://api.remove.bg/v1.0/removebg"
        private const val PHOTOROOM_URL  = "https://sdk.photoroom.com/v1/segment"
        private const val PREVIEW_SIZE   = 500  // ücretsiz tier limiti
    }

    /**
     * [apiKey]  — remove.bg API anahtarı (opsiyonel, yoksa null)
     * [bitmap]  — işlenecek kaynak görsel
     * Dönen [Bitmap]  — PNG (şeffaf arka plan), hata durumunda null
     */
    suspend fun removeBackground(bitmap: Bitmap, apiKey: String? = null): Bitmap? =
        withContext(Dispatchers.IO) {
            if (apiKey.isNullOrBlank()) return@withContext null

            try {
                val scaled = scaleBitmap(bitmap, PREVIEW_SIZE)
                val pngBytes = bitmapToPng(scaled)

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image_file", "image.png",
                        pngBytes.toRequestBody("image/png".toMediaType())
                    )
                    .addFormDataPart("size", "preview")
                    .build()

                val request = Request.Builder()
                    .url(REMOVE_BG_URL)
                    .header("X-Api-Key", apiKey)
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val bytes = response.body?.bytes() ?: return@withContext null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {
                null
            }
        }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        if (w <= maxSize && h <= maxSize) return bitmap
        val ratio = w.toFloat() / h
        return if (ratio > 1) Bitmap.createScaledBitmap(bitmap, maxSize, (maxSize / ratio).toInt(), true)
        else Bitmap.createScaledBitmap(bitmap, (maxSize * ratio).toInt(), maxSize, true)
    }

    private fun bitmapToPng(bitmap: Bitmap): ByteArray {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
        return bos.toByteArray()
    }
}
