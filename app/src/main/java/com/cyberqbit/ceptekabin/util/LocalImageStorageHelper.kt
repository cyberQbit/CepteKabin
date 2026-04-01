package com.cyberqbit.ceptekabin.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

object LocalImageStorageHelper {
    
    /**
     * Bitmap'i Android'in kalıcı "Pictures/CepteKabin" klasörüne kaydeder.
     * Uygulama silinse bile bu klasör ve içindeki resimler silinmez.
     * Kaydedilen resmin cihazdaki adresini (URI) String olarak döndürür.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            // Android 10 (Q) ve üzeri için özel klasör yolu belirtiyoruz
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CepteKabin")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        return uri?.let { imageUri ->
            try {
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    // Resmi %90 kalite ile JPEG olarak sıkıştırıp kaydediyoruz
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                
                imageUri.toString() // Başarılıysa adresi döndür
            } catch (e: Exception) {
                e.printStackTrace()
                resolver.delete(imageUri, null, null) // Hata olursa bozuk dosyayı sil
                null
            }
        }
    }
}
