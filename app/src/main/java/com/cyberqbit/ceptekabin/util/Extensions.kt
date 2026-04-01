package com.cyberqbit.ceptekabin.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale("tr", "TR"))
    return sdf.format(Date(this))
}

fun Long.toFormattedTime(pattern: String = "HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale("tr", "TR"))
    return sdf.format(Date(this))
}

fun String.toTurkishUpperCase(): String = this.uppercase(Locale("tr", "TR"))
fun Double.toCelsiusString(): String = "${this.toInt()}°C"
fun Int.toYağışOlasılığıString(): String = "%$this"

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Int.sicakligaGoreKatmanSayisi(): Int = when {
    this < 5 -> 4
    this < 15 -> 3
    this < 22 -> 2
    else -> 1
}
