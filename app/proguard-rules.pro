# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ───────────────────────────────────────────────────────────────────────────
# CepteKabin: Release Modunda Gson Serileştirmesi İçin Gerekli Kurallar
# ───────────────────────────────────────────────────────────────────────────
# Sorun: Release modunda Proguard/R8 veri sınıflarının isimlerini değiştirir ve
# Gson JSON'da eşleşecek field adını bulamaz. Bu kurallar bunu engeller.

# 1. Tüm domain model sınıflarını şifreleme
-keep class com.cyberqbit.ceptekabin.domain.model.** { *; }

# 2. KombinExportData sınıfını ve tüm field'larını koru
-keep class com.cyberqbit.ceptekabin.util.KombinExportData { *; }

# 3. Gson için gerekli config (reflection kullanıyor)
-keepattributes Signature
-keepattributes *Annotation*