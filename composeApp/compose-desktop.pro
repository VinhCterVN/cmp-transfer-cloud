-keep class com.vincent.transfercloud.** { *; }

# Ktor
-keep class io.ktor.** { *; }
-keepnames class io.ktor.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

-keep class coil3.compose.** { *; }
-keepnames class coil3.compose.** { *; }

# Serialization
-keep class kotlinx.serialization.** { *; }

-keep class kotlinx.io.** { *; }
-keepnames class kotlinx.io.** { *; }

-dontwarn kotlinx.io.**
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**
-dontwarn coil3.**
-dontwarn kotlinx.serialization.**
-dontwarn java.lang.invoke.**
-dontwarn javax.**
-dontwarn android.**
-dontwarn org.slf4j.**

-ignorewarnings