# libs.versions.toml — Sprint 1+2+3 güncellemeleri
# Mevcut dosyaya aşağıdaki satırları EKLE:
#
# [versions] bölümüne:
#   ucrop = "2.2.8"
#   workManager = "2.9.0"
#   hiltWork = "1.1.0"
#   gson = "2.10.1"
#
# [libraries] bölümüne:
#   ucrop = { group = "com.github.yalantis", name = "ucrop", version.ref = "ucrop" }
#   work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
#   hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hiltWork" }
#   hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hiltWork" }
#
# app/build.gradle.kts dependencies bölümüne:
#   implementation(libs.ucrop)
#   implementation(libs.work.runtime.ktx)
#   implementation(libs.hilt.work)
#   ksp(libs.hilt.work.compiler)
#
# app/build.gradle.kts android.defaultConfig bölümüne:
#   buildConfigField("String", "VERSION_NAME", "\"${android.defaultConfig.versionName}\"")
#
# AndroidManifest.xml içine (application bloğu içine):
#   <provider
#       android:name="androidx.work.impl.WorkManagerInitializer"
#       android:authorities="${applicationId}.workmanager-init"
#       android:exported="false"
#       tools:node="remove" />
#
# settings.gradle.kts repositories bölümüne:
#   maven { url = uri("https://jitpack.io") }  // uCrop için
