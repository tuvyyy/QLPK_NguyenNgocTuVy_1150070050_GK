package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.util;

import android.util.Log;

public final class LogSafe {
    private LogSafe() {}

    // Cache để không kiểm tra reflection nhiều lần
    private static volatile Boolean DEBUG_CACHE = null;

    private static boolean isDebug() {
        if (DEBUG_CACHE != null) return DEBUG_CACHE;
        try {
            // Dùng đúng package của app bạn
            Class<?> c = Class.forName("com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.BuildConfig");
            boolean debug = c.getField("DEBUG").getBoolean(null);
            DEBUG_CACHE = debug;
            return debug;
        } catch (Throwable ignore) {
            // Nếu không có BuildConfig (hoặc IDE chưa generate), mặc định KHÔNG log ở release
            DEBUG_CACHE = false;
            return false;
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug()) Log.d(tag, scrub(msg));
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug()) Log.e(tag, scrub(msg), tr);
    }

    // Ẩn thông tin nhạy cảm nếu lỡ được truyền vào log
    private static String scrub(String s) {
        if (s == null) return null;
        s = s.replaceAll("(?i)(token|access_token|authorization)\\s*[:=]\\s*[^\\s,;]+", "$1=***");
        s = s.replaceAll("(?i)(password|pass|pwd)\\s*[:=]\\s*[^\\s,;]+", "$1=***");
        return s;
    }
}
