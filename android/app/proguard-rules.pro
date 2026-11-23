# Keep Capacitor core and plugin classes/methods accessed via reflection
-keep class * extends com.getcapacitor.Plugin {
    public *;
}
-keepclassmembers class * extends com.getcapacitor.Plugin {
    @com.getcapacitor.PluginMethod <methods>;
}
-keep class com.slidepulse.playgames.** { *; }
-keep class com.getcapacitor.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
