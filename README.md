<div align="center">
  
  <img src="app/src/main/res/drawable-nodpi/app_logo.webp" alt="CepteKabin Logo" width="150"/>

  # 📱 CepteKabin v1.0
  
  **Akıllı Dijital Gardırop ve Hava Durumu Tabanlı Kombin Asistanı**
  
  [![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Compose-Material_3-4285F4.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
  [![Firebase](https://img.shields.io/badge/Firebase-Auth_%7C_Firestore-FFCA28.svg?style=flat-square&logo=firebase)](https://firebase.google.com)
  [![Architecture](https://img.shields.io/badge/Architecture-MVVM_%7C_Clean-success.svg?style=flat-square)](#-mimari-yap%C4%B1)
  [![License](https://img.shields.io/badge/License-MIT-gray.svg?style=flat-square)](LICENSE)

  <br>

  [![Download APK](https://img.shields.io/badge/📲_Hemen_İndir_(APK)-2EA043?style=for-the-badge&logo=android&logoColor=white)](https://github.com/KULLANICI_ADIN/CepteKabin/releases/latest)

  *Kendi gardırobunu cebinde taşı, hava durumuna en uygun kombini saniyeler içinde yap!*

</div>

---

## 📋 İçindekiler
- [✨ Temel Özellikler](#-temel-özellikler)
- [🛠️ Teknoloji Yığını (Tech Stack)](#️-teknoloji-yığını-tech-stack)
- [🏗️ Mimari Yapı](#️-mimari-yapı)
- [🧠 4 Aşamalı Hibrit Barkod Sistemi](#-4-aşamalı-hibrit-barkod-sistemi)
- [🎨 Liquid Glass UI Tasarımı](#-liquid-glass-ui-tasarımı)
- [⚙️ Kurulum ve Geliştirme](#️-kurulum-ve-geliştirme)
- [📁 Proje Dizini](#-proje-dizini)
- [🤝 Katkıda Bulunma](#-katkıda-bulunma)

---

## ✨ Temel Özellikler

* **☁️ Gerçek Zamanlı Hava Durumu:** `mooweather-api` destekli, 5 günlük hava durumu ve anlık sıcaklık takibi.
* **🤖 Akıllı Kombin Önerisi:** O günkü hava sıcaklığına, mevsime ve ürünün stok/sezon durumuna göre otomatik kıyafet seçimi.
* **📸 Gelişmiş Ürün Ekleme:** Sadece barkod okutarak veya Google ML Kit (OCR) ile etiket fotoğrafı çekerek saniyeler içinde dolaba kıyafet ekleme.
* **🔄 Bulut Senkronizasyonu:** Firebase Google Sign-In ile cihazlar arası kesintisiz veri aktarımı (Offline-First destekli).
* **🧊 Camsız Cam (Glassmorphism) Arayüz:** Göz yormayan, minimalist ve fütüristik Jetpack Compose UI tasarımı.
* **📦 Sezonlu Ürün Takibi:** "Satışta", "Stokta Yok" veya "Üretimden Kalktı" gibi detaylı ürün yaşam döngüsü yönetimi.

---

## 🛠️ Teknoloji Yığını (Tech Stack)

Uygulama, modern Android geliştirme standartlarına (Modern Android Development - MAD) %100 uyumlu olarak geliştirilmiştir.

| Kategori | Teknolojiler / Kütüphaneler |
| :--- | :--- |
| **Dil & Arayüz** | Kotlin, Jetpack Compose, Material Design 3 |
| **Mimari** | Clean Architecture, MVVM (Model-View-ViewModel) |
| **Asenkron İşlemler** | Kotlin Coroutines, Kotlin Flow |
| **Ağ Katmanı (Network)** | Retrofit2, OkHttp3, Gson Converter |
| **Yerel Veritabanı** | Room Database (SQLite tabanlı) |
| **Bulut & Yetkilendirme**| Firebase Authentication (Google Sign-In), Cloud Firestore |
| **Bağımlılık Enjeksiyonu**| Dagger - Hilt |
| **Yapay Zeka / Görüntü** | Google ML Kit (Barcode Scanning & Text Recognition OCR) |
| **Görsel Yükleme** | Coil (Compose destekli) |
| **Kamera İşlemleri** | CameraX API |

---

## 🏗️ Mimari Yapı

Proje, Sürdürülebilirlik ve Test Edilebilirlik ilkeleri gereği **Clean Architecture (Temiz Mimari)** baz alınarak katmanlı bir yapıda tasarlanmıştır.

```mermaid
graph TD
  UI[UI Layer / Jetpack Compose] --> VM[ViewModel]
  VM --> UseCase[Domain Layer / UseCases]
  UseCase --> RepoInt[Repository Interfaces]
  RepoInt -.-> RepoImpl[Data Layer / RepositoryImpl]
  RepoImpl --> Local[(Room Database)]
  RepoImpl --> Remote((Firebase / Retrofit))
````

1.  **UI Katmanı:** Sadece arayüzü çizer (`GlassCard`, `TaramaScreen` vb.) ve ViewModel'dan gelen `State`'leri dinler.
2.  **Domain Katmanı:** İş mantığını barındırır. Saf Kotlin ile yazılmıştır, Android framework'ünden bağımsızdır.
3.  **Data Katmanı:** Verinin nereden (Room, Firebase veya API) geleceğine karar veren Depo (Repository) uygulamalarını içerir.

-----

## 🧠 4 Aşamalı Hibrit Barkod Sistemi

Piyasadaki standart uygulamaların aksine CepteKabin, okutulan bir barkodu bulmak için agresif ve akıllı bir "Şelale (Fallback)" mimarisi kullanır:

1.  **Aşama 1 (Yerel Önbellek - \<0.1sn):** Önce Room Database üzerinde aranır. Daha önce sisteme eklenmişse anında getirilir.
2.  **Aşama 2 (Global API Havuzu - 0.5sn):** Ürün yerelde yoksa arka planda `UPCItemDb` ve `OpenBeautyFacts` gibi global havuzlar eşzamanlı taranır.
3.  **Aşama 3 (Web Scraping - 1.0sn):** Global API'lerde bulunamazsa, özel algoritmalar ile popüler e-ticaret sitelerinde (Trendyol vb.) arka planda kazıma işlemi yapılarak ürün başlığı ayrıştırılır.
4.  **Aşama 4 (Google ML Kit OCR - Fallback):** Hiçbir veritabanında kaydı olmayan (özel dikim veya etiketsiz) ürünler için kullanıcı kamerayı açar, kıyafetin iç etiketini çeker ve yapay zeka metinleri okuyarak (Örn: "%100 Pamuk, M Beden, Koton") formu otomatik doldurur.

-----

## 🎨 Liquid Glass UI Tasarımı

Uygulamanın görsel kimliği, fotoğrafların karmaşasını önlemek adına **Monokrom Minimalizm** ve **Liquid Glass (Camsız Cam)** efektleri üzerine kurulmuştur.

  * `GlassCard.kt` bileşeni kullanılarak, yarı saydam yüzeyler, hafif bulanıklaştırma (blur) efektleri ve ince ışık hüzmesi sınırları (borders) ile kullanıcının eklediği kıyafetler adeta bir cam vitrinin arkasındaymış gibi sergilenir.

-----

## ⚙️ Kurulum ve Geliştirme

Projeyi kendi bilgisayarınızda çalıştırmak için aşağıdaki adımları izleyin:

### Ön Koşullar

  * Android Studio (Iguana veya daha yeni bir sürüm)
  * JDK 17
  * Bir Firebase Projesi

### Adım 1: Projeyi Klonlayın

```bash
git clone [https://github.com/KULLANICI_ADIN/CepteKabin.git](https://github.com/KULLANICI_ADIN/CepteKabin.git)
cd CepteKabin
```

### Adım 2: Firebase Kurulumu

1.  [Firebase Console](https://console.firebase.google.com/)'a gidin ve yeni bir proje oluşturun.
2.  Android uygulamanızı ekleyin (Paket adı: `com.cyberqbit.ceptekabin`).
3.  İndirdiğiniz `google-services.json` dosyasını projedeki `app/` klasörünün içine yapıştırın.
4.  Firebase'de **Authentication -\> Sign-in method** altından **Google**'ı aktif edin.
5.  Projenizin SHA-1 anahtarını Firebase proje ayarlarına eklemeyi unutmayın\!

### Adım 3: API Anahtarlarını Ayarlayın

  * Proje içerisindeki `Constants.kt` (veya ilgili Config) dosyasında bulunan `WEB_CLIENT_ID` alanına Firebase'den aldığınız Web Client ID'yi yapıştırın.

### Adım 4: Derleme ve Çalıştırma

Android Studio üzerinden projeyi Sync (Senkronize) edin ve Emülatör veya gerçek cihaz üzerinden çalıştırın.

> *Not: Render.com üzerindeki hava durumu API'si "Free Tier" (Ücretsiz) katmanında olduğu için, uygulama ilk açıldığında hava durumu verisinin gelmesi sunucunun uyanma süresine bağlı olarak 30-50 saniye sürebilir.*

-----

## 🤝 Katkıda Bulunma

CepteKabin açık kaynaklı bir projedir ve katkılara her zaman açıktır\! Katkıda bulunmak isterseniz:

1.  Bu depoyu Fork'layın.
2.  Yeni bir özellik dalı (branch) oluşturun (`git checkout -b feature/YeniOzellik`).
3.  Değişikliklerinizi commit'leyin (`git commit -m 'Yeni bir harika özellik eklendi'`).
4.  Dalınızı push'layın (`git push origin feature/YeniOzellik`).
5.  Bir Pull Request (PR) açın.

-----

<div align="center"\>
<p> <b>cyberQbit</b> tarafından ❤️ ve <b>Kotlin</b> kullanılarak geliştirilmiştir.</p>
</div>
