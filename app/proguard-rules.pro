# Kotlinx serializer
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.github.brewin.mvicoroutines.**$$serializer { *; }
-keepclassmembers class com.github.brewin.mvicoroutines.** { # App package name
    *** Companion;
}
-keepclasseswithmembers class com.github.brewin.mvicoroutines.** { # App package name
    kotlinx.serialization.KSerializer serializer(...);
}