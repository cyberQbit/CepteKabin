package com.cyberqbit.ceptekabin.ui.screens.dolap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KiyaketDetayUiState(
    val kiyaket: Kiyaket? = null,
    val yukleniyor: Boolean = false,
    val deleted: Boolean = false,
    val hata: String? = null
)

@HiltViewModel
class KiyaketDetayViewModel @Inject constructor(
    private val kiyaketRepository: KiyaketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KiyaketDetayUiState())
    val uiState: StateFlow<KiyaketDetayUiState> = _uiState.asStateFlow()

    fun loadKiyaket(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true) }
            val kiyaket = kiyaketRepository.getKiyaketById(id)
            _uiState.update { it.copy(kiyaket = kiyaket, yukleniyor = false) }
        }
    }

    fun toggleFavori(kiyaket: Kiyaket) {
        viewModelScope.launch {
            kiyaketRepository.toggleFavori(kiyaket.id, !kiyaket.favori)
            _uiState.update { it.copy(kiyaket = kiyaket.copy(favori = !kiyaket.favori)) }
        }
    }

    fun delete() {
        val k = _uiState.value.kiyaket ?: return
        viewModelScope.launch {
            kiyaketRepository.deleteKiyaket(k)
            _uiState.update { it.copy(deleted = true) }
        }
    }

    fun incrementKullanim(id: Long) {
        viewModelScope.launch {
            kiyaketRepository.incrementKullanim(id)
            _uiState.update { state ->
                state.copy(kiyaket = state.kiyaket?.copy(kullanimSayisi = (state.kiyaket.kullanimSayisi + 1)))
            }
        }
    }
}
