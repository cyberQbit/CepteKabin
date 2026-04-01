package com.cyberqbit.ceptekabin.data.remote.api

import com.cyberqbit.ceptekabin.domain.model.HavaDurumu
import com.cyberqbit.ceptekabin.domain.model.HavaDurumuDurum
import com.cyberqbit.ceptekabin.domain.model.ForecastItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class HavaDurumuApiService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun getWeatherByCity(city: String, lang: String = "tr"): Result<HavaDurumu> = withContext(Dispatchers.IO) {
        try {
            val geocodeReq = Request.Builder()
                .url("https://geocoding-api.open-meteo.com/v1/search?name=$city&language=tr&count=1")
                .get()
                .build()
            
            val geoResp = okHttpClient.newCall(geocodeReq).execute()
            if (!geoResp.isSuccessful) return@withContext Result.failure(Exception("Geocoding Error"))
            
            val geoBody = geoResp.body?.string() ?: return@withContext Result.failure(Exception("Empty Response"))
            val geoJson = JSONObject(geoBody)
            val results = geoJson.optJSONArray("results") ?: return@withContext Result.failure(Exception("Şehir bulunamadı"))
            
            val first = results.getJSONObject(0)
            val lat = first.getDouble("latitude")
            val lon = first.getDouble("longitude")
            val name = first.getString("name")
            
            getWeatherFromOpenMeteo(lat, lon, name)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeatherByLocation(lat: Double, lon: Double, lang: String = "tr"): Result<HavaDurumu> = withContext(Dispatchers.IO) {
        try {
            val result = getWeatherFromOpenMeteo(lat, lon, "Konumunuz")
            result
        } catch(e:Exception){ Result.failure(e) }
    }
    
    private fun getWeatherFromOpenMeteo(lat: Double, lon: Double, cityName: String): Result<HavaDurumu> {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
        val req = Request.Builder().url(url).get().build()
        val resp = okHttpClient.newCall(req).execute()
        if (!resp.isSuccessful) return Result.failure(Exception("API Error"))
        
        val body = resp.body?.string() ?: return Result.failure(Exception("Empty Response"))
        val json = JSONObject(body)
        val current = json.getJSONObject("current")
        val daily = json.getJSONObject("daily")
        
        val temp = current.getDouble("temperature_2m")
        val hum = current.getInt("relative_humidity_2m")
        val appTemp = current.getDouble("apparent_temperature")
        val wind = current.getDouble("wind_speed_10m")
        val code = current.getInt("weather_code")
        
        val durum = wmoToDurum(code)
        
        val dates = daily.getJSONArray("time")
        val maxTemps = daily.getJSONArray("temperature_2m_max")
        val minTemps = daily.getJSONArray("temperature_2m_min")
        val codes = daily.getJSONArray("weather_code")
        
        val forecasts = mutableListOf<ForecastItem>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale("tr"))
        
        for (i in 0 until Math.min(5, dates.length())) {
            val dateStr = dates.getString(i)
            val d = sdf.parse(dateStr)
            val gun = if (d != null) dayFormat.format(d) else ""
            forecasts.add(
                ForecastItem(
                    tarih = dateStr,
                    gun = gun,
                    sicaklikMin = minTemps.getDouble(i),
                    sicaklikMax = maxTemps.getDouble(i),
                    durum = wmoToDurum(codes.getInt(i)),
                    nemOrani = 0,
                    ruzgarHizi = 0.0,
                    yağışOlasılığı = 0
                )
            )
        }
        
        val havaDurumu = HavaDurumu(
            sehir = cityName,
            sehirId = cityName,
            sicaklik = temp,
            hissedilenSicaklik = appTemp,
            durum = durum,
            aciklama = durum.displayName,
            nemOrani = hum,
            ruzgarHizi = wind,
            gunBatimi = 0L,
            gunDogumu = 0L,
            guncelTarih = sdf.format(Date()),
            forecastList = forecasts
        )
        return Result.success(havaDurumu)
    }

    private fun wmoToDurum(code: Int): HavaDurumuDurum {
        return HavaDurumuDurum.fromIconAndCode("", code)
    }
}
