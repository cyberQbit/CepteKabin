package com.cyberqbit.ceptekabin.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.cyberqbit.ceptekabin.BuildConfig
import com.cyberqbit.ceptekabin.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val PREF_LATEST_VERSION_NAME = "latest_version_name"
        const val PREF_LATEST_VERSION_URL  = "latest_version_url"
        const val WORK_NAME = "update_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(24, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val request = Request.Builder()
                .url(Constants.GITHUB_RELEASES_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return Result.success()

            val body = response.body?.string() ?: return Result.success()
            val json = JSONObject(body)
            val tagName    = json.optString("tag_name", "")
            val htmlUrl    = json.optString("html_url", "")
            val prerelease = json.optBoolean("prerelease", false)

            if (tagName.isBlank() || prerelease) return Result.success()

            val prefs = applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(PREF_LATEST_VERSION_NAME, tagName)
                .putString(PREF_LATEST_VERSION_URL, htmlUrl)
                .apply()

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
