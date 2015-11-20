# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\iantoal\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# TODO Must be removed before release and we need to move back to using 23.1.1 libs
# TODO as there are so many fixes in PrefernceFragmentCompat
# TODO Project Oxford VSO task - 6751
-keep public class android.support.v7.preference.Preference { *; }
-keep public class * extends android.support.v7.preference.Preference { *; }