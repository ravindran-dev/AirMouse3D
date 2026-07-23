# Add project specific ProGuard rules here.

# Keep data classes serialized to/from Firebase Realtime Database
-keepclassmembers class com.airmouse3d.model.** {
    *;
}
-keep class com.airmouse3d.model.** { *; }

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
