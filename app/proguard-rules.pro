# KnowSpends Proguard / R8 rules

# Room Database, Entities, and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep class com.knowspends.app.data.local.entity.** { *; }
-keep interface com.knowspends.app.data.local.dao.** { *; }
-dontwarn androidx.room.paging.**

# Domain Models
-keep class com.knowspends.app.domain.model.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }

# WorkManager Workers
-keep class com.knowspends.app.work.** extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Jetpack Glance Widgets
-keep class com.knowspends.app.ui.widget.** extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class com.knowspends.app.ui.widget.** extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# Hilt / Dagger
-dontwarn com.google.errorprone.annotations.**
