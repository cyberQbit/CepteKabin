package com.cyberqbit.ceptekabin.ui.screens.kombin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.local.database.dao.TakvimGirisiDao
import com.cyberqbit.ceptekabin.data.local.database.entity.TakvimGirisiEntity
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class KombinTakvimViewModel @Inject constructor(
    private val takvimDao: TakvimGirisiDao,
    private val kombinRepository: KombinRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(getMidnightTimestamp(System.currentTimeMillis()))
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _gunlukGirisler = MutableStateFlow<List<TakvimGirisiEntity>>(emptyList())
    val gunlukGirisler: StateFlow<List<TakvimGirisiEntity>> = _gunlukGirisler.asStateFlow()

    private val _tumKombinler = MutableStateFlow<List<Kombin>>(emptyList())
    val tumKombinler: StateFlow<List<Kombin>> = _tumKombinler.asStateFlow()

    init {
        loadKombinler()
        loadGirislerForDate(_selectedDate.value)
    }

    fun setSelectedDate(timestamp: Long) {
        val midnight = getMidnightTimestamp(timestamp)
        _selectedDate.value = midnight
        loadGirislerForDate(midnight)
    }

    private fun loadKombinler() {
        viewModelScope.launch {
            kombinRepository.getAllKombinler().collect { _tumKombinler.value = it }
        }
    }

    private fun loadGirislerForDate(timestamp: Long) {
        viewModelScope.launch {
            _gunlukGirisler.value = takvimDao.getGirislerForGun(timestamp)
        }
    }

    fun addKombinToDate(kombin: Kombin) {
        viewModelScope.launch {
            val currentGirisler = takvimDao.getGirislerForGun(_selectedDate.value)
            if (currentGirisler.size < 3) {
                val slot = currentGirisler.size
                val newGiris = TakvimGirisiEntity(
                    tarihGun = _selectedDate.value,
                    slot = slot,
                    kombinId = kombin.id,
                    kombinAd = kombin.ad,
                    ustGiyimAd = kombin.ustGiyim?.let { "${it.marka} ${it.tur.displayName}" },
                    altGiyimAd = kombin.altGiyim?.let { "${it.marka} ${it.tur.displayName}" },
                    disGiyimAd = kombin.disGiyim?.let { "${it.marka} ${it.tur.displayName}" },
                    ayakkabiAd = kombin.ayakkabi?.let { "${it.marka} ${it.tur.displayName}" },
                    aksesuarAd = kombin.aksesuar?.let { "${it.marka} ${it.tur.displayName}" },
                    ustGiyimResim = kombin.ustGiyim?.imageUrl,
                    altGiyimResim = kombin.altGiyim?.imageUrl,
                    disGiyimResim = kombin.disGiyim?.imageUrl,
                    ayakkabiResim = kombin.ayakkabi?.imageUrl,
                    aksesuarResim = kombin.aksesuar?.imageUrl
                )
                takvimDao.insert(newGiris)
                loadGirislerForDate(_selectedDate.value)
            }
        }
    }

    fun removeTakvimGirisi(giris: TakvimGirisiEntity) {
        viewModelScope.launch {
            takvimDao.delete(giris)
            loadGirislerForDate(_selectedDate.value)
        }
    }

    private fun getMidnightTimestamp(timeInMillis: Long): Long {
        return Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
