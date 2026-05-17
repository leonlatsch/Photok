# keep readable stack traces & class names
-dontobfuscate

# keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable

# Keep generic type info (needed by Gson)
-keepattributes Signature

# Keep annotations (SerializedName etc.)
-keepattributes *Annotation*

# Keep fields
-keepclassmembers class dev.leonlatsch.photok.** {
    <fields>;
}

# Keep TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken