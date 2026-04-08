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
            kombinRepository.getAllKombinler().collect { kombinler ->
                _tumKombinler.value = kombinler
            }
        }
    }

    private fun loadGirislerForDate(timestamp: Long) {
        viewModelScope.launch {
            _gunlukGirisler.value = takvimDao.getGirislerForGun(timestamp)
        }
    }

    fun addKombinToDate(kombin: Kombin, ogun: String = "GÜNLÜK") {
        viewModelScope.launch {
            val currentGirisler = takvimDao.getGirislerForGun(_selectedDate.value)
            if (currentGirisler.size < 3) {
                // Kombindeki parçaların resim url'lerini toplayıp virgülle ayırarak kombinGorselleri alanına yazıyoruz
                val imageUrls = listOfNotNull(
                    kombin.ustGiyim?.imageUrl,
                    kombin.altGiyim?.imageUrl,
                    kombin.disGiyim?.imageUrl,
                    kombin.ayakkabi?.imageUrl,
                    kombin.aksesuar?.imageUrl
                ).joinToString(",")
                
                val newGiris = TakvimGirisiEntity(
                    tarihGunu = _selectedDate.value,
                    ogun = ogun,
                    kombinId = kombin.id,
                    kombinAd = kombin.ad,
                    kombinGorselleri = imageUrls
                )
                takvimDao.insertTakvimGirisi(newGiris)
                loadGirislerForDate(_selectedDate.value)
            }
        }
    }

    fun removeTakvimGirisi(giris: TakvimGirisiEntity) {
        viewModelScope.launch {
            takvimDao.deleteTakvimGirisi(giris)
            loadGirislerForDate(_selectedDate.value)
        }
    }

    private fun getMidnightTimestamp(timeInMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
