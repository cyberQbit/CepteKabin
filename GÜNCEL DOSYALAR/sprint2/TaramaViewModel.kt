package com.cyberqbit.ceptekabin.ui.screens.tarama

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.service.DppUrlService
import com.cyberqbit.ceptekabin.domain.model.BarkodSonuc
import com.cyberqbit.ceptekabin.domain.repository.BarkodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaramaUiState(
    val isScanning: Boolean        = true,
    val isLoading: Boolean         = false,
    val errorMessage: String?      = null,
    val sonuc: BarkodSonuc?        = null,
    val isDppUrl: Boolean          = false,       // QR kodu URL mi?
    val showDppPreview: Boolean    = false,        // DPP önizleme diyaloğu
    val tarananDeger: String       = ""
)

@HiltViewModel
class TaramaViewModel @Inject constructor(
    private val barkodRepository: BarkodRepository,
    private val dppUrlService: DppUrlService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaramaUiState())
    val uiState: StateFlow<TaramaUiState> = _uiState.asStateFlow()

    fun onBarkodDetected(value: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isScanning = false, tarananDeger = value) }

        when {
            DppUrlService.isDppUrl(value) -> handleDppUrl(value)
            else                          -> handleBarkod(value)
        }
    }

    // ── DPP QR URL ───────────────────────────────────────────────────────────
    private fun handleDppUrl(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isDppUrl = true) }
            dppUrlService.urunBilgisiCek(url)
                .onSuccess { sonuc ->
                    _uiState.update {
                        it.copy(isLoading = false, sonuc = sonuc, showDppPreview = true)
                    }
                }
                .onFailure { e ->
                    // DPP başarısız → KiyaketEkleScreen'e URL ile git
                    _uiState.update {
                        it.copy(isLoading = false,
                            errorMessage = "Ürün bilgisi çekilemedi: ${e.message}",
                            sonuc = BarkodSonuc(barkod = url, kaynak = "dpp_fallback"))
                    }
                }
        }
    }

    // ── Normal barkod / QR ───────────────────────────────────────────────────
    private fun handleBarkod(barkod: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isDppUrl = false) }
            barkodRepository.getBarkodBilgisi(barkod)
                .onSuccess { sonuc ->
                    _uiState.update { it.copy(isLoading = false, sonuc = sonuc) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false,
                            errorMessage = e.message ?: "Barkod sorgulama başarısız",
                            sonuc = BarkodSonuc(barkod = barkod, kaynak = "manual"))
                    }
                }
        }
    }

    fun dismissDppPreview() {
        _uiState.update { it.copy(showDppPreview = false) }
    }

    fun retry() {
        _uiState.update { TaramaUiState(isScanning = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
