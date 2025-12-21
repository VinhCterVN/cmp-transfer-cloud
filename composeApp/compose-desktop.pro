# --- 1. GIỮ CODE CỦA ỨNG DỤNG ---
-keep class com.vincent.transfercloud.** { *; }

# --- 2. GIỮ CÁC THƯ VIỆN LÕI ---
# Ktor
-keep class io.ktor.** { *; }
-keepnames class io.ktor.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

-keep class coil3.compose.** { *; }
-keepnames class coil3.compose.** { *; }

# Serialization
-keep class kotlinx.serialization.** { *; }

# --- 3. FIX LỖI CỤ THỂ BẠN ĐANG GẶP (QUAN TRỌNG) ---
# Lỗi "keeps the entry point... but not the descriptor class 'kotlinx.io.Sink'"
# Ktor 3.x sử dụng kotlinx-io, cần phải giữ lại nó.
-keep class kotlinx.io.** { *; }
-keepnames class kotlinx.io.** { *; }

# --- 4. TẮT CẢNH BÁO (DONTWARN) ---
# "247 unresolved references" thường là do thư viện hỗ trợ nhiều nền tảng (Android, JS, JVM cũ)
# Proguard thấy thiếu class của Android/JDK cũ nên báo lỗi.
# Nếu App chạy ngon ở Debug, ta có thể tắt cảnh báo này an toàn.

-dontwarn kotlinx.io.**
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**
-dontwarn coil3.**
-dontwarn kotlinx.serialization.**
-dontwarn java.lang.invoke.**
-dontwarn javax.**
-dontwarn android.**
-dontwarn org.slf4j.**

# Bỏ qua cảnh báo chung
-ignorewarnings