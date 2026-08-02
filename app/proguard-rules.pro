# WordBopper R8 rules

# Preserve stack trace line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ViewModel subclasses — R8 may strip constructors used by ViewModelProvider
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Kotlin data classes used as state/model objects keep their fields
-keepclassmembers class com.marconius.wordbopper.model.** {
    *;
}

# Kotlin enums — keep name() and values() for any enum in the app
-keepclassmembers enum com.marconius.wordbopper.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# AudioEngine uses Android's MediaPlayer/SoundPool via reflection paths; keep it intact
-keep class com.marconius.wordbopper.audio.AudioEngine { *; }

# Jetpack Compose — the compiler plugin generates classes with synthetic names
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }


# Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**
