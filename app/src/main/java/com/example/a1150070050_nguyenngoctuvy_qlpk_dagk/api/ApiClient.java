package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api;

import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class ApiClient {
    public static final String BASE_API = "http://192.168.0.207:5179"; // đổi nếu cần
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static OkHttpClient client;
    public static OkHttpClient get() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }
}
