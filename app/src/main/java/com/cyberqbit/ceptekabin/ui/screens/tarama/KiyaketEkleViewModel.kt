package com.cyberqbit.ceptekabin.ui.screens.tarama

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.remote.api.UrunKoduSearchService
import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.repository.BarkodRepository
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
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
    private val urunKoduSearchService: UrunKoduSearchService
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
                kiyaketRepository.insertKiyaket(kiyaket)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Kayıt hatası")
            }
        }
    }

    fun temizle() {
        _barkodSonuc.value = null
        _errorMessage.value = null
        _kaynak.value = ""
        _urunKoduHata.value = null
    }
}

