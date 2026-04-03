package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.local.database.dao.KombinDao
import com.cyberqbit.ceptekabin.data.local.database.dao.KombinKullanimDao
import com.cyberqbit.ceptekabin.data.local.database.entity.KombinKullanimEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class KullanimItem(
    val id: Long,
    val kombinId: Long,
    val kombinAd: String,
    val tarih: Long
)

data class KombinTakvimUiState(
    val aktivGunler: Set<Int>                   = emptySet(),
    val secilenGun: Long?                       = null,
    val secilenGunKombinler: List<KullanimItem> = emptyList(),
    val yukleniyor: Boolean                     = false
)

@HiltViewModel
class KombinTakvimViewModel @Inject constructor(
    private val kullanimDao: KombinKullanimDao,
    private val kombinDao: KombinDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(KombinTakvimUiState())
    val uiState: StateFlow<KombinTakvimUiState> = _uiState.asStateFlow()

    private var monthJob: kotlinx.coroutines.Job? = null

    fun loadMonth(year: Int, month: Int) {
        monthJob?.cancel()
        monthJob = viewModelScope.launch {
            _uiState.update { it.copy(yukleniyor = true) }

            val start = Calendar.getInstance().apply {
                set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val end = Calendar.getInstance().apply {
                set(year, month, 1, 23, 59, 59)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }.timeInMillis

            kullanimDao.getByDateRange(start, end).collect { list ->
                val aktifler = list.map { e ->
                    Calendar.getInstance().apply { timeInMillis = e.tarih }
                        .get(Calendar.DAY_OF_MONTH)
                }.toSet()
                _uiState.update { it.copy(aktivGunler = aktifler, yukleniyor = false) }
            }
        }
    }

    fun selectDay(dayMs: Long) {
        viewModelScope.launch {
            val dayStart = Calendar.getInstance().apply {
                timeInMillis = dayMs
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0);       set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + 86_399_999L

            kullanimDao.getByDateRange(dayStart, dayEnd)
                .map { list ->
                    list.mapNotNull { entity ->
                        val kombin = kombinDao.getById(entity.kombinId) ?: return@mapNotNull null
                        KullanimItem(entity.id, entity.kombinId, kombin.ad, entity.tarih)
                    }
                }
                .first()
                .also { items ->
                    _uiState.update { it.copy(secilenGun = dayMs, secilenGunKombinler = items) }
                }
        }
    }

    fun kaydetKullanim(kombinId: Long) {
        viewModelScope.launch {
            kullanimDao.insert(KombinKullanimEntity(kombinId = kombinId))
        }
    }
}
