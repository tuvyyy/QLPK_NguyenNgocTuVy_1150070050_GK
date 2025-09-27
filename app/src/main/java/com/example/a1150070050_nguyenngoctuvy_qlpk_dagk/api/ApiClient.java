package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api;

import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class ApiClient {
    // Sử dụng IP của máy tính trong mạng LAN (192.168.1.6)
    public static final String BASE_API = "http://192.168.1.6:5179"; // Đảm bảo đúng IP máy chủ backend
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static OkHttpClient client;

    public static OkHttpClient get() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS) // Timeout kết nối
                    .readTimeout(15, TimeUnit.SECONDS)    // Timeout đọc dữ liệu
                    .writeTimeout(15, TimeUnit.SECONDS)   // Timeout ghi dữ liệu
                    .build();
        }
        return client;
    }
}

