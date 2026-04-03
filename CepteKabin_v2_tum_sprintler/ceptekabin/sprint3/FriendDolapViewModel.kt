package com.cyberqbit.ceptekabin.ui.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.remote.firebase.FriendProfile
import com.cyberqbit.ceptekabin.data.remote.firebase.FriendRequest
import com.cyberqbit.ceptekabin.data.remote.firebase.FriendService
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Arkadaş Dolabı ─────────────────────────────────────────────────────────────

data class FriendDolapUiState(
    val kiyaketler: List<Kiyaket> = emptyList(),
    val friendName: String        = "",
    val isLoading: Boolean        = false,
    val error: String?            = null
)

@HiltViewModel
class FriendDolapViewModel @Inject constructor(
    private val friendService: FriendService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendDolapUiState())
    val uiState: StateFlow<FriendDolapUiState> = _uiState.asStateFlow()

    fun loadFriendWardrobe(friendUid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            friendService.getFriendKiyaketler(friendUid)
                .onSuccess { list ->
                    _uiState.update { it.copy(kiyaketler = list, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}

// ── Arkadaşlar Ekranı ──────────────────────────────────────────────────────────

data class FriendsUiState(
    val friends: List<FriendProfile>        = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean                  = false,
    val error: String?                      = null,
    val searchResult: FriendProfile?        = null,
    val searchError: String?                = null,
    val isSearching: Boolean                = false
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendService: FriendService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val friends  = friendService.getFriends().getOrElse { emptyList() }
            val requests = friendService.getPendingRequests().getOrElse { emptyList() }
            _uiState.update { it.copy(friends = friends, pendingRequests = requests, isLoading = false) }
        }
    }

    fun searchByEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchResult = null, searchError = null) }
            friendService.findUserByEmail(email)
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            isSearching  = false,
                            searchResult = profile,
                            searchError  = if (profile == null) "Bu e-posta ile kullanıcı bulunamadı." else null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSearching = false, searchError = e.message) }
                }
        }
    }

    fun sendRequest(toUid: String) {
        viewModelScope.launch {
            friendService.sendFriendRequest(toUid)
                .onSuccess {
                    _uiState.update { it.copy(searchResult = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(searchError = e.message) }
                }
        }
    }

    fun acceptRequest(requestId: String, fromUid: String) {
        viewModelScope.launch {
            friendService.acceptRequest(requestId, fromUid)
                .onSuccess { loadAll() }
        }
    }
}
