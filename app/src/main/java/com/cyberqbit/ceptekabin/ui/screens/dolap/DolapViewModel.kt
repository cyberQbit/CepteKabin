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

    private val _kiyafetler = MutableStateFlow<List<Kiyaket>>(emptyList())
    val kiyafetler: StateFlow<List<Kiyaket>> = _kiyafetler.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    // Geriye uyumluluk
    val seciliKategori: StateFlow<String> get() = _selectedCategory
    private val _sadeceFavoriler = MutableStateFlow(false)
    val sadeceFavoriler: StateFlow<Boolean> = _sadeceFavoriler.asStateFlow()

    init {
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { _kiyafetler.value = it }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategory(category: String) { _selectedCategory.value = category }
    fun kategoriSec(kategori: String) { setCategory(kategori) }

    fun enterMultiSelect(firstId: Long) {
        _isMultiSelectMode.value = true
        _selectedIds.value = setOf(firstId)
    }

    fun toggleSelection(id: Long) {
        _selectedIds.update { current -> if (id in current) current - id else current + id }
        if (_selectedIds.value.isEmpty()) _isMultiSelectMode.value = false
    }

    fun exitMultiSelect() {
        _isMultiSelectMode.value = false
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id -> kiyaketRepository.deleteKiyaketById(id) }
            exitMultiSelect()
        }
    }

    fun toggleFavoriler() {
        _sadeceFavoriler.value = !_sadeceFavoriler.value
        if (_sadeceFavoriler.value) {
            viewModelScope.launch {
                kiyaketRepository.getFavoriKiyaketler().collect { _kiyafetler.value = it }
            }
        } else {
            viewModelScope.launch {
                kiyaketRepository.getAllKiyaketler().collect { _kiyafetler.value = it }
            }
        }
    }

    fun toggleFavori(kiyaket: Kiyaket) {
        viewModelScope.launch { kiyaketRepository.toggleFavori(kiyaket.id, !kiyaket.favori) }
    }

    fun deleteKiyaket(kiyaket: Kiyaket) {
        viewModelScope.launch { kiyaketRepository.deleteKiyaket(kiyaket) }
    }
}
