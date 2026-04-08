package com.cyberqbit.ceptekabin.util

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.Keep
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

@Keep // Proguard'ın bu sınıfın ismini değiştirmesini engeller (Gson serileştirmesi için)
data class KombinExportData(val kombin: Kombin, val kiyaketler: List<Kiyaket>)

object KombinShareHelper {

    private const val EXTENSION = ".kmb"
    private const val JSON_FILE_NAME = "kombin_data.json"

    // ─── PLAY STORE LINK ────────────────────────────────────────────────────────
    // TODO: Play Store onaylandıktan sonra gerçek linki buraya yaz
    const val PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.cyberqbit.ceptekabin"

    // ─── 1. DOSYA OLUŞTURMA ────────────────────────────────────────────────────

    /**
     * Kombini ve kıyafet resimlerini tek bir .kmb (ZIP) dosyasına sıkıştırır.
     * [shareFile] belirtilmezse cacheDir'de otomatik oluşturulur.
     */
    suspend fun createKmbFile(
        context: Context,
        kombin: Kombin,
        kiyaketler: List<Kiyaket>,
        shareFile: File = File(context.cacheDir, "CepteKabin_${sanitizeFileName(kombin.ad)}.kmb")
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val exportData = KombinExportData(kombin, kiyaketler)
            val json = Gson().toJson(exportData)

            ZipOutputStream(BufferedOutputStream(FileOutputStream(shareFile))).use { zos ->
                // JSON verisi
                zos.putNextEntry(ZipEntry(JSON_FILE_NAME))
                zos.write(json.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // Kıyafet resimleri
                kiyaketler.forEach { kiyaket ->
                    if (!kiyaket.imageUrl.isNullOrBlank()) {
                        runCatching {
                            val imageUri = Uri.parse(kiyaket.imageUrl)
                            context.contentResolver.openInputStream(imageUri)?.use { fis ->
                                zos.putNextEntry(ZipEntry("image_${kiyaket.id}.ckb"))
                                fis.copyTo(zos)
                                zos.closeEntry()
                            }
                        } // Okunamayan resmi sessizce atla
                    }
                }
            }

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", shareFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ─── 2. PAYLAŞIM İNTENT'LERİ ──────────────────────────────────────────────

    /**
     * .kmb dosyasını + açıklama metnini içeren bir share Intent döndürür.
     * WhatsApp, Telegram, E-posta, Drive vb. tüm uygulamalarla çalışır.
     *
     * NOT: WhatsApp dosyayı iletir; metin caption olarak görünür.
     * Telegram her ikisini de ayrı mesaj olarak gönderir.
     */
    suspend fun createFileShareIntent(
        context: Context,
        kombin: Kombin,
        kiyaketler: List<Kiyaket>
    ): Intent? {
        val shareFile = File(
            context.cacheDir,
            "CepteKabin_${sanitizeFileName(kombin.ad)}_Kombini.kmb"
        )
        val fileUri = createKmbFile(context, kombin, kiyaketler, shareFile) ?: return null

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_TEXT, buildFileShareText(kombin, kiyaketler))
            putExtra(Intent.EXTRA_SUBJECT, "CepteKabin — ${kombin.ad} 🎽")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Sadece davet metnini + uygulama linkini paylaşır.
     * Arkadaşı henüz uygulamayı yüklememişse bu intent kullanılır.
     */
    fun createInviteIntent(kombin: Kombin): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, buildInviteText(kombin))
            putExtra(Intent.EXTRA_SUBJECT, "CepteKabin'e Davet 🎽")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    // ─── 3. IMPORT (ALMA) ─────────────────────────────────────────────────────

    /**
     * Gelen .kmb dosyasını parse eder.
     *
     * CRITICAL: Orijinal kıyafet ID'leri KORUNUR çünkü ViewModel bu ID'leri
     * şu şekilde kullanır:
     *   originalId → yeni DB ID eşlemesi
     *
     * Eğer id=0 yapılırsa eşleme bozulur ve kombin referansları (ustGiyim vb.)
     * null kalır.
     */
    suspend fun parseKmbFileForImport(
        context: Context,
        uri: Uri
    ): KombinExportData? = withContext(Dispatchers.IO) {
        try {
            var rawData: KombinExportData? = null
            // "image_5.ckb" → "content://..." yerel URI eşlemesi
            val imageMap = mutableMapOf<String, String>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val entryName = entry.name
                        // Her entry'nin tüm byte'larını oku
                        val bytes = zis.readBytes()

                        when {
                            entryName == JSON_FILE_NAME -> {
                                rawData = Gson().fromJson(
                                    bytes.toString(Charsets.UTF_8),
                                    KombinExportData::class.java
                                )
                            }
                            entryName.startsWith("image_") && entryName.endsWith(".ckb") -> {
                                if (bytes.isNotEmpty()) {
                                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    if (bmp != null) {
                                        val filename = "kmb_import_${
                                            entryName.removeSuffix(".ckb")
                                        }_${System.currentTimeMillis()}"
                                        val savedUri = LocalImageStorageHelper.saveBitmapToGallery(
                                            context, bmp, filename
                                        )
                                        if (savedUri != null) imageMap[entryName] = savedUri
                                    }
                                }
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }

            rawData?.let { data ->
                // Resimleri güncelle, orijinal ID'leri KOR
                val updatedKiyaketMap: Map<Long, Kiyaket> = data.kiyaketler.associate { k ->
                    val imageKey = "image_${k.id}.ckb"
                    k.id to k.copy(imageUrl = imageMap[imageKey] ?: k.imageUrl)
                }

                val updatedKombinKiyaketler = data.kiyaketler.map { k ->
                    updatedKiyaketMap[k.id] ?: k
                }

                // Kombin'in kıyafet referanslarını da güncelle (resim URL'leri için)
                val updatedKombin = data.kombin.copy(
                    ustGiyim  = data.kombin.ustGiyim?.let  { updatedKiyaketMap[it.id] ?: it },
                    altGiyim  = data.kombin.altGiyim?.let  { updatedKiyaketMap[it.id] ?: it },
                    disGiyim  = data.kombin.disGiyim?.let  { updatedKiyaketMap[it.id] ?: it },
                    ayakkabi  = data.kombin.ayakkabi?.let  { updatedKiyaketMap[it.id] ?: it },
                    aksesuar  = data.kombin.aksesuar?.let  { updatedKiyaketMap[it.id] ?: it }
                )

                KombinExportData(updatedKombin, updatedKombinKiyaketler)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ─── 4. YARDIMCI FONKSİYONLAR ─────────────────────────────────────────────

    /** Promo görsel için app_logo URI'si döner (paylaşım fallback'i için) */
    suspend fun getPromoImageUri(context: Context): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.cacheDir, "CepteKabin_Promo.png")
            if (!file.exists()) {
                val d = androidx.core.content.ContextCompat.getDrawable(
                    context, com.cyberqbit.ceptekabin.R.drawable.app_logo
                )
                if (d != null) {
                    val w = d.intrinsicWidth.takeIf { it > 0 } ?: 512
                    val h = d.intrinsicHeight.takeIf { it > 0 } ?: 512
                    val bmp = android.graphics.Bitmap.createBitmap(
                        w, h, android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bmp)
                    d.setBounds(0, 0, canvas.width, canvas.height)
                    d.draw(canvas)
                    FileOutputStream(file).use {
                        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull()
    }

    /** Dosya adı için güvenli string üretir */
    fun sanitizeFileName(name: String): String =
        name.replace(Regex("[^\\w\\sğüşıöçĞÜŞİÖÇ-]"), "")
            .replace("\\s+".toRegex(), "_")
            .take(30)
            .ifBlank { "Kombin" }

    // ─── 5. METİN ŞABLONLARİ ──────────────────────────────────────────────────

    private fun buildFileShareText(kombin: Kombin, kiyaketler: List<Kiyaket>): String {
        val parcalar = listOfNotNull(
            kombin.ustGiyim?.let  { "👕 ${it.marka} — ${it.tur.displayName}" },
            kombin.altGiyim?.let  { "👖 ${it.marka} — ${it.tur.displayName}" },
            kombin.disGiyim?.let  { "🧥 ${it.marka} — ${it.tur.displayName}" },
            kombin.ayakkabi?.let  { "👟 ${it.marka} — ${it.tur.displayName}" },
            kombin.aksesuar?.let  { "🎩 ${it.marka} — ${it.tur.displayName}" }
        ).joinToString("\n")

        return """
🎽 Sana "${kombin.ad}" kombinimi gönderdim!

$parcalar

✨ Bu kombini 1 adımda dolabına ekle:
  1️⃣ CepteKabin'i yükle → $PLAY_STORE_LINK
  2️⃣ Bu mesajdaki .kmb dosyasına dokun
  3️⃣ Kombin otomatik dolabına eklenir 🎉

_(CepteKabin — Akıllı Dijital Gardırop)_
        """.trimIndent()
    }

    private fun buildInviteText(kombin: Kombin): String = """
🎽 CepteKabin'i kullanıyorum, "${kombin.ad}" kombinimi görmeni istiyorum!

CepteKabin ile:
✅ Kıyafetlerini dijital dolabında tut
✅ Hava durumuna göre kombin önerisi al
✅ Barkod okutarak saniyede kıyafet ekle
✅ Kombinlerini arkadaşlarınla paylaş 💌

📲 Ücretsiz indir: $PLAY_STORE_LINK
    """.trimIndent()

    // ─── 6. LEGACY (Geriye uyumluluk) ─────────────────────────────────────────

    /** @deprecated Yeni kod parseKmbFileForImport() kullanmalı */
    suspend fun importKmbFile(context: Context, uri: Uri): KombinExportData? =
        parseKmbFileForImport(context, uri)

    // ─── 7. ORGANİK BÜYÜME: GENEL UYGULAMA PAYLAŞIMI ───────────────────────

    /**
     * Sadece davet metnini ve uygulama linkini paylaşır (Kombin bağımsız).
     * Ana sayfadaki rastgele paylaşım teşviki için kullanılır.
     */
    fun createAppInviteIntent(): Intent {
        val text = """
🎽 CepteKabin ile sen de dijital gardırobunu oluştur!

✅ Kıyafetlerini telefonunda tut
✅ Hava durumuna göre kombin önerisi al
✅ Barkod okutarak saniyede kıyafet ekle

📲 Ücretsiz indir: $PLAY_STORE_LINK
        """.trimIndent()

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "CepteKabin'e Katıl 🎽")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
