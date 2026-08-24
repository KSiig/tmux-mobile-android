# Keep JavaScript interface methods accessible from WebView
-keepclassmembers class com.ksiig.tmuxmobile.TmuxBridge {
    @android.webkit.JavascriptInterface <methods>;
}
