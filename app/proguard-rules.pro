# ===========================================================
# ProGuard / R8 rules – Smart-Glasses
# Compatible Java 17, Firebase, Glide, OkHttp, gRPC
# ===========================================================

# -------------------------
# Garder GlobalNotificationService et ses classes internes
# -------------------------
-keep class com.example.lado.services.GlobalNotificationService$* { *; }

# -------------------------
# Garder Firebase (réflexion)
# -------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# -------------------------
# Garder les modèles de données Firebase
# -------------------------
-keep class com.example.lado.Models.** { *; }

# -------------------------
# Garder Glide
# -------------------------
-keep class com.bumptech.glide.** { *; }
-keep interface com.bumptech.glide.** { *; }
-keep class com.bumptech.glide.load.resource.bitmap.** { *; }

# -------------------------
# Garder OkHttp et gRPC
# -------------------------
-keep class com.squareup.okhttp.** { *; }
-keep class io.grpc.okhttp.** { *; }
-keep class io.grpc.** { *; }
-keepclassmembers class com.squareup.okhttp.** { *; }
-keepclassmembers class io.grpc.okhttp.** { *; }

# -------------------------
# Garder les annotations et classes générées
# -------------------------
-keep class javax.annotation.** { *; }
-keep class com.google.protobuf.** { *; }

# -------------------------
# Garder toutes les classes internes anonymes et constructeurs (Java 17)
# -------------------------
-keepclassmembers class * {
    <init>(...);
}
-keepattributes InnerClasses, Signature, SourceFile, LineNumberTable, EnclosingMethod

# -------------------------
# Éviter la suppression de ressources importantes
# -------------------------
-keep class * implements java.io.Serializable { *; }
-keepclassmembers class * implements java.io.Serializable { *; }

# -------------------------
# Optionnel : pour le debug
# -------------------------
-keepattributes *Annotation*
