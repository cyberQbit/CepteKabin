package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KombinViewModel @Inject constructor(
    private val kombinRepository: KombinRepository
) : ViewModel() {

    private val _kombinler = MutableStateFlow<List<Kombin>>(emptyList())
    val kombinler: StateFlow<List<Kombin>> = _kombinler.asStateFlow()

    private val _favorilerOnly = MutableStateFlow(false)
    val favorilerOnly: StateFlow<Boolean> = _favorilerOnly.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadKombinler()
    }

    private fun loadKombinler() {
        viewModelScope.launch {
            if (_favorilerOnly.value) {
                kombinRepository.getFavoriKombinler().collect { liste ->
                    _kombinler.value = liste
                }
            } else {
                kombinRepository.getAllKombinler().collect { liste ->
                    _kombinler.value = liste
                }
            }
        }
    }

    fun toggleFavori(kombin: Kombin) {
        viewModelScope.launch {
            kombinRepository.toggleFavori(kombin.id, !kombin.favori)
        }
    }

    fun deleteKombin(kombin: Kombin) {
        viewModelScope.launch {
            kombinRepository.deleteKombin(kombin)
        }
    }

    fun toggleFavorilerOnly() {
        _favorilerOnly.value = !_favorilerOnly.value
        loadKombinler()
    }
}
