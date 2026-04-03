package com.cyberqbit.ceptekabin.ui.screens.chatbot

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.domain.model.Kiyaket
import com.cyberqbit.ceptekabin.domain.repository.KiyaketRepository
import com.cyberqbit.ceptekabin.ui.screens.home.WeatherLoadState
import com.cyberqbit.ceptekabin.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

enum class ChatRole { USER, ASSISTANT, SYSTEM }

data class ChatMessage(val role: ChatRole, val content: String)

data class ChatbotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean          = false,
    val apiKey: String              = "",
    val error: String?              = null
)

@HiltViewModel
class StyleChatbotViewModel @Inject constructor(
    private val kiyaketRepository: KiyaketRepository,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    private var allKiyaketler: List<Kiyaket> = emptyList()

    init {
        loadApiKey()
        viewModelScope.launch {
            kiyaketRepository.getAllKiyaketler().collect { allKiyaketler = it }
        }
    }

    private fun loadApiKey() {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val key   = prefs.getString("openai_api_key", null) ?: ""
        _uiState.update { it.copy(apiKey = key) }
    }

    fun setApiKey(key: String) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("openai_api_key", key).apply()
        _uiState.update { it.copy(apiKey = key) }
    }

    fun sendMessage(text: String) {
        val key = _uiState.value.apiKey
        if (key.isBlank()) {
            _uiState.update { it.copy(error = "Önce API anahtarı ayarlayın.") }
            return
        }

        val userMsg = ChatMessage(ChatRole.USER, text)
        _uiState.update { it.copy(messages = it.messages + userMsg, isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val systemPrompt = buildSystemPrompt()
                val history      = buildApiHistory(systemPrompt)
                val reply        = callOpenAI(key, history)
                val assistantMsg = ChatMessage(ChatRole.ASSISTANT, reply)
                _uiState.update { it.copy(messages = it.messages + assistantMsg, isLoading = false) }
            } catch (e: Exception) {
                val errMsg = ChatMessage(ChatRole.ASSISTANT,
                    "Üzgünüm, bir hata oluştu: ${e.message ?: "Bilinmeyen hata"}")
                _uiState.update { it.copy(messages = it.messages + errMsg, isLoading = false) }
            }
        }
    }

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList(), error = null) }
    }

    // ── OpenAI API çağrısı ────────────────────────────────────────────────────

    private suspend fun callOpenAI(apiKey: String, messages: JSONArray): String {
        val body = JSONObject().apply {
            put("model", "gpt-4o-mini")  // Düşük maliyet, hızlı
            put("messages", messages)
            put("max_tokens", 600)
            put("temperature", 0.7)
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            throw Exception("API Hatası ${response.code}: $errBody")
        }

        val json   = JSONObject(response.body?.string() ?: "")
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun buildApiHistory(systemPrompt: String): JSONArray {
        val arr = JSONArray()
        // System
        arr.put(JSONObject().apply {
            put("role", "system"); put("content", systemPrompt)
        })
        // Mevcut konuşma geçmişi (son 20 mesaj — token tasarrufu)
        _uiState.value.messages.takeLast(20).forEach { msg ->
            arr.put(JSONObject().apply {
                put("role", when (msg.role) { ChatRole.USER -> "user"; else -> "assistant" })
                put("content", msg.content)
            })
        }
        return arr
    }

    // ── Sistem prompt'u — kullanıcının dolabını içerir ─────────────────────────

    private fun buildSystemPrompt(): String {
        val wardrobe = if (allKiyaketler.isEmpty()) {
            "Kullanıcının dolabında henüz kıyafet yok."
        } else {
            allKiyaketler.take(80).joinToString("\n") { k ->
                "- ${k.marka} ${k.tur.displayName}" +
                (if (k.renk != null) " (${k.renk})" else "") +
                (if (k.beden.isNotBlank() && k.beden != "Standart") " Beden:${k.beden}" else "") +
                (if (k.mevsim.displayName != "Dört Mevsim") " [${k.mevsim.displayName}]" else "")
            }
        }

        return """
Sen CepteKabin uygulamasının Türkçe konuşan, samimi ve yardımsever bir stil asistanısın.
Kullanıcının dolabındaki kıyafetleri bilerek onlara kişiselleştirilmiş kombin önerileri sunuyorsun.

KULLANICININ DOLABI:
$wardrobe

KURALLAR:
1. Yalnızca kullanıcının dolabındaki kıyafetleri kullan. Olmayan ürün önerme.
2. Önerilerin somut ol: "Mavi kot + beyaz tişört + beyaz sneaker" gibi.
3. Hava durumu veya etkinlik bilgisi verilirse bunu dikkate al.
4. Kısa ve anlaşılır cevaplar ver (maksimum 4-5 cümle veya madde).
5. Eğer dolap boşsa veya uygun kıyafet yoksa bunu nazikçe belirt.
6. Türkçe konuş.
        """.trimIndent()
    }
}
