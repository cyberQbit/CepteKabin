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

    private val _konumIzniGerekli = MutableStateFlow(false)
    val konumIzniGerekli: StateFlow<Boolean> = _konumIzniGerekli.asStateFlow()

    private val _sehirAdi = MutableStateFlow<String?>(null)
    val sehirAdi: StateFlow<String?> = _sehirAdi.asStateFlow()

    init {
        loadSonEklenenler()
        loadFavoriKombinler()
    }

    fun loadHavaDurumuWithLocation() {
        viewModelScope.launch {
            _havaDurumuYukleniyor.value = true

            when (val result = locationService.getCurrentLocation()) {
                is LocationService.LocationResult.Success -> {
                    _sehirAdi.value = result.cityName
                    loadHavaDurumuByCity(result.cityName)
                }
                is LocationService.LocationResult.Error -> {
                    // Konum alınamazsa varsayılan şehir
                    loadHavaDurumuByCity("Istanbul")
                }
            }

            _havaDurumuYukleniyor.value = false
        }
    }

    fun loadHavaDurumuByCity(sehir: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val normalizedSehir = normalizeTurkishCityName(sehir)
            _sehirAdi.value = sehir

            havaDurumuRepository.getWeatherByCity(normalizedSehir)
                .onSuccess { _havaDurumu.value = it }
                .onFailure {
                    // Yedek olarak Istanbul dene
                    if (normalizedSehir != "Istanbul") {
                        loadHavaDurumuByCity("Istanbul")
                    }
                }

            _isLoading.value = false
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
