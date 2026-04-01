package com.cyberqbit.ceptekabin.ui.screens.dolap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DolapViewModel @Inject constructor(
    private val kiyaketRepository: KiyaketRepository
) : ViewModel() {

    private val _kiyaketler = MutableStateFlow<List<Kiyaket>>(emptyList())
    val kiyaketler: StateFlow<List<Kiyaket>> = _kiyaketler.asStateFlow()

    private val _seciliKategori = MutableStateFlow("Tümü")
    val seciliKategori: StateFlow<String> = _seciliKategori.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadKiyaketler()
    }

    private fun loadKiyaketler() {
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { liste ->
                _kiyaketler.value = filtrele(liste, _seciliKategori.value)
            }
        }
    }

    fun kategoriSec(kategori: String) {
        _seciliKategori.value = kategori
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { liste ->
                _kiyaketler.value = filtrele(liste, kategori)
            }
        }
    }

    private fun filtrele(liste: List<Kiyaket>, kategori: String): List<Kiyaket> {
        return when (kategori) {
            "Tümü" -> liste
            "Üst Giyim" -> liste.filter { it.tur.name in listOf("TISORT", "GOMLEK", "SWEAT", "HIRKA") }
            "Alt Giyim" -> liste.filter { it.tur.name in listOf("PANTOLON", "ETEK", "SORT") }
            "Dış Giyim" -> liste.filter { it.tur.name in listOf("CEKET", "KABAN", "MONTO", "YAGMURLUK") }
            "Ayakkabı" -> liste.filter { it.tur.name in listOf("AYAKKABI", "TERLIK", "SANTRAFOR", "BOT") }
            "Aksesuar" -> liste.filter { it.tur.name in listOf("CANTA", "SAPKA", "ESARP", "TAKI", "CORAP") }
            else -> liste
        }
    }

    fun toggleFavori(kiyaket: Kiyaket) {
        viewModelScope.launch {
            kiyaketRepository.toggleFavori(kiyaket.id, !kiyaket.favori)
        }
    }

    fun deleteKiyaket(kiyaket: Kiyaket) {
        viewModelScope.launch {
            kiyaketRepository.deleteKiyaket(kiyaket)
        }
    }
}
