# Keep default ProGuard rules; minify is disabled for the base shell.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
