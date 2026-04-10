<div align="center">

  <img src="app/src/main/res/drawable-nodpi/app_logo.webp" alt="CepteKabin Logo" width="185"/>

  # 📱 CepteKabin v2.2

  **Yapay Zeka Destekli Moda Asistanı, Dijital Gardırop ve Kişisel Kombin Planlayıcı**

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Compose-Material_3-4285F4.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
  [![Architecture](https://img.shields.io/badge/Architecture-Clean_%7C_MVVM-success.svg?style=flat-square)](#-mimari-yap%C4%B1)
  [![UI](https://img.shields.io/badge/UI-iOS_Style-000000.svg?style=flat-square)](#-modern-aray%C3%BCz)
  [![AI](https://img.shields.io/badge/AI-ML_Kit_%7C_GPT-FF6F00.svg?style=flat-square&logo=google)](https://developers.google.com/ml-kit)
  [![License](https://img.shields.io/badge/License-MIT-gray.svg?style=flat-square)](LICENSE)

  <br>

  <a href="https://play.google.com/store/apps/details?id=com.cyberqbit.ceptekabin">
    <img src="https://img.shields.io/badge/Google_Play'den_İndir_(⚠️_Kapalı_Test_Sürecinde*¹)-000000?style=for-the-badge&logo=google-play&logoColor=white" height="80" alt="Google Play'den İndir!"/>
  </a>
  <br><br>
  <a href="https://github.com/cyberQbit/CepteKabin/releases/latest">
    <img src="https://img.shields.io/badge/%F0%9F%93%81_CEPTEKAB%C4%B0N'%C4%B0_HEMEN_%C4%B0ND%C4%B0R_(APK)-2EA043?style=for-the-badge&logo=android&logoColor=white" height="65" alt="CepteKabin İndir"/>
  </a>
  <br><br>
  <p><b>✨ Kendi gardırobunu cebinde taşı, yapay zeka ile tarzını baştan yarat! ✨</b></p>

</div>

---

## 📋 İçindekiler
- [🎉 v2.2 Yenilikler](#-v22-yenilikler)
- [✨ Temel Özellikler](#-temel-özellikler)
- [🧠 Yapay Zeka Motorları](#-yapay-zeka-motorları)
- [🛠️ Teknoloji Yığını](#️-teknoloji-yığını)
- [🏗️ Mimari Yapı](#️-mimari-yapı)
- [📅 Kombin Takvimi](#-kombin-takvimi)
- [🎨 Modern Arayüz](#-modern-arayüz)
- [⚙️ Kurulum ve Geliştirme](#️-kurulum-ve-geliştirme)
- [🤝 Katkıda Bulunma](#-katkıda-bulunma)

---

## 🎉 v2.2 Yenilikler

### 🛡️ Güvenlik ve İzin Yönetimi

| Özellik | Açıklama |
|---------|----------|
| **Kamera İzin Kontrolü** | Çalışma zamanında `CAMERA` izni isteniyor, izin verilmeden kamera açılmıyor |
| **Güvenli Kamera Akışı** | İzin reddedilirse uygulama çökmez, kullanıcıya bilgi verilir |

### 🌡️ Hava Durumu Optimizasyonu

| Özellik | Önce | Sonra |
|---------|------|-------|
| **API Tasarrufu** | Her ekran geçişinde API çağrısı | 15 dakika bayatlık kontrolü |
| **Manuel Yenileme** | Spam yapılabiliyordu | 60 saniye cooldown |
| **Cache Senkronizasyonu** | Her ekran bağımsız yüklüyordu | Room Flow ile reaktif paylaşım |
| **HomeScreen Davranışı** | Her dönüşte yeniden yüklüyordu | Cache'den anlık gösterim |

### 🎨 UI/UX İyileştirmeleri

| Özellik | Açıklama |
|---------|----------|
| **Sabit Koyu Tema** | Sistem temasından bağımsız, tek renk paleti |
| **Safe Area Düzeltmeleri** | Onboarding ve Auth ekranlarında navigation bar çakışması giderildi |
| **Navigation Bar Padding** | 3-tuşlu ve gesture navigasyon için optimize edildi |

### 🔐 Kimlik Doğrulama

| Özellik | Açıklama |
|---------|----------|
| **Zorunlu Hesap Seçici** | Google Sign-In'de önceki oturum otomatik kullanılmıyor |
| **Onboarding Zorunluluğu** | "Atla" butonu kaldırıldı, giriş yapmadan uygulamaya giriş yok |

### 🐛 v2.2 Hata Düzeltmeleri

| # | Sorun | Çözüm |
|---|-------|-------|
| 1 | Türkçe karakterler bozuk görünüyordu | 18 dosyada encoding düzeltildi |
| 2 | Kamera butonuna basınca uygulama çöküyordu | Çalışma zamanı izin kontrolü eklendi |
| 3 | HomeScreen'de sürekli shimmer loading | Cache-first stratejisi uygulandı |
| 4 | Hava durumu manuel yenileme spam | 60 saniye cooldown eklendi |
| 5 | Google girişinde hesap seçici gösterilmiyordu | Önceki oturum temizleniyor |
| 6 | Aydınlık tema cihazlarda görünüyordu | Sabit koyu tema uygulandı |

---

## ✨ Temel Özellikler

| Özellik | Açıklama |
|---------|----------|
| **☁️ Akıllı Hava Durumu** | 5 günlük hava tahmini + 15dk cache optimizasyonu |
| **🧠 AI Kombin Önerileri** | Hava durumuna ve renk uyumuna göre otomatik öneriler |
| **📦 Gizlilik Odaklı Depolama** | Görseller `.nomedia` klasörlerinde şifrelenir |
| **🛒 Barkod ile Hızlı Ekleme** | ML Kit OCR ile barkod okutma |
| **📅 Kombin Takvimi** | 30 güne kombin planla, günde 3 kombin |
| **🎨 Renk Uyumu Analizi** | 12 renk grubu üzerinden kombinasyon puanlaması |
| **👤 Sanal Prova** | Fotoğraf doğrulama ile yüz tespiti |
| **🔄 Cihazlar Arası Senkronizasyon** | Google hesabı ile tüm veriler senkronize |

---

## 🧠 Yapay Zeka Motorları

### WeatherOutfitEngine
7 hava kategorisi ile kıyafet önerir:

| Kategori | Sıcaklık | Örnek Kıyafetler |
|----------|----------|------------------|
| Kavurucu | ≥ 35°C | Tişört, Şort, Sandalet |
| Sıcak | 28-34°C | Tişört, Polo, Sneaker |
| Ilık | 20-27°C | Gömlek, Bluz, Chino |
| Serin | 12-19°C | Sweatshirt, Kazak, Bot |
| Soğuk | 5-11°C | Mont, Kalın Pantolon, Atkı |
| Çok Soğuk | 0-4°C | Termal içlik, Kaban, Eldiven |
| Dondurucu | < 0°C | Parka, Yün Çorap, Bere |

**Konfor Endeksi:** 0-100 arası skor (sıcaklık, nem, rüzgar, yağış)

### SmartKombinSuggester
Dolabınızdaki kıyafetlerden **en uyumlu kombinasyonları** üretir:
- Renk uyumu %40, hava uyumu %60 ağırlıklı
- Mevsim ve hava durumuna göre 1-3 katman önerisi
- Maksimum 3 öneri, tekrarlanmayan seçimler

### ColorHarmonyUtil
```
Uyum Puanı (100 üzerinden):
• Aynı renk grubu           → 85 puan
• Nötr renk ile kombinasyon  → 95 puan
• Lacivert bazlı            → 90 puan
• Komplementer              → 80 puan
• Analog (yan yana)         → 75 puan
• Çakışan renkler           → 45 puan
```

---

## 🛠️ Teknoloji Yığını

| Kategori | Teknolojiler |
|:---------|:-------------|
| **Dil & UI** | Kotlin, Jetpack Compose, Material Design 3 |
| **Mimari** | Clean Architecture, MVVM, Compose Navigation |
| **Asenkron** | Coroutines, StateFlow, SharedFlow |
| **Veritabanı** | Room Database (v6) + Flow reaktif sorgular |
| **Ağ** | Retrofit2, OkHttp3, Gson |
| **AI & Görüntü** | Google ML Kit (Face, OCR), CameraX |
| **Görsel** | Coil (AsyncImage) |
| **DI** | Dagger-Hilt |
| **Cloud** | Firebase Auth, Cloud Firestore |

---

## 🏗️ Mimari Yapı

```
app/src/main/java/com/cyberqbit/ceptekabin/
├── domain/
│   ├── model/          # Kiyaket, Kombin, HavaDurumu, ForecastItem
│   ├── engine/          # WeatherOutfitEngine, SmartKombinSuggester, ColorHarmonyUtil
│   ├── repository/      # Repository interfaces
│   └── util/           # PhotoValidationUtil
├── data/
│   ├── local/
│   │   ├── database/   # Room entities, DAOs, Database (v6)
│   │   └── dao/        # TakvimGirisiDao, WeatherCacheDao (Flow)
│   ├── repository/     # Repository implementations
│   └── service/        # LocationService, GoogleAuthService
├── ui/
│   ├── screens/        # Home, Dolap, Kombin, HavaDurumu, Takvim, Onboarding, Auth
│   ├── components/     # GlassCard, GlassSurface, ShimmerLoading, TutorialOverlay
│   ├── navigation/     # NavGraph, Screen routes
│   └── theme/          # Color, Type, Theme (sabit koyu tema)
└── util/              # Constants
```

---

## 📅 Kombin Takvimi

- **30 günlük görünüm** - geçmişe 5 gün, ileriye 30 gün
- **Günde 3 kombin** - sabah, öğle, akşam slotları
- **Arşiv modu** - geçmiş günler salt okunur
- **Anlık snapshot** - kıyafet silinse bile görsel kaydedilir
- **Kullanım takibi** - her kıyafetin kaç kez giyildiği

---

## 🎨 Modern Arayüz

### iOS Tarzı Animasyonlar

| Animasyon | Açıklama |
|-----------|----------|
| **Press Effect** | 0.97-0.98f scale, spring(damping=0.7, stiffness=400) |
| **SlideUpFadeIn** | fade(300ms) + slideUp(spring damping=0.8) |
| **Shimmer** | 1200ms döngü, FastOutSlowInEasing |
| **Page Transition** | slideHorizontal + fade, spring(damping=0.85) |

### GlassCard / GlassSurface
- 16dp corner radius
- Dark: 0.7f opacity
- 1dp subtle border
- Soft shadow

### Modern Navigation Bar
- Sadece ikonlar (label yok)
- Aktif: dolu (filled) ikon + 52dp yuvarlak background
- Pasif: çerçeveli (outlined) ikon
- 28dp ikon boyutu
- iOS tarzı spring press effect

### Safe Area Desteği
- `statusBarsPadding()` + `navigationBarsPadding()`
- 3-tuşlu ve gesture navigasyon desteği
- Edge-to-edge immersive experience

### Tasarım Sistemi

| Token | Değer |
|-------|-------|
| Primary | `#2196F3` |
| Secondary | `#00BCD4` |
| Accent | `#FFB300` |
| Error | `#E53935` |

---

## ⚙️ Kurulum ve Geliştirme

### Ön Koşullar
- Android Studio (Koal+)
- JDK 17+
- `google-services.json` (Firebase)
- Google ML Kit

### Adımlar

```bash
git clone https://github.com/cyberQbit/CepteKabin.git
cd CepteKabin
```

2. Firebase Console'dan `google-services.json`'ı `app/` klasörüne koyun

3. `Constants.kt` içindeki `WEB_CLIENT_ID`'yi güncelleyin

4. Derleyin:
```bash
./gradlew clean
./gradlew bundleRelease
```

### Önemli Notlar

> ⚠️ **Veritabanı v6:** İlk açılışta eski veriler silinebilir. `fallbackToDestructiveMigration()` aktif.

> 💡 **Konum İzni:** Hava durumu için `ACCESS_FINE_LOCATION` gereklidir.

> 📷 **Kamera İzni:** Android 6.0+ için çalışma zamanında `CAMERA` izni istenir.

---

## 🤝 Katkıda Bulunma

1. Fork'layın
2. Branch oluşturun (`git checkout -b feature/YeniFikir`)
3. Commit'leyin
4. Push'layın ve Pull Request açın

---
#### <b>*</b> <sup>1</sup> (Not: Uygulamanın Google Play Store sürümü "Kapalı Test - Alpha" sürecinde olup, bu sürece dahil olmak için geliştirici ile Google Hesabınıza ait e-posta hesabınızı paylaşmanız gerekmektedir!)
---

<div align="center">
<p><b>cyberQbit</b> tarafından ❤️ ile geliştirilmiştir.</p>
</div>
