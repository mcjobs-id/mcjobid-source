# Project ProGuard / R8 Rules for MCJOBID

# Keep Domain Models & Data Classes
-keep class com.isankamil.mcjobid.domain.model.** { *; }
-keep class com.isankamil.mcjobid.data.model.** { *; }

# Keep Room Entities & DAOs
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.**

# Keep Firestore Entities & Data Field Names for Serialization
-keepclassmembers class com.isankamil.mcjobid.data.local.entity.** {
    <fields>;
    <methods>;
}

# Keep Hilt & Dependency Injection
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Firebase & Google Play Services
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Coil Image Loader
-keep class io.coilkt.** { *; }

# Room Migration
-keep class * extends androidx.room.migration.Migration { *; }
