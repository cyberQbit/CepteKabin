package com.cyberqbit.ceptekabin.ui.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VirtualTryOnUiState(
    val isLoading: Boolean = false,
    val previewUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class VirtualTryOnViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(VirtualTryOnUiState())
    val uiState: StateFlow<VirtualTryOnUiState> = _uiState.asStateFlow()

    fun generatePreview(kombinId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                // TODO: ML model ile preview oluştur
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Hata") }
            }
        }
    }
}
