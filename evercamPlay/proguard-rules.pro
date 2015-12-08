# Don't show warnings for the following libraries
-dontwarn io.evercam.**
-dontwarn okio.**
-dontwarn org.joda.time.**
-dontwarn org.simpleframework.xml.**
-dontwarn com.mixpanel.android.**
-dontwarn com.google.android.gms.**

# Keep JNI methods
-keepclassmembers class **.VideoActivity {
    long native_custom_data;
    native <methods>;
    void nativeRequestSample(String);
    void nativeSetUri(String, int);
    void nativeInit();
    void nativeFinalize();
    void nativePlay();
    void nativePause();
    boolean nativeClassInit();
    void nativeSurfaceInit(Object);
    void nativeSurfaceFinalize();
    void nativeExpose();
    void onVideoLoaded();
    void onVideoLoadFailed();
    void onSampleRequestSuccess(byte[], int);
    void onSampleRequestFailed();
}

# Fix the MenuBuilder NoClassDefFoundError https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.view.menu.**,!android.support.design.internal.NavigationMenu,!android.support.design.internal.NavigationMenuPresenter,!android.support.design.internal.NavigationSubMenu,** {*;}

# Keep line numbers for Crashlytics bug reports
# Now it's included in the Splunk settings
# -keepattributes SourceFile,LineNumberTable

# Splunk suggested settings
-keep class com.splunk.** { *; }
-optimizationpasses 25
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# -printmapping out.map
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keep class com.splunk.** { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

