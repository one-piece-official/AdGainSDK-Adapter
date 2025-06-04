# Consumer ProGuard rules for AdGain Adapter

-keep class com.tobid.adapter.adgain.** { *; }

-keep class com.adgain.sdk.** { *; }
-dontwarn com.adgain.**

# WindMill SDK
-keep class com.windmill.** { *; }
-dontwarn com.windmill.**