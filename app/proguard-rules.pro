# Règles ProGuard / R8 pour Mediaflix TV

# --- App : garder les activités et leurs membres (Android les instancie par réflexion)
-keep public class com.mediaflix.tv.MainActivity
-keep public class com.mediaflix.tv.PinActivity
-keep public class com.mediaflix.tv.Config

# --- Interface Javascript : sinon R8 supprime la méthode goToPin() exposée à la WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# --- WebView callbacks invoqués depuis le JS via addJavascriptInterface
-keepattributes JavascriptInterface
-keepattributes *Annotation*

# --- OkHttp : la lib se débrouille seule mais on garde les avertissements silencieux
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Kotlin coroutines / réflexion minimale
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.debug.**

# --- AndroidX (déjà couvert par les règles par défaut, en sécurité)
-keep class androidx.appcompat.app.AppCompatActivity { *; }
