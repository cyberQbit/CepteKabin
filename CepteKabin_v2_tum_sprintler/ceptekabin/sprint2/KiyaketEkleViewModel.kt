package com.cyberqbit.ceptekabin.ui.screens.tarama

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.remote.api.DppUrlService
import com.cyberqbit.ceptekabin.data.remote.api.UrunKoduSearchService
import com.cyberqbit.ceptekabin.data.remote.firebase.StorageService
import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.repository.BarkodRepository
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.util.LocalImageStorageHelper
import com.cyberqbit.ceptekabin.util.isGecerliBarkod
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Sprint 2 — DPP prefix sabiti */
private const val DPP_PREFIX = "DPP:"

@HiltViewModel
class KiyaketEkleViewModel @Inject constructor(
    private val barkodRepository: BarkodRepository,
    private val kiyaketRepository: KiyaketRepository,
    private val urunKoduSearchService: UrunKoduSearchService,
    private val dppUrlService: DppUrlService,          // Sprint 2 — #15
    private val storageService: StorageService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _barkodSonuc              = MutableStateFlow<BarkodSonuc?>(null)
    val barkodSonuc: StateFlow<BarkodSonuc?> = _barkodSonuc.asStateFlow()

    private val _isLoading                = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean>     = _isLoading.asStateFlow()

    private val _errorMessage             = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?>  = _errorMessage.asStateFlow()

    private val _kaynak                   = MutableStateFlow("")
    val kaynak: StateFlow<String>         = _kaynak.asStateFlow()

    private val _urunKoduAramaYukleniyor  = MutableStateFlow(false)
    val urunKoduAramaYukleniyor: StateFlow<Boolean> = _urunKoduAramaYukleniyor.asStateFlow()

    private val _urunKoduHata             = MutableStateFlow<String?>(null)
    val urunKoduHata: StateFlow<String?>  = _urunKoduHata.asStateFlow()

    // ── Barkod / DPP otomatik ayrımı ─────────────────────────────────────────

    fun barkodAra(value: String) {
        if (value.startsWith(DPP_PREFIX)) {
            // #15: DPP QR URL — TaramaScreen tarafından prefix eklendi
            val url = value.removePrefix(DPP_PREFIX)
            dppAra(url)
        } else {
            normalBarkodAra(value)
        }
    }

    private fun dppAra(url: String) {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null
            val sonuc = dppUrlService.fetchDppProduct(url)
            if (sonuc != null) {
                _barkodSonuc.value = sonuc
                _kaynak.value      = "QR (DPP)"
            } else {
                _errorMessage.value = "QR koddan ürün bilgisi alınamadı. Lütfen manuel doldurun."
            }
            _isLoading.value = false
        }
    }

    private fun normalBarkodAra(barkod: String) {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null

            if (!barkod.isGecerliBarkod()) {
                _isLoading.value    = false
                _errorMessage.value = "Hatalı barkod okuması. Lütfen tekrar deneyin."
                return@launch
            }

            try {
                if (kiyaketRepository.checkBarkodExists(barkod)) {
                    _isLoading.value    = false
                    _errorMessage.value = "Bu ürün zaten dolabınızda kayıtlı!"
                    return@launch
                }
            } catch (_: Exception) {}

            barkodRepository.barkodAra(barkod)
                .onSuccess { sonuc ->
                    _barkodSonuc.value = sonuc
                    _kaynak.value      = sonuc.kaynak
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                    _barkodSonuc.value  = null
                }
            _isLoading.value = false
        }
    }

    fun urunKoduAra(urunKodu: String) {
        if (urunKodu.isBlank()) return
        viewModelScope.launch {
            _urunKoduAramaYukleniyor.value = true
            _urunKoduHata.value            = null
            val sonuc = urunKoduSearchService.urunKoduAra(urunKodu.trim())
            if (sonuc != null) {
                _barkodSonuc.value         = sonuc
                _kaynak.value              = "Ürün Kodu"
                _urunKoduHata.value        = null
            } else {
                _urunKoduHata.value = "Ürün kodu ile ürün bulunamadı."
            }
            _urunKoduAramaYukleniyor.value = false
        }
    }

    fun saveKiyaket(kiyaket: Kiyaket, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalKiyaket = kiyaket
                if (!kiyaket.imageUrl.isNullOrBlank() &&
                    (kiyaket.imageUrl.startsWith("content://") ||
                     kiyaket.imageUrl.startsWith("file://"))) {
                    val bmp = decodeBitmapFromUri(kiyaket.imageUrl)
                    if (bmp != null) {
                        val savedUri = LocalImageStorageHelper.saveBitmapToGallery(
                            context, bmp,
                            "kiyafet_${System.currentTimeMillis()}"
                        )
                        if (savedUri != null) {
                            finalKiyaket = kiyaket.copy(imageUrl = savedUri, firebaseStoragePath = null)
                        } else {
                            throw Exception("Fotoğraf cihaza kaydedilemedi.")
                        }
                    }
                }
                kiyaketRepository.insertKiyaket(finalKiyaket)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Kayıt hatası oluştu.")
            }
        }
    }

    private fun decodeBitmapFromUri(uri: String): Bitmap? = try {
        val contentUri = android.net.Uri.parse(uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, contentUri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, contentUri)
        }
    } catch (_: Exception) { null }

    fun temizle() {
        _barkodSonuc.value    = null
        _errorMessage.value   = null
        _kaynak.value         = ""
        _urunKoduHata.value   = null
    }

    suspend fun getKiyaket(id: Long): Kiyaket? = kiyaketRepository.getKiyaketById(id)
}
