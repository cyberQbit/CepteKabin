package com.cyberqbit.ceptekabin.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

object LocalImageStorageHelper {
    
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String): String? {
        val contentValues = ContentValues().apply {
            // UZANTI HİLESİ: .ckb yaparak Galerinin bunu resim olarak görmesini engelliyoruz
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.ckb")
            // MIME HİLESİ: Resim değil, rastgele bir dosya gibi tanıtıyoruz
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Pictures yerine Belgeler klasöründe saklıyoruz (Galeri burayı taramaz)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/CepteKabin")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        // MediaStore.Images yerine MediaStore.Files kullanıyoruz!
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        return uri?.let { imageUri ->
            try {
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    // SIKIŞTIRMA: JPEG yerine WEBP kullanıp, kaliteyi 70'e çekerek devasa yer tasarrufu sağlıyoruz
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 70, outputStream)
                    } else {
                        @Suppress("DEPRECATION")
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 70, outputStream)
                    }
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                
                imageUri.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                resolver.delete(imageUri, null, null)
                null
            }
        }
    }
}
