# === TuitionOS ProGuard Rules ===

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# === Room ===
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# === Moshi ===
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep class com.example.data.*RoomEntity { *; }
-keep class com.example.data.DataExportImportManager$ExportBundle { *; }

# === Retrofit / OkHttp ===
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* *;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class retrofit2.** { *; }

# === BCrypt ===
-keep class at.favre.lib.crypto.bcrypt.** { *; }
-dontwarn at.favre.lib.crypto.bcrypt.**

# === EncryptedSharedPreferences ===
-keep class androidx.security.crypto.** { *; }

# === Kotlin Coroutines ===
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# === Compose ===
# Compose works fine with R8 by default, no broad keep needed

# === Coil ===
-dontwarn coil.**
-keep class coil.** { *; }

# === Keep data classes for JSON serialization ===
-keep class com.example.data.TuitionEntities { *; }
-keep class com.example.data.StudentEntity { *; }
-keep class com.example.data.BatchEntity { *; }
-keep class com.example.data.AttendanceRecordEntity { *; }
-keep class com.example.data.FeeHistoryEntity { *; }
-keep class com.example.data.LeadEntity { *; }
-keep class com.example.data.StaffEntity { *; }
-keep class com.example.data.SettingsEntity { *; }

# === Enum classes ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === Supabase / Ktor / KotlinX Serialization ===
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# KotlinX Serialization
-keep @kotlinx.serialization.Serializable class **$*Companion {*;}
-keep @kotlinx.serialization.Serializable class * {*;}
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Supabase
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# Supabase Cloud models
-keep class com.example.data.CloudRepository$Cloud* { *; }
