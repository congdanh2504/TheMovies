# Preserve line numbers for crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Moshi — keep model classes and reflective adapter
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
-keep @com.squareup.moshi.JsonClass class * { *; }

# Retrofit — keep service interfaces
-keepattributes Signature, Exceptions
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Room — keep entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# kotlinx.serialization — keep @Serializable classes (navigation destinations)
-keepattributes *Annotation*
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.** { *; }

# Hilt — generated components are handled by AGP, but keep entry points
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Coil
-dontwarn coil.**
