# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Règles pour le Storage Access Framework (SAF) ---

# Préserver les classes liées au Storage Access Framework
-keep class androidx.documentfile.provider.** { *; }

# Préserver les classes de modèle pour GSON
-keep class fr.bdst.aatt.data.util.DatabaseBackup { *; }
-keep class fr.bdst.aatt.data.model.** { *; }

# Règles pour GSON
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Règles CRITIQUES pour préserver les informations de type générique utilisées par GSON
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Préserver les classes pour les URI persistants de SAF
-keepclassmembers class * {
    @android.content.Intent.persistable <fields>;
}
-keep class android.net.Uri { *; }

# Autres règles pour assurer la compatibilité en release
-keep class fr.bdst.aatt.data.util.SAFBackupHelper { *; }
-keep class fr.bdst.aatt.data.util.SAFBackupHelper$BackupInfo { *; }
-keep class fr.bdst.aatt.data.util.Converters { *; }