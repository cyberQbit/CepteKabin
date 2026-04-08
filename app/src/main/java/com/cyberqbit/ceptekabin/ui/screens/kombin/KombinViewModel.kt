package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KombinViewModel @Inject constructor(
    private val kombinRepository: KombinRepository,
    private val kiyaketRepository: KiyaketRepository
) : ViewModel() {

    private val _kombinler = MutableStateFlow<List<Kombin>>(emptyList())
    val kombinler: StateFlow<List<Kombin>> = _kombinler.asStateFlow()

    private val _favorilerOnly = MutableStateFlow(false)
    val favorilerOnly: StateFlow<Boolean> = _favorilerOnly.asStateFlow()

    private val _siralama = MutableStateFlow(KombinSiralama.EN_YENI)
    val siralama: StateFlow<KombinSiralama> = _siralama.asStateFlow()

    private val _dolapBos = MutableStateFlow(false)
    val dolapBos: StateFlow<Boolean> = _dolapBos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadKombinler()
        checkDolapBos()
    }

    private fun loadKombinler() {
        viewModelScope.launch {
            val flow = if (_favorilerOnly.value) kombinRepository.getFavoriKombinler()
            else kombinRepository.getAllKombinler()
            flow.collect { list ->
                _kombinler.value = when (_siralama.value) {
                    KombinSiralama.EN_YENI -> list.sortedByDescending { it.olusturmaTarihi }
                    KombinSiralama.FAVORILER -> list.sortedByDescending { it.favori }
                    KombinSiralama.EN_COK_GIYILEN -> list.sortedByDescending { it.puan }
                }
            }
        }
    }

    private fun checkDolapBos() {
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { list ->
                _dolapBos.value = list.isEmpty()
            }
        }
    }

    suspend fun getKombinById(id: Long): Kombin? = kombinRepository.getKombinById(id)

    fun setSiralama(s: KombinSiralama) {
        _siralama.value = s
        loadKombinler()
    }

    fun toggleFavori(kombin: Kombin) {
        val guncelKombinler = _kombinler.value.map {
            if (it.id == kombin.id) it.copy(favori = !it.favori) else it
        }
        _kombinler.value = guncelKombinler
        viewModelScope.launch { kombinRepository.toggleFavori(kombin.id, !kombin.favori) }
    }

    fun shareKombin(kombin: Kombin) {
        // Paylaşım mantığı - ileride KombinShareHelper ile entegre edilecek
    }

    fun deleteKombin(kombin: Kombin) {
        viewModelScope.launch { kombinRepository.deleteKombin(kombin) }
    }

    fun incrementPuan(id: Long) {
        viewModelScope.launch { kombinRepository.incrementPuan(id) }
    }

    fun toggleFavorilerOnly() {
        _favorilerOnly.value = !_favorilerOnly.value
        loadKombinler()
    }
}
