# Modelleri Sifrelemeden Koru (Cokmeleri Onlemek Icin)
-keep class com.cyberqbit.ceptekabin.domain.model.** { *; }
-keep class com.cyberqbit.ceptekabin.data.local.database.entity.** { *; }

# Retrofit & Gson
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter

# Hilt / Dagger DI
-keep class dagger.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends androidx.lifecycle.ViewModel

# Firebase & Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
