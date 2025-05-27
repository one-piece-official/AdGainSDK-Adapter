# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep AdGain Adapter classes
-keep class com.union_test.toutiao.adgain.** { *; }

# Keep AdGain SDK classes
-keep class com.adgain.sdk.** { *; }

# Keep ByteDance Mediation classes
-keep class com.bytedance.sdk.openadsdk.mediation.** { *; } 