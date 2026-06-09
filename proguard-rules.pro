# MoonLoader Music ProGuard rules

# Keep NewPipe Extractor
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**

# Keep Room entities
-keep class com.moonloader.music.data.model.** { *; }

# Keep data models
-keepclassmembers class * {
    @androidx.room.* *;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ExoPlayer/Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
