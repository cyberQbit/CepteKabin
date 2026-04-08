package com.cyberqbit.ceptekabin.ui.screens.home

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.local.database.dao.WeatherCacheDao
import com.cyberqbit.ceptekabin.data.local.database.entity.WeatherCacheEntity
import com.cyberqbit.ceptekabin.data.service.LocationService
import com.cyberqbit.ceptekabin.domain.engine.SmartKombinSuggester
import com.cyberqbit.ceptekabin.domain.engine.WeatherOutfitEngine
import com.cyberqbit.ceptekabin.domain.model.ForecastItem
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.HavaDurumuDurum
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.repository.HavaDurumuRepository
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import com.cyberqbit.ceptekabin.util.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WeatherLoadState { IDLE, LOADING_SKELETON, LOADING_FRESH, LOADED, ERROR }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val havaDurumuRepository: HavaDurumuRepository,
    private val kiyaketRepository: KiyaketRepository,
    private val kombinRepository: KombinRepository,
    private val locationService: LocationService,
    private val weatherCacheDao: WeatherCacheDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _havaDurumu = MutableStateFlow<HavaDurumu?>(null)
    val havaDurumu: StateFlow<HavaDurumu?> = _havaDurumu.asStateFlow()

    private val _cachedHavaDurumu = MutableStateFlow<HavaDurumu?>(null)
    val cachedHavaDurumu: StateFlow<HavaDurumu?> = _cachedHavaDurumu.asStateFlow()

    private val _weatherLoadState = MutableStateFlow(WeatherLoadState.IDLE)
    val weatherLoadState: StateFlow<WeatherLoadState> = _weatherLoadState.asStateFlow()

    private val _showingCachedWeather = MutableStateFlow(false)
    val showingCachedWeather: StateFlow<Boolean> = _showingCachedWeather.asStateFlow()

    private val _sonEklenenler = MutableStateFlow<List<Kiyaket>>(emptyList())
    val sonEklenenler: StateFlow<List<Kiyaket>> = _sonEklenenler.asStateFlow()

    private val _onerilenKombinler = MutableStateFlow<List<SmartKombinSuggester.KombinOnerisi>>(emptyList())
    val onerilenKombinler: StateFlow<List<SmartKombinSuggester.KombinOnerisi>> = _onerilenKombinler.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val havaDurumuYukleniyor: StateFlow<Boolean> get() = _isLoading

    private val _dolapIstatistikleri = MutableStateFlow(DolapIstatistikleri())
    val dolapIstatistikleri: StateFlow<DolapIstatistikleri> = _dolapIstatistikleri.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _sehirAdi = MutableStateFlow<String?>(null)
    val sehirAdi: StateFlow<String?> = _sehirAdi.asStateFlow()

    private val _sonGuncelleme = MutableStateFlow<String?>(null)
    val sonGuncelleme: StateFlow<String?> = _sonGuncelleme.asStateFlow()

    private val _konumIzniVerildi = MutableStateFlow<Boolean?>(null)
    val konumIzniVerildi: StateFlow<Boolean?> = _konumIzniVerildi.asStateFlow()

    private val _manuelSehir = MutableStateFlow<String?>(null)
    val manuelSehir: StateFlow<String?> = _manuelSehir.asStateFlow()

    private val _showShareDialog = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> = _showShareDialog.asStateFlow()

    private val gson = Gson()
    private var weatherJob: Job? = null

    init {
        loadUserName()
        loadManuelSehir()
        loadDolapVerileri()
        loadCachedWeather()
    }

    private fun loadUserName() {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        _userName.value = prefs.getString(Constants.PREF_USER_NAME, null)
    }

    private fun loadDolapVerileri() {
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { tumKiyafetler ->
                _sonEklenenler.value = tumKiyafetler
                    .sortedByDescending { it.eklenmeTarihi }
                    .take(10)

                val kategoriler = tumKiyafetler.groupBy { it.kategori }.mapValues { it.value.size }
                _dolapIstatistikleri.value = DolapIstatistikleri(
                    toplamKiyafet = tumKiyafetler.size,
                    toplamKombin = _dolapIstatistikleri.value.toplamKombin,
                    kategoriler = kategoriler
                )

                _havaDurumu.value?.let { hava -> guncelleOneriler(tumKiyafetler, hava) }
            }
        }
        viewModelScope.launch {
            kombinRepository.getAllKombinler().collect { kombinler ->
                _dolapIstatistikleri.update { it.copy(toplamKombin = kombinler.size) }
            }
        }
    }

    private fun guncelleOneriler(kiyafetler: List<Kiyaket>, havaDurumu: HavaDurumu) {
        viewModelScope.launch {
            val oneriler = SmartKombinSuggester.onerilerUret(kiyafetler, havaDurumu, 3)
            _onerilenKombinler.value = oneriler
        }
    }

    // Cache
    private fun loadCachedWeather() {
        viewModelScope.launch {
            val cache = weatherCacheDao.getCache() ?: return@launch
            _cachedHavaDurumu.value = cache.toHavaDurumu()
            _havaDurumu.value = cache.toHavaDurumu()
            _showingCachedWeather.value = true
            val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale("tr", "TR"))
            _sonGuncelleme.value = "Son bilinen: ${sdf.format(Date(cache.kayitTarihi))}"
        }
    }

    private suspend fun saveWeatherToCache(hava: HavaDurumu) {
        val forecastJson = gson.toJson(hava.forecastList)
        weatherCacheDao.saveCache(WeatherCacheEntity(
            sehir = hava.sehir, sicaklik = hava.sicaklik, hissedilen = hava.hissedilenSicaklik,
            durum = hava.durum.name, nemOrani = hava.nemOrani, ruzgarHizi = hava.ruzgarHizi,
            forecastJson = forecastJson
        ))
    }

    private fun WeatherCacheEntity.toHavaDurumu(): HavaDurumu {
        val type = object : TypeToken<List<ForecastItem>>() {}.type
        val forecast: List<ForecastItem> = try { gson.fromJson(forecastJson, type) ?: emptyList() } catch (_: Exception) { emptyList() }
        return HavaDurumu(
            sehir = sehir, sehirId = sehir, sicaklik = sicaklik, hissedilenSicaklik = hissedilen,
            durum = HavaDurumuDurum.valueOf(durum), aciklama = HavaDurumuDurum.valueOf(durum).displayName,
            nemOrani = nemOrani, ruzgarHizi = ruzgarHizi, gunBatimi = 0L, gunDogumu = 0L,
            guncelTarih = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(kayitTarihi)),
            forecastList = forecast
        )
    }

    fun setKonumIzniDurumu(verildi: Boolean) { _konumIzniVerildi.value = verildi }

    private fun loadManuelSehir() {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        _manuelSehir.value = prefs.getString(Constants.PREF_MANUAL_CITY, null)
    }

    fun setManuelSehir(sehir: String) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(Constants.PREF_MANUAL_CITY, sehir).apply()
        _manuelSehir.value = sehir
        loadHavaDurumuByCity(sehir)
    }

    fun loadHavaDurumuWithLocation() {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            _isLoading.value = true
            _weatherLoadState.value = if (_havaDurumu.value != null)
                WeatherLoadState.LOADING_FRESH else WeatherLoadState.LOADING_SKELETON
            try {
                when (val result = locationService.getCurrentLocation()) {
                    is LocationService.LocationResult.Success -> {
                        _sehirAdi.value = result.cityName
                        fetchWeather(normalizeTurkishCityName(result.cityName))
                    }
                    is LocationService.LocationResult.Error -> fetchWeather(getDefaultCity())
                }
            } catch (_: Exception) { fetchWeather(getDefaultCity()) }
            _isLoading.value = false
        }
    }

    fun loadHavaDurumuByCity(sehir: String) {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            _isLoading.value = true
            _weatherLoadState.value = if (_havaDurumu.value != null)
                WeatherLoadState.LOADING_FRESH else WeatherLoadState.LOADING_SKELETON
            _sehirAdi.value = sehir
            fetchWeather(normalizeTurkishCityName(sehir))
            _isLoading.value = false
        }
    }

    private fun getDefaultCity(): String {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(Constants.PREF_MANUAL_CITY, null)
            ?: prefs.getString(Constants.PREF_LAST_CITY, null) ?: "Ankara"
    }

    private suspend fun fetchWeather(city: String) {
        havaDurumuRepository.getWeatherByCity(city)
            .onSuccess { hava ->
                _havaDurumu.value = hava
                _showingCachedWeather.value = false
                _weatherLoadState.value = WeatherLoadState.LOADED
                val sdf = SimpleDateFormat("HH.mm - dd/MM/yyyy", Locale("tr", "TR"))
                _sonGuncelleme.value = "Son güncelleme: ${sdf.format(Date())}"
                saveWeatherToCache(hava)
                val kiyafetler = _sonEklenenler.value
                if (kiyafetler.isNotEmpty()) guncelleOneriler(kiyafetler, hava)
            }
            .onFailure {
                if (_havaDurumu.value == null) _weatherLoadState.value = WeatherLoadState.ERROR
            }
    }

    private fun normalizeTurkishCityName(sehir: String): String {
        val map = mapOf('İ' to 'I','ı' to 'i','Ğ' to 'G','ğ' to 'g',
            'Ü' to 'U','ü' to 'u','Ş' to 'S','ş' to 's','Ö' to 'O','ö' to 'o','Ç' to 'C','ç' to 'c')
        return sehir.map { map[it] ?: it }.joinToString("")
    }

    fun setKonumIzniGerekli(gerekli: Boolean) { /* geriye uyumluluk */ }

    fun checkAndShowSharePrompt() {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val wasShown = prefs.getBoolean("share_shown_last_time", false)
        if (wasShown) {
            prefs.edit().putBoolean("share_shown_last_time", false).apply()
        } else {
            if ((1..100).random() <= 30) {
                _showShareDialog.value = true
                prefs.edit().putBoolean("share_shown_last_time", true).apply()
            }
        }
    }

    fun dismissShareDialog() { _showShareDialog.value = false }
}
