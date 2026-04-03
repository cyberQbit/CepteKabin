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
import com.cyberqbit.ceptekabin.domain.model.ForecastItem
import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.HavaDurumuDurum
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.model.Kombin
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

    private val _havaDurumu          = MutableStateFlow<HavaDurumu?>(null)
    val havaDurumu: StateFlow<HavaDurumu?> = _havaDurumu.asStateFlow()

    private val _cachedHavaDurumu    = MutableStateFlow<HavaDurumu?>(null)
    val cachedHavaDurumu: StateFlow<HavaDurumu?> = _cachedHavaDurumu.asStateFlow()

    private val _weatherLoadState    = MutableStateFlow(WeatherLoadState.IDLE)
    val weatherLoadState: StateFlow<WeatherLoadState> = _weatherLoadState.asStateFlow()

    // true = API yüklenirken cache'den gelen veri gösteriliyor
    private val _showingCachedWeather = MutableStateFlow(false)
    val showingCachedWeather: StateFlow<Boolean> = _showingCachedWeather.asStateFlow()

    private val _sonEklenenler       = MutableStateFlow<List<Kiyaket>>(emptyList())
    val sonEklenenler: StateFlow<List<Kiyaket>> = _sonEklenenler.asStateFlow()

    private val _onerilenKombinler   = MutableStateFlow<List<Kombin>>(emptyList())
    val onerilenKombinler: StateFlow<List<Kombin>> = _onerilenKombinler.asStateFlow()

    private val _isLoading           = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Eski field adlarıyla geriye uyumluluk
    val havaDurumuYukleniyor: StateFlow<Boolean> get() = _isLoading

    private val _sehirAdi            = MutableStateFlow<String?>(null)
    val sehirAdi: StateFlow<String?> = _sehirAdi.asStateFlow()

    private val _sonGuncelleme       = MutableStateFlow<String?>(null)
    val sonGuncelleme: StateFlow<String?> = _sonGuncelleme.asStateFlow()

    private val _konumIzniVerildi    = MutableStateFlow<Boolean?>(null)  // null=bilinmiyor
    val konumIzniVerildi: StateFlow<Boolean?> = _konumIzniVerildi.asStateFlow()

    private val _manuelSehir         = MutableStateFlow<String?>(null)
    val manuelSehir: StateFlow<String?> = _manuelSehir.asStateFlow()

    private val _showShareDialog     = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> = _showShareDialog.asStateFlow()

    private val gson = Gson()
    private var weatherJob: Job? = null
    private var weatherLoaded = false

    init {
        loadManuelSehir()
        loadSonEklenenler()
        loadAkilliOneriler()
        loadCachedWeather()
    }

    // ── Cache ─────────────────────────────────────────────────────────────────

    private fun loadCachedWeather() {
        viewModelScope.launch {
            val cache = weatherCacheDao.getCache() ?: return@launch
            _cachedHavaDurumu.value = cache.toHavaDurumu()
            _havaDurumu.value       = cache.toHavaDurumu()
            _showingCachedWeather.value = true
            val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale("tr", "TR"))
            val dateStr = sdf.format(Date(cache.kayitTarihi))
            _sonGuncelleme.value = "Son bilinen: $dateStr"
        }
    }

    private suspend fun saveWeatherToCache(hava: HavaDurumu) {
        val forecastJson = gson.toJson(hava.forecastList)
        weatherCacheDao.saveCache(
            WeatherCacheEntity(
                sehir       = hava.sehir,
                sicaklik    = hava.sicaklik,
                hissedilen  = hava.hissedilenSicaklik,
                durum       = hava.durum.name,
                nemOrani    = hava.nemOrani,
                ruzgarHizi  = hava.ruzgarHizi,
                forecastJson = forecastJson
            )
        )
    }

    private fun WeatherCacheEntity.toHavaDurumu(): HavaDurumu {
        val type = object : TypeToken<List<ForecastItem>>() {}.type
        val forecast: List<ForecastItem> = try {
            gson.fromJson(forecastJson, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
        return HavaDurumu(
            sehir               = sehir,
            sehirId             = sehir,
            sicaklik            = sicaklik,
            hissedilenSicaklik  = hissedilen,
            durum               = HavaDurumuDurum.valueOf(durum),
            aciklama            = HavaDurumuDurum.valueOf(durum).displayName,
            nemOrani            = nemOrani,
            ruzgarHizi          = ruzgarHizi,
            gunBatimi           = 0L,
            gunDogumu           = 0L,
            guncelTarih         = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(kayitTarihi)),
            forecastList        = forecast
        )
    }

    // ── Konum izni durumu ─────────────────────────────────────────────────────

    fun setKonumIzniDurumu(verildi: Boolean) {
        _konumIzniVerildi.value = verildi
    }

    // ── Manuel şehir ─────────────────────────────────────────────────────────

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

    // ── Yükleme fonksiyonları ─────────────────────────────────────────────────

    fun loadHavaDurumuWithLocation() {
        if (weatherLoaded && !_showingCachedWeather.value) return
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            _isLoading.value = true
            _weatherLoadState.value = if (_havaDurumu.value != null)
                WeatherLoadState.LOADING_FRESH else WeatherLoadState.LOADING_SKELETON
            try {
                when (val result = locationService.getCurrentLocation()) {
                    is LocationService.LocationResult.Success -> {
                        _sehirAdi.value = result.cityName
                        val city = normalizeTurkishCityName(result.cityName)
                        fetchWeather(city)
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
            ?: prefs.getString(Constants.PREF_LAST_CITY, null)
            ?: "Ankara"
    }

    private suspend fun fetchWeather(city: String) {
        havaDurumuRepository.getWeatherByCity(city)
            .onSuccess { hava ->
                _havaDurumu.value = hava
                _showingCachedWeather.value = false
                _weatherLoadState.value = WeatherLoadState.LOADED
                val sdf = SimpleDateFormat("HH.mm - dd/MM/yyyy", Locale("tr", "TR"))
                _sonGuncelleme.value = "Son güncelleme: ${sdf.format(Date())}"
                weatherLoaded = true
                saveWeatherToCache(hava)
            }
            .onFailure {
                if (_havaDurumu.value == null) {
                    _weatherLoadState.value = WeatherLoadState.ERROR
                }
            }
    }

    private fun normalizeTurkishCityName(sehir: String): String {
        val map = mapOf('İ' to 'I','ı' to 'i','Ğ' to 'G','ğ' to 'g',
            'Ü' to 'U','ü' to 'u','Ş' to 'S','ş' to 's','Ö' to 'O','ö' to 'o','Ç' to 'C','ç' to 'c')
        return sehir.map { map[it] ?: it }.joinToString("")
    }

    // ── Gardrırop verileri ────────────────────────────────────────────────────

    fun setKonumIzniGerekli(gerekli: Boolean) { /* geriye uyumluluk */ }

    private fun loadSonEklenenler() {
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { _sonEklenenler.value = it.take(5) }
        }
    }

    private fun loadAkilliOneriler() {
        viewModelScope.launch {
            combine(havaDurumu, kombinRepository.getAllKombinler()) { hava, tumKombinler ->
                if (tumKombinler.isEmpty()) emptyList()
                else if (hava == null) tumKombinler.shuffled().take(3)
                else {
                    val sicaklik = hava.sicaklik
                    val uygunlar = tumKombinler.filter { kombin ->
                        val turler = listOfNotNull(
                            kombin.ustGiyim, kombin.altGiyim, kombin.ayakkabi
                        ).map { it.tur.displayName }
                        when {
                            sicaklik >= 22.0 -> turler.any { it in listOf("Tişört","Şort","Etek","Crop Top") }
                                && turler.none { it in listOf("Kaban","Mont","Parka") }
                            sicaklik < 15.0  -> turler.any { it in listOf("Kaban","Mont","Parka","Kazak","Hırka","Sweatshirt") }
                            else             -> turler.any { it in listOf("Gömlek","Tişört","Pantolon","Ceket","Blazer") }
                        }
                    }
                    (if (uygunlar.isNotEmpty()) uygunlar else tumKombinler).shuffled().take(3)
                }
            }.collect { _onerilenKombinler.value = it }
        }
    }

    // ── Paylaşım teşviki ──────────────────────────────────────────────────────

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
