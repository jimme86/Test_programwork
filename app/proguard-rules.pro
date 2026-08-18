# Add project specific ProGuard rules here.
# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.offgridlifestyle.shop.**$$serializer { *; }
-keepclassmembers class com.offgridlifestyle.shop.** {
    *** Companion;
}
-keepclasseswithmembers class com.offgridlifestyle.shop.** {
    kotlinx.serialization.KSerializer serializer(...);
}
