# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep original source file and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Hilt/Dagger rules
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ComponentManager { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.UnsafeCasts { *; }
-keep @dagger.hilt.InstallIn class *
-keep @dagger.Module class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# Keep ViewModel subclasses
-keep public class * extends androidx.lifecycle.ViewModel { *; }

# Jsoup
-keep class org.jsoup.** { *; }

# Scraper Sources & Domain Models
-keep class my.noveldokusha.scraper.sources.** { *; }
-keep class my.noveldokusha.scraper.SourceInterface* { *; }
-keep class my.noveldokusha.scraper.domain.** { *; }
-keep class my.noveldokusha.core.** { *; }

# Interceptors & Bypass Result (Fix for startup crash if R8 strips them)
-keep class my.noveldokusha.network.interceptors.** { *; }
-keep class my.noveldokusha.network.interceptors.CloudfareVerificationInterceptor { *; }
-keep class my.noveldokusha.network.interceptors.CloudfareVerificationInterceptor$BypassResult { *; }

# Kotlin Serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class my.noveldokusha.scraper.domain.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements com.google.gson.TypeAdapterFactory
-keep public class * implements com.google.gson.JsonSerializer
-keep public class * implements com.google.gson.JsonDeserializer

# AndroidX Navigation
-keep class * extends androidx.navigation.NavArgs { *; }

# WorkManager
-keep class * extends androidx.work.ListenableWorker { *; }

# Timber
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# General stability
-ignorewarnings
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}