<div align="center">
  
  <img src="app/src/main/res/drawable-nodpi/app_logo.webp" alt="CepteKabin Logo" width="175"/>

  # 📱 CepteKabin v2.0
  
  **Yapay Zeka Destekli Moda Asistanı, Dijital Gardırop ve Sosyal Stil Ağı**
  
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Compose-Material_3-4285F4.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
  [![Architecture](https://img.shields.io/badge/Architecture-Clean_%7C_MVVM-success.svg?style=flat-square)](#-mimari-yap%C4%B1)
  [![AI](https://img.shields.io/badge/AI-ML_Kit_%7C_GPT-FF6F00.svg?style=flat-square&logo=google)](https://developers.google.com/ml-kit)
  [![License](https://img.shields.io/badge/License-MIT-gray.svg?style=flat-square)](LICENSE)

  <br>

  <a href="https://github.com/cyberQbit/CepteKabin/releases/latest">
    <img src="https://img.shields.io/badge/📥_CEPTEKABİN'İ_HEMEN_İNDİR_(APK)-2EA043?style=for-the-badge&logo=android&logoColor=white" height="65" alt="CepteKabin İndir"/>
  </a>
  
  <p><b>✨ Kendi gardırobunu cebinde taşı, yapay zeka ile tarzını baştan yarat! ✨</b></p>

</div>

---

## 📋 İçindekiler
- [🚀 v2.0 ile Gelen Yenilikler](#-v20-ile-gelen-yenilikler)
- [✨ Temel Özellikler](#-temel-özellikler)
- [🛠️ Teknoloji Yığını](#️-teknoloji-yığını)
- [🏗️ Mimari Yapı](#️-mimari-yapı)
- [🔗 Ekosistem & Paylaşım](#-ekosistem--paylaşım)
- [🎨 Liquid Glass Arayüz](#-liquid-glass-arayüz)
- [⚙️ Kurulum ve Geliştirme](#️-kurulum-ve-geliştirme)
- [🤝 Katkıda Bulunma](#-katkıda-bulunma)

---

## 🚀 v2.0 ile Gelen Yenilikler

CepteKabin v2.0, bir giysi depolama uygulamasından ziyade kişisel stilistiniz olarak yeniden tasarlandı:

* **🪄 Yapay Zeka Arka Plan Silici (Magic Eraser):** Kıyafetinizi nerede çekerseniz çekin, yapay zeka saniyeler içinde arka planı temizler ve profesyonel, şeffaf (PNG) e-ticaret görselleri oluşturur.
* **🤖 GPT Destekli Stil Chatbot'u:** *"Hafta sonu Antalya'daki düğün için bana 3 kombin öner"* yazın, yapay zeka dolabınızı analiz edip size en uygun parçaları eşleştirsin.
* **👗 Sanal Kabin (Virtual Try-On):** Kendi fotoğrafınızı yükleyin ve beğendiğiniz kombinleri artırılmış gerçeklik (AR) desteğiyle **kendi üzerinizde** görün!
* **📅 Kombin Takvimi & CPW Analizi:** Hangi gün ne giydiğinizi şık bir takvimde takip edin. "Giyilme Başına Maliyet" (Cost Per Wear) algoritmasıyla sürdürülebilir modaya katkı sağlayın.
* **👯 Ortak Dolaplar (Borrow & Share):** Arkadaşlarınızı ekleyin, onların dolaplarını gezin ve hafta sonu için kıyafet ödünç alma istekleri gönderin.

---

## ✨ Temel Özellikler

* **☁️ Gerçek Zamanlı Hava Durumu:** Konumunuza özel 5 günlük hava durumu raporu ve o günkü hava sıcaklığına / mevsime tam uyumlu otomatik kombin önerileri.
* **🧠 4 Aşamalı Hibrit Tarayıcı:** Barkod okutun, yerel DB, global e-ticaret ağları ve **Google ML Kit OCR** (akıllı etiket okuyucu) ile kıyafetlerinizi saniyeler içinde manuel veri girmeden ekleyin.
* **📦 %100 Gizlilik (Offline-First):** V2 mimarisiyle birlikte görselleriniz artık bulutta değil, cihazınızın kalıcı hafızasında gizli klasörlerde (`.nomedia`) şifrelenerek saklanır. Kotanızı yemez, galerinizde görünmez.
* **✏️ Tam Teşekküllü Düzenleme:** Dinamik beden algoritması (Ayakkabılar için 16-47 numara vb.) ve mantıksal kategori eşleşmesi ile tüm dolabınızı özgürce yönetin.

---

## 🛠️ Teknoloji Yığını

Proje, **Modern Android Development (MAD)** vizyonunun en güncel prensipleriyle kodlanmıştır.

| Kategori | Teknolojiler / Araçlar |
| :--- | :--- |
| **Dil & UI** | Kotlin, Jetpack Compose, Material Design 3 |
| **Mimari** | Clean Architecture, MVVM, Compose Navigation |
| **Asenkron / Akış** | Coroutines, StateFlow, SharedFlow |
| **Veritabanı (Yerel)** | Room Database (Kompleks İlişkiler & Fallback) |
| **Ağ (Network)** | Retrofit2, OkHttp3, Gson |
| **AI & Görüntü İşleme**| Google ML Kit (Vision/OCR), Background Remover Service |
| **Bulut (Opsiyonel)** | Firebase Authentication, Cloud Firestore |
| **Diğer** | Dagger-Hilt (DI), Coil, CameraX, Android FileProvider |

---

## 🏗️ Mimari Yapı

Sürdürülebilirlik ve test edilebilirlik için katı bir **Clean Architecture** benimsenmiştir.

```mermaid
graph TD
  UI[UI Layer / Compose Screens] --> VM[ViewModel]
  VM --> UseCase[Domain Layer / UseCases]
  UseCase --> RepoInt[Repository Interfaces]
  RepoInt -.-> RepoImpl[Data Layer / RepositoryImpl]
  RepoImpl --> Local[(Room DB / Cihaz Hafızası)]
  RepoImpl --> Remote((Hava Durumu API / OCR))
````

-----

## 🔗 Ekosistem & Paylaşım

  * **`.kmb` Teknolojisi:** Kombinlerinizi, içindeki kıyafetlerin tam çözünürlüklü görselleriyle beraber tek bir `.kmb` dosyasına sıkıştırarak WhatsApp veya Instagram'dan anında paylaşın.
  * **Deep Linking (Derin Bağlantılar):** `ceptekabin.com/kombin/...` formatındaki davet linklerine tıklayan kullanıcılar, uygulama yüklüyse direkt kombine, değilse indirme sayfasına yönlendirilir.

-----

## 🎨 Liquid Glass Arayüz

Klasik ve sıkıcı kart tasarımları yerine, yarı saydam yüzeyler ve hafif bulanıklaştırma (blur) efektleri sunan **Liquid Glass (Camsız Cam)** felsefesi. Karanlık Mod (Dark Mode) ile tam uyumlu, pürüzsüz 60 FPS Compose animasyonları.

-----

## ⚙️ Kurulum ve Geliştirme

Projeyi derlemek için aşağıdaki adımları izleyin:

### Ön Koşullar

  * Android Studio (Koala veya daha yeni sürüm önerilir)
  * JDK 17+
  * Geçerli bir `google-services.json` (Firebase için)

### Adımlar

1.  Depoyu klonlayın:
    ```bash
    git clone [https://github.com/KULLANICI_ADIN/CepteKabin.git](https://github.com/KULLANICI_ADIN/CepteKabin.git)
    cd CepteKabin
    ```
2.  Proje kök dizinine, Firebase Console'dan aldığınız `google-services.json` dosyasını `app/` klasörü altına yapıştırın.
3.  `Constants.kt` içerisindeki `WEB_CLIENT_ID` alanına kendi Firebase Web Client ID'nizi girin.
4.  VSCode veya Android Studio terminali üzerinden projeyi temizleyip derleyin:
    ```bash
    ./gradlew clean
    ./gradlew assembleRelease
    ```

-----

## 🤝 Katkıda Bulunma

Açık kaynak dünyasını destekliyoruz\! Yeni özellikler eklemek, bug çözmek veya arayüzü geliştirmek için:

1.  Projeyi Fork'layın.
2.  Yeni bir branch oluşturun (`git checkout -b feature/YeniFikir`).
3.  Değişikliklerinizi commit'leyin (`git commit -m 'Yapay zeka modülü eklendi'`).
4.  Branch'inizi push'layın ve Pull Request (PR) açın.

-----

<div align="center">
<p><b>cyberQbit</b> tarafından ❤️ ile geliştirilmiştir.</p>
</div>
