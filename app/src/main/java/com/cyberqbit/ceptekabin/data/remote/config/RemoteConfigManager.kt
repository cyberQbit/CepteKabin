package com.cyberqbit.ceptekabin.data.remote.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CepteKabin Dinamik Yonetim Katmani
 *
 * Firebase Remote Config uzerinden:
 * - Ozellik ac/kapat (Feature Flagging)
 * - Dinamik metin ve icerik guncelleme
 * - AI oneri parametreleri ayarlama
 * - Ozel gun tema/banner yonetimi
 * - Acil devre disi birakma (Kill Switch)
 *
 * Firebase Console'dan degisiklikleri aninda tum kullanicilara yansitir.
 */
@Singleton
class RemoteConfigManager @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private val _featureFlags = MutableStateFlow(FeatureFlags())
    val featureFlags: StateFlow<FeatureFlags> = _featureFlags.asStateFlow()

    private val _dynamicContent = MutableStateFlow(DynamicContent())
    val dynamicContent: StateFlow<DynamicContent> = _dynamicContent.asStateFlow()

    private val _aiConfig = MutableStateFlow(AiConfig())
    val aiConfig: StateFlow<AiConfig> = _aiConfig.asStateFlow()

    init {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // Production: 1 saat cache
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(defaultValues)
        parseAll()
    }

    /**
     * Firebase'den en son degerleri ceker.
     * Uygulama her acildiginda ve belirli araliklarda cagirilir.
     */
    suspend fun fetchAndActivate(): Boolean {
        return try {
            val activated = remoteConfig.fetchAndActivate().await()
            parseAll()
            Log.d(TAG, "Remote Config guncellendi. Activated: $activated")
            activated
        } catch (e: Exception) {
            Log.w(TAG, "Remote Config fetch basarisiz: ${e.message}")
            false
        }
    }

    private fun parseAll() {
        _featureFlags.value = FeatureFlags(
            takvimEnabled        = remoteConfig.getBoolean("feature_takvim_enabled"),
            chatbotEnabled       = remoteConfig.getBoolean("feature_chatbot_enabled"),
            barkodEnabled        = remoteConfig.getBoolean("feature_barkod_enabled"),
            kombinPaylasimiEnabled = remoteConfig.getBoolean("feature_kombin_paylasimi_enabled"),
            sanalProvaEnabled    = remoteConfig.getBoolean("feature_sanal_prova_enabled"),
            aiKombinEnabled      = remoteConfig.getBoolean("feature_ai_kombin_enabled"),
            havaDurumuEnabled    = remoteConfig.getBoolean("feature_hava_durumu_enabled"),
            tutorialEnabled      = remoteConfig.getBoolean("feature_tutorial_enabled"),
            maintenanceMode      = remoteConfig.getBoolean("maintenance_mode"),
            forceUpdateMinVersion = remoteConfig.getString("force_update_min_version")
        )

        _dynamicContent.value = DynamicContent(
            homeBannerText       = remoteConfig.getString("home_banner_text"),
            homeBannerVisible    = remoteConfig.getBoolean("home_banner_visible"),
            homeBannerColor      = remoteConfig.getString("home_banner_color"),
            promoImageUrl        = remoteConfig.getString("promo_image_url"),
            specialDayTheme      = remoteConfig.getString("special_day_theme"),
            announcementText     = remoteConfig.getString("announcement_text"),
            announcementVisible  = remoteConfig.getBoolean("announcement_visible")
        )

        _aiConfig.value = AiConfig(
            renkUyumuAgirligi    = remoteConfig.getDouble("ai_renk_uyumu_agirligi").toFloat(),
            havaUyumuAgirligi    = remoteConfig.getDouble("ai_hava_uyumu_agirligi").toFloat(),
            maxKombinOnerisi     = remoteConfig.getLong("ai_max_kombin_onerisi").toInt(),
            sicaklikEsikleri     = remoteConfig.getString("ai_sicaklik_esikleri")
        )
    }

    // ─── Yardimci Erisimler ─────────────────────────────────────────────────

    fun isFeatureEnabled(key: String): Boolean = remoteConfig.getBoolean(key)
    fun getString(key: String): String = remoteConfig.getString(key)
    fun getLong(key: String): Long = remoteConfig.getLong(key)

    companion object {
        private const val TAG = "RemoteConfig"

        /** Firebase Console'da tanimlanmamis key'ler icin varsayilan degerler */
        private val defaultValues = mapOf<String, Any>(
            // ─── Feature Flags (varsayilan: hepsi acik) ───
            "feature_takvim_enabled"          to true,
            "feature_chatbot_enabled"         to true,
            "feature_barkod_enabled"          to true,
            "feature_kombin_paylasimi_enabled" to true,
            "feature_sanal_prova_enabled"     to true,
            "feature_ai_kombin_enabled"       to true,
            "feature_hava_durumu_enabled"     to true,
            "feature_tutorial_enabled"        to true,
            "maintenance_mode"                to false,
            "force_update_min_version"        to "",

            // ─── Dinamik Icerik ───
            "home_banner_text"    to "",
            "home_banner_visible" to false,
            "home_banner_color"   to "#2196F3",
            "promo_image_url"     to "",
            "special_day_theme"   to "",
            "announcement_text"   to "",
            "announcement_visible" to false,

            // ─── AI Parametreleri ───
            "ai_renk_uyumu_agirligi" to 0.4,
            "ai_hava_uyumu_agirligi" to 0.6,
            "ai_max_kombin_onerisi"  to 3L,
            "ai_sicaklik_esikleri"   to ""  // bos ise varsayilan engine degerleri kullanilir
        )
    }
}

/**
 * Ozellik ac/kapat bayraklari.
 * Firebase Console'dan tek tikla herhangi bir ozelligi devre disi birakabilirsin.
 */
data class FeatureFlags(
    val takvimEnabled: Boolean = true,
    val chatbotEnabled: Boolean = true,
    val barkodEnabled: Boolean = true,
    val kombinPaylasimiEnabled: Boolean = true,
    val sanalProvaEnabled: Boolean = true,
    val aiKombinEnabled: Boolean = true,
    val havaDurumuEnabled: Boolean = true,
    val tutorialEnabled: Boolean = true,
    val maintenanceMode: Boolean = false,
    val forceUpdateMinVersion: String = ""
)

/**
 * Dinamik icerik: banner, duyuru, ozel gun temasi.
 * Magaza guncellemesi gerekmeden aninda degistirilebilir.
 */
data class DynamicContent(
    val homeBannerText: String = "",
    val homeBannerVisible: Boolean = false,
    val homeBannerColor: String = "#2196F3",
    val promoImageUrl: String = "",
    val specialDayTheme: String = "",
    val announcementText: String = "",
    val announcementVisible: Boolean = false
)

/**
 * AI motor parametreleri.
 * Kombin onerisi agirliklarini ve esik degerlerini uzaktan ayarlayabilirsin.
 */
data class AiConfig(
    val renkUyumuAgirligi: Float = 0.4f,
    val havaUyumuAgirligi: Float = 0.6f,
    val maxKombinOnerisi: Int = 3,
    val sicaklikEsikleri: String = ""
)
