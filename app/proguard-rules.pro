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

# PDF Viewer - WebView JavaScript interface
-keepclassmembers class com.rejowan.pdfreaderpro.presentation.components.pdf.WebInterface {
   public *;
}

# PDF Viewer - Exception classes
-keep class com.rejowan.pdfreaderpro.presentation.components.pdf.PdfException
-keep class * extends com.rejowan.pdfreaderpro.presentation.components.pdf.PdfException

# ==========================================
# Logging - Strip in release builds
# ==========================================

# Remove Timber logging calls
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}

# Remove Android Log calls (in case any direct calls exist)
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# ==========================================
# Room Database
# ==========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# ==========================================
# iText PDF
# ==========================================
-keep class com.itextpdf.** { *; }
-keep class org.bouncycastle.** { *; }

# iText - Ignore missing Java SE classes (not available on Android)
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.xml.stream.**
-dontwarn com.itextpdf.bouncycastlefips.**
-dontwarn com.itextpdf.eutrustedlistsresources.**
-dontwarn sharpen.config.**
-dontwarn aQute.bnd.annotation.**
-dontwarn org.codehaus.stax2.**
-dontwarn com.ctc.wstx.**

# ==========================================
# Kotlin Serialization
# ==========================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializable classes
-keep,includedescriptorclasses class com.rejowan.pdfreaderpro.**$$serializer { *; }
-keepclassmembers class com.rejowan.pdfreaderpro.** {
    *** Companion;
}
-keepclasseswithmembers class com.rejowan.pdfreaderpro.** {
    kotlinx.serialization.KSerializer serializer(...);
}