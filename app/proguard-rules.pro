-keepattributes JavascriptInterface,Annotation*,EnclosingMethod,InnerClasses,Signature
-keepattributes SourceFile,LineNumberTable

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.almekhlafi.FPRO.** { *; }
-keep class com.example.** { *; }

-dontwarn **
