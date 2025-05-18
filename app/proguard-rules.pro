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

# Verhindert das Entfernen von App-Klassen
-keep class com.example.** { *; }

# Verhindert das Entfernen von Klassen, die über Reflection aufgerufen werden
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Bewahre alle Activities, Fragments und ViewModels
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.lifecycle.ViewModel

# Optional: Logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Erhalte generische Typinformationen (zwingend für Gson + TypeToken)
-keepattributes Signature
-keepattributes *Annotation*

# Verhindere, dass ApiResponse und seine Typinformationen gelöscht werden
-keep class com.rettungshundeEinsatzApp.functions.ApiResponse { *; }

# Verhindere, dass UserDataDto obfuskiert wird oder Felder verloren gehen
-keep class com.rettungshundeEinsatzApp.functions.UserDataDto { *; }

# Allgemeine Regel für alle Gson-Modellklassen im functions-Paket (optional aber hilfreich)
-keep class com.rettungshundeEinsatzApp.functions.** { *; }

# Wenn du eigene Gson-Feldnamen mit @SerializedName verwendest:
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}