package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.network.AllowedHostInterceptor;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public final class ApiClient {
    // ✅ IP Wi-Fi máy backend (ipconfig cho thấy IPv4 = 192.168.1.7)
    public static final String HOST   = "172.20.10.2";
    public static final int    PORT   = 5179;
    public static final String SCHEME = "http";

    // Luôn có "/" ở cuối để ghép endpoint
    public static final String BASE_API = SCHEME + "://" + HOST + ":" + PORT + "/";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static OkHttpClient client;

    private ApiClient() {}

    public static OkHttpClient get() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    // ✅ Khóa chỉ cho phép gọi đúng IP/port/scheme ở trên
                    .addInterceptor(new AllowedHostInterceptor(HOST, PORT, SCHEME))
                    .build();
        }
        return client;
    }
}
