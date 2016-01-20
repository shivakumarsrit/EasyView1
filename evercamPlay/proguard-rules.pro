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
    void nativeSurfaceUpdate(Object);
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
-keepattributes SourceFile,LineNumberTable

