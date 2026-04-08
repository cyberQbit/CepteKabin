# Play Store Yayın Rehberi — CepteKabin

Bu klasör Play Store yayını için gereken tüm materyalleri içerir.

---

## Klasör İçeriği

| Dosya | Açıklama |
|-------|----------|
| `privacy_policy.md` | Gizlilik politikası (web'e yüklenecek) |
| `store_listing.md` | Mağaza açıklamaları, anahtar kelimeler, data safety |
| `CHECKLIST.md` | Bu dosya — adım adım kontrol listesi |

---

## ADIM ADIM KONTROL LİSTESİ

### Hazırlık (Tek Seferlik)

- [ ] **Keystore dosyasını yedekle** — `app-release.keystore` veya `.jks` dosyasını güvenli bir yere kopyala. Kaybedersen uygulamayı güncelleyemezsin!
- [ ] **Gizlilik Politikasını web'e yükle:**
  - Seçenek A: GitHub'da yeni bir repo aç → `privacy_policy.md` yükle → GitHub Pages'ı aktif et
  - Seçenek B: [notion.site](https://notion.site) → yeni sayfa oluştur → içeriği yapıştır → "Yayınla" → "Web'de paylaş" aç
  - URL örneği: `https://cyberqbit.github.io/ceptekabin-privacy`
- [ ] **Uygulama İkonu hazırla (512×512 PNG)**
  - Mevcut `ic_launcher.webp` (xxxhdpi = 192×192px) yetersiz
  - [Android Image Asset Studio](https://developer.android.com/studio/write/image-asset-studio) veya Figma kullan
  - Şeffaf arka plan OLMAMALI — tam dolu kare ikon gerekli
- [ ] **Feature Graphic hazırla (1024×500 PNG)**
  - Uygulama adı + slogan + ekran görüntüsü kompozisyonu
  - Figma şablonu önerilir: [Material Design Feature Graphic Template](https://m3.material.io/)

### AAB Dosyası Oluşturma

```powershell
cd C:\CepteKabin
.\gradlew clean bundleRelease
```

Çıktı: `app/build/outputs/bundle/release/CepteKabin-release.aab`

### Play Console Adımları

1. [play.google.com/console](https://play.google.com/console) → Uygulama Oluştur
2. **Uygulama Adı:** CepteKabin
3. **Dil:** Türkçe (tr)
4. **Tür:** Uygulama | **Fiyat:** Ücretsiz
5. Sol menü → **Uygulama İçeriği** → tüm formları doldur:
   - Gizlilik Politikası URL'si
   - Uygulama erişimi (test hesabı bilgisi)
   - Veri güvenliği (`store_listing.md`'deki tablodan doldur)
   - İçerik derecelendirmesi anketi (Herkes / 3+)
6. **Ana Mağaza Girişi** → `store_listing.md`'den kopyala-yapıştır
7. **Üretim** → Yeni Sürüm → AAB yükle → İncelemeye Gönder

### Grafik Boyutları

| Materyal | Boyut | Format |
|----------|-------|--------|
| Uygulama İkonu | 512×512 px | PNG (şeffaf bg yok) |
| Feature Graphic | 1024×500 px | PNG veya JPEG |
| Ekran Görüntüsü (telefon) | Min 320px, maks 3840px | PNG veya JPEG |
| Ekran Görüntüsü (tablet 7") | Min 320px, maks 3840px | PNG veya JPEG (isteğe bağlı) |

### Onay Süresi

İlk yayın için Google inceleme ekibi **3–7 iş günü** içinde geri dönecek.  
Sonraki güncellemeler genellikle **birkaç saat** içinde onaylanır.

---

## ÖNEMLİ NOTLAR

- `versionCode = 3` ile başlıyoruz. Play Console bunu kabul eder ama bir sonraki güncellemede `4` olmalı.
- `isMinifyEnabled = false` şu an. Play Store ProGuard/R8 şiddetle tavsiye eder — sonraki sürümde aktif et.
- `PLAY_STORE_LINK` sabiti `KombinShareHelper.kt:29`'da — Play Store linki onaylandıktan sonra güncelle.
