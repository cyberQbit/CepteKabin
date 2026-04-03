package com.cyberqbit.ceptekabin.ui.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.cyberqbit.ceptekabin.domain.model.Kiyaket

data class FriendDolapUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val friendName: String = "",
    val kiyaketler: List<Kiyaket> = emptyList()
)

@HiltViewModel
class FriendDolapViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(FriendDolapUiState())
    val uiState: StateFlow<FriendDolapUiState> = _uiState.asStateFlow()

    fun loadFriendWardrobe(friendUserId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                // TODO: FriendService'den dolabı yükle
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Hata") }
            }
        }
    }
}
