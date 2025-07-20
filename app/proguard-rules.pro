# -----------------------------
# Allgemeine Regeln
# -----------------------------

# Verhindert das Entfernen von App-Klassen
-keep class com.example.** { *; }
-keep class com.rettungshundeEinsatzApp.** { *; }

# Behalte alle Klassen, die mit @Keep markiert sind
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Activities, Fragments, ViewModels vollständig behalten
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.lifecycle.ViewModel

# Logging entfernen im Release-Build (optional)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# -----------------------------
# Gson-spezifisch (zwingend)
# -----------------------------

# Bewahre alle Generics und Annotationen – wichtig für TypeToken und Retrofit/Gson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# Behalte alle Klassen mit @SerializedName-Feldern
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# -----------------------------
# Retrofit + API-spezifisch
# -----------------------------

# Behalte dein Retrofit-Service-Interface vollständig
-keep interface com.rettungshundeEinsatzApp.functions.areas.ApiService { *; }

# Behalte alle beteiligten API-Modelle vollständig
-keep class com.rettungshundeEinsatzApp.functions.areas.UploadAreasRequest { *; }
-keep class com.rettungshundeEinsatzApp.functions.areas.UploadAreasResponse { *; }
-keep class com.rettungshundeEinsatzApp.functions.areas.UploadArea { *; }
-keep class com.rettungshundeEinsatzApp.functions.areas.UploadAreaPoint { *; }
-keep class com.rettungshundeEinsatzApp.functions.areas.DownloadAreasResponse { *; }
-keep class com.rettungshundeEinsatzApp.functions.areas.DownloadArea { *; }

# Behalte alle Klassen im Bereich "functions.areas" vollständig (zur Sicherheit)
-keep class com.rettungshundeEinsatzApp.functions.areas.** { *; }

# Optional: falls andere functions-Pakete auch Modelle enthalten
-keep class com.rettungshundeEinsatzApp.functions.** { *; }

# Behalte Retrofit-Interfaces und HTTP-Annotierungen
-keep interface retrofit2.Call
-keep interface retrofit2.http.* { *; }

# -----------------------------
# Optional: falls du Room oder andere Annotationsverarbeitung nutzt
# -----------------------------

# Room-Support für R8
-keep class androidx.room.Entity
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Gson: Felder mit @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}