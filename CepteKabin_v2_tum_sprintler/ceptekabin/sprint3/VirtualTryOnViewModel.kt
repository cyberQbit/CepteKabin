package com.cyberqbit.ceptekabin.ui.screens.tryon

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.domain.repository.KombinRepository
import com.cyberqbit.ceptekabin.util.LocalImageStorageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class TryOnUiState(
    val kombinAd: String?    = null,
    val garmentUrls: List<String> = emptyList(),
    val userPhotoUri: Uri?   = null,
    val resultUri: Uri?      = null,
    val isProcessing: Boolean = false,
    val error: String?       = null
)

@HiltViewModel
class VirtualTryOnViewModel @Inject constructor(
    private val kombinRepository: KombinRepository,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TryOnUiState())
    val uiState: StateFlow<TryOnUiState> = _uiState.asStateFlow()

    // IDM-VTON için daha uzun timeout — işlem 20-60s sürebilir
    private val tryOnClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun loadKombin(kombinId: Long) {
        viewModelScope.launch {
            val kombin = kombinRepository.getKombinById(kombinId) ?: return@launch
            val garmentUrls = listOfNotNull(
                kombin.ustGiyim?.imageUrl,
                kombin.altGiyim?.imageUrl,
                kombin.disGiyim?.imageUrl,
                kombin.ayakkabi?.imageUrl
            )
            _uiState.update {
                it.copy(kombinAd = kombin.ad, garmentUrls = garmentUrls)
            }
        }
    }

    fun setUserPhoto(uri: Uri) {
        _uiState.update { it.copy(userPhotoUri = uri, resultUri = null, error = null) }
    }

    fun clearPhoto() {
        _uiState.update { it.copy(userPhotoUri = null, resultUri = null, error = null) }
    }

    fun process() {
        val photoUri     = _uiState.value.userPhotoUri ?: return
        val garmentUrls  = _uiState.value.garmentUrls
        if (garmentUrls.isEmpty()) {
            _uiState.update { it.copy(error = "Kombinde görsel olan kıyafet bulunamadı.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null, resultUri = null) }
            try {
                // İlk uygun kıyafeti al (üst giyim öncelikli)
                val garmentUrl = garmentUrls.first()
                val resultBitmap = callTryOnApi(photoUri, garmentUrl)

                if (resultBitmap != null) {
                    val savedUri = LocalImageStorageHelper.saveBitmapToGallery(
                        context, resultBitmap,
                        "tryon_${System.currentTimeMillis()}"
                    )
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            resultUri    = savedUri?.let { Uri.parse(it) }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "İşlem tamamlanamadı. API yanıt vermedi."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, error = "Hata: ${e.message}")
                }
            }
        }
    }

    /**
     * HuggingFace IDM-VTON Gradio API.
     *
     * NOT: Bu endpoint halka açık demo API'sini kullanır.
     * Üretimde kendi HuggingFace Space'inizi deploy edin:
     *   https://huggingface.co/spaces/yisol/IDM-VTON
     *
     * Ücretsiz tier yavaş olabilir (cold start dahil 60-90s).
     */
    private suspend fun callTryOnApi(
        personUri: Uri,
        garmentUrl: String
    ): android.graphics.Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Kişi görselini oku
            val personStream = context.contentResolver.openInputStream(personUri)
            val personBitmap = BitmapFactory.decodeStream(personStream) ?: return@withContext null
            val personPng    = bitmapToBytes(personBitmap)

            // Kıyafet görselini indir
            val garmentResponse = tryOnClient.newCall(
                Request.Builder().url(garmentUrl).build()
            ).execute()
            val garmentBytes = garmentResponse.body?.bytes() ?: return@withContext null

            // Gradio predict endpoint (IDM-VTON Space)
            val GRADIO_URL = "https://yisol-idm-vton.hf.space/run/predict"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("person_img", "person.png",
                    personPng.toRequestBody("image/png".toMediaType()))
                .addFormDataPart("garment_img", "garment.png",
                    garmentBytes.toRequestBody("image/png".toMediaType()))
                .addFormDataPart("is_checked", "true")
                .addFormDataPart("is_checked_crop", "false")
                .addFormDataPart("denoise_steps", "30")
                .addFormDataPart("seed", "42")
                .build()

            val request = Request.Builder()
                .url(GRADIO_URL)
                .post(requestBody)
                .build()

            val response = tryOnClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)

            // Gradio yanıtı: { "data": ["<base64 veya URL>"] }
            val data = json.optJSONArray("data")
            val resultValue = data?.optString(0) ?: return@withContext null

            // Base64 ise decode et
            if (resultValue.startsWith("data:image")) {
                val base64 = resultValue.substringAfter(",")
                val bytes  = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else {
                // URL ise indir
                val imgResponse = tryOnClient.newCall(
                    Request.Builder().url(resultValue).build()
                ).execute()
                val imgBytes = imgResponse.body?.bytes() ?: return@withContext null
                BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun bitmapToBytes(bitmap: android.graphics.Bitmap): ByteArray {
        val bos = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, bos)
        return bos.toByteArray()
    }
}
