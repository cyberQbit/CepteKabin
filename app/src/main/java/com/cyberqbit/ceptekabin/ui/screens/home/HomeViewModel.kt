package com.cyberqbit.ceptekabin.ui.screens.home

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.service.LocationService
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.domain.repository.HavaDurumuRepository
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val havaDurumuRepository: HavaDurumuRepository,
    private val kiyaketRepository: KiyaketRepository,
    private val kombinRepository: KombinRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _havaDurumu = MutableStateFlow<HavaDurumu?>(null)
    val havaDurumu: StateFlow<HavaDurumu?> = _havaDurumu.asStateFlow()

    private val _sonEklenenler = MutableStateFlow<List<Kiyaket>>(emptyList())
    val sonEklenenler: StateFlow<List<Kiyaket>> = _sonEklenenler.asStateFlow()

    private val _favoriKombinler = MutableStateFlow<List<Kombin>>(emptyList())
    val favoriKombinler: StateFlow<List<Kombin>> = _favoriKombinler.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _havaDurumuYukleniyor = MutableStateFlow(false)
    val havaDurumuYukleniyor: StateFlow<Boolean> = _havaDurumuYukleniyor.asStateFlow()

    private val _sehirAdi = MutableStateFlow<String?>(null)
    val sehirAdi: StateFlow<String?> = _sehirAdi.asStateFlow()

    private val _konumIzniGerekli = MutableStateFlow(false)
    val konumIzniGerekli: StateFlow<Boolean> = _konumIzniGerekli.asStateFlow()

    // Track if we already started a weather load to avoid duplicate calls
    private var weatherLoadJob: Job? = null
    private var weatherLoaded = false

    init {
        loadSonEklenenler()
        loadFavoriKombinler()
    }

    // FIX: guard against duplicate loads; use a single coroutine job
    fun loadHavaDurumuWithLocation() {
        if (weatherLoaded && _havaDurumu.value != null) return
        weatherLoadJob?.cancel()
        weatherLoadJob = viewModelScope.launch {
            _havaDurumuYukleniyor.value = true
            try {
                when (val result = locationService.getCurrentLocation()) {
                    is LocationService.LocationResult.Success -> {
                        val city = normalizeTurkishCityName(result.cityName)
                        _sehirAdi.value = result.cityName
                        fetchWeather(city)
                    }
                    is LocationService.LocationResult.Error -> {
                        // Konum alınamazsa İstanbul ile devam et
                        fetchWeather("Istanbul")
                    }
                }
            } catch (e: Exception) {
                fetchWeather("Istanbul")
            } finally {
                _havaDurumuYukleniyor.value = false
            }
        }
    }

    fun loadHavaDurumuByCity(sehir: String) {
        weatherLoadJob?.cancel()
        weatherLoadJob = viewModelScope.launch {
            _havaDurumuYukleniyor.value = true
            val normalized = normalizeTurkishCityName(sehir)
            _sehirAdi.value = sehir
            fetchWeather(normalized)
            _havaDurumuYukleniyor.value = false
        }
    }

    private suspend fun fetchWeather(city: String) {
        havaDurumuRepository.getWeatherByCity(city)
            .onSuccess {
                _havaDurumu.value = it
                weatherLoaded = true
            }
            .onFailure {
                if (city != "Istanbul") {
                    havaDurumuRepository.getWeatherByCity("Istanbul")
                        .onSuccess { _havaDurumu.value = it }
                }
            }
    }

    private fun normalizeTurkishCityName(sehir: String): String {
        val trMap = mapOf(
            'İ' to 'I', 'ı' to 'i',
            'Ğ' to 'G', 'ğ' to 'g',
            'Ü' to 'U', 'ü' to 'u',
            'Ş' to 'S', 'ş' to 's',
            'Ö' to 'O', 'ö' to 'o',
            'Ç' to 'C', 'ç' to 'c'
        )
        return sehir.map { trMap[it] ?: it }.joinToString("")
    }

    fun setKonumIzniGerekli(gerekli: Boolean) {
        _konumIzniGerekli.value = gerekli
    }

    private fun loadSonEklenenler() {
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { kiyaketler ->
                _sonEklenenler.value = kiyaketler.take(5)
            }
        }
    }

    private fun loadFavoriKombinler() {
        viewModelScope.launch {
            kombinRepository.getFavoriKombinler().collect { kombinler ->
                _favoriKombinler.value = kombinler
            }
        }
    }
}
