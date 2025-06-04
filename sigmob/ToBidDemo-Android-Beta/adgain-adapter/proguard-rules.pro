# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-keep class com.tobid.adapter.adgain.** { *; }

-keep class com.adgain.sdk.** { *; }
-dontwarn com.adgain.**

# WindMill SDK
-keep class com.windmill.** { *; }
-dontwarn com.windmill.**
