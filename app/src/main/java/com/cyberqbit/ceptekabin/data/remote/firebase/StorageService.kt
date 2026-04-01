package com.cyberqbit.ceptekabin.data.remote.firebase

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor() {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadKiyafetImage(bitmap: Bitmap, userId: String): Result<Pair<String, String>> {
        return try {
            // Optimize image: Max 1080p, 80% JPEG quality
            val scaledBitmap = scaleBitmapToMaxSize(bitmap, 1080)
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()

            val fileName = "kiyafetler/${userId}/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(fileName)
            
            val uploadTask = imageRef.putBytes(data).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            
            Result.success(Pair(downloadUrl, fileName))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun scaleBitmapToMaxSize(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) return bitmap
        
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (ratio > 1) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
