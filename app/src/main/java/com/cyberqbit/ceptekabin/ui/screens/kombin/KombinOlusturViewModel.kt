package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KombinOlusturUiState(
    val kiyaketler: List<Kiyaket> = emptyList(),
    val seciliUst: Kiyaket? = null,
    val seciliAlt: Kiyaket? = null,
    val seciliDis: Kiyaket? = null,
    val seciliAyak: Kiyaket? = null,
    val seciliAksesuar: Kiyaket? = null,
    val yukleniyor: Boolean = false,
    val savedSuccess: Boolean = false,
    val hata: String? = null
)

@HiltViewModel
class KombinOlusturViewModel @Inject constructor(
    private val kiyaketRepository: KiyaketRepository,
    private val kombinRepository: KombinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KombinOlusturUiState())
    val uiState: StateFlow<KombinOlusturUiState> = _uiState.asStateFlow()

    init {
        loadKiyaketler()
    }

    private fun loadKiyaketler() {
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { liste ->
                _uiState.update { it.copy(kiyaketler = liste) }
            }
        }
    }

    fun secKiyaket(slot: KiyaketSlot, kiyaket: Kiyaket) {
        _uiState.update {
            when (slot) {
                KiyaketSlot.UST      -> it.copy(seciliUst = kiyaket)
                KiyaketSlot.ALT      -> it.copy(seciliAlt = kiyaket)
                KiyaketSlot.DIS      -> it.copy(seciliDis = kiyaket)
                KiyaketSlot.AYAK     -> it.copy(seciliAyak = kiyaket)
                KiyaketSlot.AKSESUAR -> it.copy(seciliAksesuar = kiyaket)
            }
        }
    }

    fun temizleSlot(slot: KiyaketSlot) {
        _uiState.update {
            when (slot) {
                KiyaketSlot.UST      -> it.copy(seciliUst = null)
                KiyaketSlot.ALT      -> it.copy(seciliAlt = null)
                KiyaketSlot.DIS      -> it.copy(seciliDis = null)
                KiyaketSlot.AYAK     -> it.copy(seciliAyak = null)
                KiyaketSlot.AKSESUAR -> it.copy(seciliAksesuar = null)
            }
        }
    }

    fun kaydet(kombin: Kombin) {
        if (kombin.ustGiyim == null && kombin.altGiyim == null) {
            _uiState.update { it.copy(hata = "En az bir üst veya alt giyim seçmelisiniz.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true, hata = null) }
            try {
                kombinRepository.insertKombin(kombin)
                _uiState.update { it.copy(yukleniyor = false, savedSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(yukleniyor = false, hata = e.message ?: "Kayıt hatası") }
            }
        }
    }

    fun loadKombinForEdit(kombinId: Long) {
        if (kombinId == 0L) return
        viewModelScope.launch {
            val kombin = kombinRepository.getKombinById(kombinId)
            kombin?.let { k ->
                _uiState.update { state ->
                    state.copy(
                        seciliUst = k.ustGiyim,
                        seciliAlt = k.altGiyim,
                        seciliDis = k.disGiyim,
                        seciliAyak = k.ayakkabi,
                        seciliAksesuar = k.aksesuar
                    )
                }
            }
        }
    }
}
