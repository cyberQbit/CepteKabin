package com.cyberqbit.ceptekabin.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class KombinExportData(val kombin: Kombin, val kiyaketler: List<Kiyaket>)

object KombinShareHelper {
    private const val EXTENSION = ".kmb"
    private const val JSON_FILE_NAME = "kombin_data.json"

    // 1. GÖNDERME: Kombini ve resimleri .kmb dosyasına sıkıştırır
    suspend fun createKmbFile(context: Context, kombin: Kombin, kiyaketler: List<Kiyaket>): Uri? = withContext(Dispatchers.IO) {
        try {
            val exportData = KombinExportData(kombin, kiyaketler)
            val jsonString = Gson().toJson(exportData)

            // Cache klasöründe geçici bir .kmb dosyası oluştur
            val shareFile = File(context.cacheDir, "Kombin_${System.currentTimeMillis()}$EXTENSION")
            
            ZipOutputStream(BufferedOutputStream(FileOutputStream(shareFile))).use { zos ->
                // JSON'u ekle
                zos.putNextEntry(ZipEntry(JSON_FILE_NAME))
                zos.write(jsonString.toByteArray())
                zos.closeEntry()

                // Resimleri ekle (Sadece yerel .ckb veya content urileri)
                kiyaketler.forEach { kiyaket ->
                    if (!kiyaket.imageUrl.isNullOrBlank()) {
                        val imageUri = Uri.parse(kiyaket.imageUrl)
                        context.contentResolver.openInputStream(imageUri)?.use { fis ->
                            zos.putNextEntry(ZipEntry("image_${kiyaket.id}.ckb"))
                            fis.copyTo(zos)
                            zos.closeEntry()
                        }
                    }
                }
            }
            // Paylaşım için güvenli URI (FileProvider) oluştur
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", shareFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 2. ALMA: Gelen .kmb dosyasını açar ve telefona kaydeder
    suspend fun importKmbFile(context: Context, uri: Uri): KombinExportData? = withContext(Dispatchers.IO) {
        try {
            var exportData: KombinExportData? = null
            val imageMap = mutableMapOf<String, String>() 

            ZipInputStream(context.contentResolver.openInputStream(uri)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name == JSON_FILE_NAME) {
                        val jsonString = zis.bufferedReader().readText()
                        exportData = Gson().fromJson(jsonString, KombinExportData::class.java)
                    } else if (entry.name.startsWith("image_")) {
                        // Gelen resmi cihazın gizli klasörüne kaydet (.ckb olarak)
                        val bitmap = BitmapFactory.decodeStream(zis)
                        if (bitmap != null) {
                            val newFileName = "kmb_import_${System.currentTimeMillis()}"
                            val savedUri = LocalImageStorageHelper.saveBitmapToGallery(context, bitmap, newFileName)
                            if (savedUri != null) {
                                imageMap[entry.name] = savedUri
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            exportData?.let { data ->
                val updatedKiyaketler = data.kiyaketler.map { kiyaket ->
                    val expectedImageName = "image_${kiyaket.id}.ckb"
                    val newLocalUri = imageMap[expectedImageName]
                    kiyaket.copy(imageUrl = newLocalUri ?: kiyaket.imageUrl, id = 0) 
                }
                val updatedKombin = data.kombin.copy(id = 0)
                return@withContext KombinExportData(updatedKombin, updatedKiyaketler)
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPromoImageUri(context: Context): Uri? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "CepteKabin_Tanitim.png")
            if (!file.exists()) {
                val drawable = androidx.core.content.ContextCompat.getDrawable(context, com.cyberqbit.ceptekabin.R.drawable.app_logo)
                if (drawable != null) {
                    val bitmap = if (drawable is android.graphics.drawable.BitmapDrawable) {
                        drawable.bitmap
                    } else {
                        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 512
                        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 512
                        val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bmp
                    }
                    FileOutputStream(file).use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
