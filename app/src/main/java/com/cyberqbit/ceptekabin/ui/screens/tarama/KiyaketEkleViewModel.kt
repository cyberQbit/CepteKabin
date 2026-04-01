package com.cyberqbit.ceptekabin.ui.screens.tarama

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.remote.api.UrunKoduSearchService
import com.cyberqbit.ceptekabin.data.remote.firebase.StorageService
import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.repository.BarkodRepository
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KiyaketEkleViewModel @Inject constructor(
    private val barkodRepository: BarkodRepository,
    private val kiyaketRepository: KiyaketRepository,
    private val urunKoduSearchService: UrunKoduSearchService,
    private val storageService: StorageService,
    @dagger.hilt.android.qualifiers.ApplicationContext
    private val context: Context
) : ViewModel() {

    private val _barkodSonuc = MutableStateFlow<BarkodSonuc?>(null)
    val barkodSonuc: StateFlow<BarkodSonuc?> = _barkodSonuc.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _kaynak = MutableStateFlow("")
    val kaynak: StateFlow<String> = _kaynak.asStateFlow()

    private val _urunKoduAramaYukleniyor = MutableStateFlow(false)
    val urunKoduAramaYukleniyor: StateFlow<Boolean> = _urunKoduAramaYukleniyor.asStateFlow()

    private val _urunKoduHata = MutableStateFlow<String?>(null)
    val urunKoduHata: StateFlow<String?> = _urunKoduHata.asStateFlow()

    fun barkodAra(barkod: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            barkodRepository.barkodAra(barkod)
                .onSuccess { sonuc ->
                    _barkodSonuc.value = sonuc
                    _kaynak.value = sonuc.kaynak
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                    _barkodSonuc.value = null
                }

            _isLoading.value = false
        }
    }

    fun urunKoduAra(urunKodu: String) {
        if (urunKodu.isBlank()) return
        viewModelScope.launch {
            _urunKoduAramaYukleniyor.value = true
            _urunKoduHata.value = null

            val sonuc = urunKoduSearchService.urunKoduAra(urunKodu.trim())
            if (sonuc != null) {
                _barkodSonuc.value = sonuc
                _kaynak.value = "urun_kodu"
                _urunKoduHata.value = null
            } else {
                _urunKoduHata.value = "Ürün kodu ile ürün bulunamadı"
            }

            _urunKoduAramaYukleniyor.value = false
        }
    }

    fun saveKiyaket(
        kiyaket: Kiyaket,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalKiyaket = kiyaket

                // Process image if it's a local URI
                if (!kiyaket.imageUrl.isNullOrBlank() && 
                    (kiyaket.imageUrl!!.startsWith("content://") || kiyaket.imageUrl!!.startsWith("file://"))) {
                    
                    try {
                        // Decode Bitmap from URI
                        val bitmap = decodeBitmapFromUri(kiyaket.imageUrl!!)
                        
                        if (bitmap != null) {
                            // Get current user ID or use anonymous
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
                            
                            // Upload image to Firebase Storage
                            val uploadResult = storageService.uploadKiyafetImage(bitmap, userId)
                            
                            val pair = uploadResult.getOrNull()
                            if (pair != null) {
                                val downloadUrl = pair.first
                                val storagePath = pair.second
                                finalKiyaket = kiyaket.copy(
                                    imageUrl = downloadUrl,
                                    firebaseStoragePath = storagePath
                                )
                            } else {
                                throw Exception("Image upload failed")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onError("Görüntü işleme hatası: ${e.message}")
                        _isLoading.value = false
                        return@launch
                    }
                }

                // Save to repository
                kiyaketRepository.insertKiyaket(finalKiyaket)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Kayıt hatası")
            }
        }
    }

    private fun decodeBitmapFromUri(uri: String): Bitmap? {
        return try {
            val contentUri = android.net.Uri.parse(uri)
            
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, contentUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, contentUri)
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun temizle() {
        _barkodSonuc.value = null
        _errorMessage.value = null
        _kaynak.value = ""
        _urunKoduHata.value = null
    }
}
