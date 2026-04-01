package com.cyberqbit.ceptekabin.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        return suspendCancellableCoroutine { continuation ->
            val cancellationToken = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val cityName = getCityName(location.latitude, location.longitude)
                    continuation.resume(
                        LocationResult.Success(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            cityName = cityName
                        )
                    )
                } else {
                    continuation.resume(LocationResult.Error("Konum alınamadı"))
                }
            }.addOnFailureListener { e ->
                continuation.resume(LocationResult.Error(e.message ?: "Konum hatası"))
            }

            continuation.invokeOnCancellation {
                cancellationToken.cancel()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("tr", "TR"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var result: String? = null
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    result = addresses.firstOrNull()?.let { addr ->
                        addr.adminArea?.replace("İl", "")?.trim()
                            ?: addr.subAdminArea?.trim()
                            ?: addr.locality?.trim()
                    }
                }
                result ?: "Bilinmeyen"
            } else {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                addresses?.firstOrNull()?.let { addr ->
                    addr.adminArea?.replace("İl", "")?.trim()
                        ?: addr.subAdminArea?.trim()
                        ?: addr.locality?.trim()
                } ?: "Bilinmeyen"
            }
        } catch (e: Exception) {
            "Bilinmeyen"
        }
    }

    sealed class LocationResult {
        data class Success(val latitude: Double, val longitude: Double, val cityName: String) : LocationResult()
        data class Error(val message: String) : LocationResult()
    }
}
