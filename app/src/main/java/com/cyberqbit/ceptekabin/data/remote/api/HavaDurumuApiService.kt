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
    companion object {
        const val BASE_URL = "https://mooweather-api.onrender.com/api/weather/"
    }

    suspend fun getWeatherByCity(city: String, lang: String = "tr"): Result<HavaDurumu> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${BASE_URL}${city}?lang=$lang")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error: ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            val json = JSONObject(body)

            val havaDurumu = parseWeatherResponse(json)
            Result.success(havaDurumu)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeatherByLocation(lat: Double, lon: Double, lang: String = "tr"): Result<HavaDurumu> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${BASE_URL}location?lat=$lat&lon=$lon&lang=$lang")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error: ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            val json = JSONObject(body)

            val havaDurumu = parseWeatherResponse(json)
            Result.success(havaDurumu)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseWeatherResponse(json: JSONObject): HavaDurumu {
        val name = json.optString("Name", "")
        val temp = json.optDouble("Temp", 0.0)
        val tempMax = json.optDouble("TempMax", 0.0)
        val tempMin = json.optDouble("TempMin", 0.0)
        val feelsLike = json.optDouble("FeelsLike", 0.0)
        val humidity = json.optInt("Humidity", 0)
        val pressure = json.optInt("Pressure", 0)
        val description = json.optString("Description", "")
        val windSpeed = json.optDouble("WindSpeed", 0.0)
        val visibility = json.optInt("Visibility", 0)
        val clouds = json.optInt("Clouds", 0)
        val sunrise = json.optLong("Sunrise", 0)
        val sunset = json.optLong("Sunset", 0)

        val durum = parseWeatherStatus(description, clouds)
        val icon = parseWeatherIcon(durum)

        val forecastList = parseForecast(json)

        return HavaDurumu(
            sehir = name,
            sehirId = name,
            sicaklik = temp,
            hissedilenSicaklik = feelsLike,
            durum = durum,
            aciklama = description,
            nemOrani = humidity,
            ruzgarHizi = windSpeed,
            gunBatimi = sunset,
            gunDogumu = sunrise,
            guncelTarih = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            forecastList = forecastList
        )
    }

    private fun parseWeatherStatus(description: String, clouds: Int): HavaDurumuDurum {
        val lowerDesc = description.lowercase()
        return when {
            lowerDesc.contains("güneş") || lowerDesc.contains("sunny") || lowerDesc.contains("clear") -> HavaDurumuDurum.GUNESLI
            lowerDesc.contains("bulut") || lowerDesc.contains("cloud") -> {
                if (clouds < 30) HavaDurumuDurum.AZ_BULUTLU
                else if (clouds < 70) HavaDurumuDurum.PARCALI_BULUTLU
                else HavaDurumuDurum.COK_BULUTLU
            }
            lowerDesc.contains("yağmur") || lowerDesc.contains("rain") -> HavaDurumuDurum.YAGMURLU
            lowerDesc.contains("kar") || lowerDesc.contains("snow") -> HavaDurumuDurum.KARLI
            lowerDesc.contains("fırtına") || lowerDesc.contains("storm") -> HavaDurumuDurum.FIRTINALI
            lowerDesc.contains("sis") || lowerDesc.contains("fog") -> HavaDurumuDurum.Sisli
            else -> HavaDurumuDurum.BILINMIYOR
        }
    }

    private fun parseWeatherIcon(durum: HavaDurumuDurum): String {
        return when (durum) {
            HavaDurumuDurum.GUNESLI -> "☀️"
            HavaDurumuDurum.AZ_BULUTLU -> "🌤️"
            HavaDurumuDurum.PARCALI_BULUTLU -> "⛅"
            HavaDurumuDurum.COK_BULUTLU -> "☁️"
            HavaDurumuDurum.YAGMURLU -> "🌧️"
            HavaDurumuDurum.YAGIS_HAKLI -> "🌦️"
            HavaDurumuDurum.KARLI -> "🌨️"
            HavaDurumuDurum.FIRTINALI -> "⛈️"
            HavaDurumuDurum.Sisli -> "🌫️"
            HavaDurumuDurum.RUZGARLI -> "💨"
            else -> "❓"
        }
    }

    private fun parseForecast(json: JSONObject): List<ForecastItem> {
        val forecast = json.optJSONArray("forecast") ?: return emptyList()
        val items = mutableListOf<ForecastItem>()

        for (i in 0 until minOf(forecast.length(), 5)) {
            try {
                val item = forecast.getJSONObject(i)
                val dateStr = item.optString("date", "")
                val dayName = item.optString("day", "")
                val minTemp = item.optDouble("temp_min", 0.0)
                val maxTemp = item.optDouble("temp_max", 0.0)
                val desc = item.optString("description", "")
                val humidity = item.optInt("humidity", 0)
                val wind = item.optDouble("wind", 0.0)
                val rain = item.optInt("rain", 0)

                items.add(
                    ForecastItem(
                        tarih = dateStr,
                        gun = dayName,
                        sicaklikMin = minTemp,
                        sicaklikMax = maxTemp,
                        durum = parseWeatherStatus(desc, 50),
                        nemOrani = humidity,
                        ruzgarHizi = wind,
                        yağışOlasılığı = rain
                    )
                )
            } catch (e: Exception) {
                continue
            }
        }

        return items
    }
}
