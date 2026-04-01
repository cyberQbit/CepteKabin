package com.cyberqbit.ceptekabin.ui.screens.home

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.service.LocationService
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
import com.cyberqbit.ceptekabin.domain.repository.HavaDurumuRepository
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import com.cyberqbit.ceptekabin.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val locationService: LocationService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _havaDurumu = MutableStateFlow<HavaDurumu?>(null)
    val havaDurumu: StateFlow<HavaDurumu?> = _havaDurumu.asStateFlow()

    private val _sonEklenenler = MutableStateFlow<List<Kiyaket>>(emptyList())
    val sonEklenenler: StateFlow<List<Kiyaket>> = _sonEklenenler.asStateFlow()

    private val _onerilenKombinler = MutableStateFlow<List<Kombin>>(emptyList())
    val onerilenKombinler: StateFlow<List<Kombin>> = _onerilenKombinler.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _havaDurumuYukleniyor = MutableStateFlow(false)
    val havaDurumuYukleniyor: StateFlow<Boolean> = _havaDurumuYukleniyor.asStateFlow()

    private val _sehirAdi = MutableStateFlow<String?>(null)
    val sehirAdi: StateFlow<String?> = _sehirAdi.asStateFlow()

    private val _sonGuncelleme = MutableStateFlow<String?>(null)
    val sonGuncelleme: StateFlow<String?> = _sonGuncelleme.asStateFlow()

    private val _konumIzniGerekli = MutableStateFlow(false)
    val konumIzniGerekli: StateFlow<Boolean> = _konumIzniGerekli.asStateFlow()

    // Track if we already started a weather load to avoid duplicate calls
    private var weatherLoadJob: Job? = null
    private var weatherLoaded = false

    init {
        loadSonEklenenler()
        loadAkilliOneriler()
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
                        fetchWeather("Ankara")
                    }
                }
            } catch (e: Exception) {
                fetchWeather("Ankara")
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
                val sdf = SimpleDateFormat("HH.mm - dd/MM/yyyy", Locale("tr", "TR"))
                _sonGuncelleme.value = "Son Güncelleme: ${sdf.format(Date())}"
                weatherLoaded = true
            }
            .onFailure {
                if (city != "Ankara") {
                    havaDurumuRepository.getWeatherByCity("Ankara")
                        .onSuccess { 
                            _havaDurumu.value = it
                            val sdf = SimpleDateFormat("HH.mm - dd/MM/yyyy", Locale("tr", "TR"))
                            _sonGuncelleme.value = "Son Güncelleme: ${sdf.format(Date())}"
                            weatherLoaded = true
                        }
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

    private fun loadAkilliOneriler() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(havaDurumu, kombinRepository.getAllKombinler()) { hava, tumKombinler ->
                if (tumKombinler.isEmpty()) {
                    emptyList()
                } else if (hava == null) {
                    tumKombinler.shuffled().take(3)
                } else {
                    val sicaklik = hava.sicaklik

                    val uygunKombinler = tumKombinler.filter { kombin ->
                        val parcalar = listOfNotNull(kombin.ustGiyim, kombin.altGiyim, kombin.ayakkabi)
                        val turler = parcalar.map { it.tur.displayName }

                        when {
                            sicaklik >= 22.0 -> turler.any { it == "Tişört" || it == "Şort" || it == "Etek" } && !turler.contains("Kaban / Mont")
                            sicaklik < 15.0 -> turler.any { it == "Kaban / Mont" || it == "Kazak" || it == "Hırka" || it == "Sweatshirt" }
                            else -> turler.any { it == "Gömlek" || it == "Tişört" || it == "Pantolon" || it == "Ceket" }
                        }
                    }

                    if (uygunKombinler.isNotEmpty()) {
                        uygunKombinler.shuffled().take(3)
                    } else {
                        tumKombinler.shuffled().take(3)
                    }
                }
            }.collect { oneriler ->
                _onerilenKombinler.value = oneriler
            }
        }
    }

    // ─── ORGANIK BÜYÜME: PAYLAŞIM TEŞVİK SİSTEMİ ───────────────────────────────

    private val _showShareDialog = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> = _showShareDialog.asStateFlow()

    fun checkAndShowSharePrompt() {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val wasShownLastTime = prefs.getBoolean("share_shown_last_time", false)

        if (wasShownLastTime) {
            // Arka arkaya gelmemesi için bu sefer gösterme ve durumu sıfırla
            prefs.edit().putBoolean("share_shown_last_time", false).apply()
        } else {
            // Gösterilmediyse zarı at (%30 ihtimalle gösterelim)
            val randomChance = (1..100).random()
            if (randomChance <= 30) {
                _showShareDialog.value = true
                prefs.edit().putBoolean("share_shown_last_time", true).apply()
            } else {
                prefs.edit().putBoolean("share_shown_last_time", false).apply()
            }
        }
    }

    fun dismissShareDialog() {
        _showShareDialog.value = false
    }
}
