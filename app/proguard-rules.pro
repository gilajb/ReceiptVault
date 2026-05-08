-keep class com.receiptvault.data.local.entities.** { *; }
-keep class com.receiptvault.data.local.dao.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ML Kit
-keep class com.google.mlkit.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
