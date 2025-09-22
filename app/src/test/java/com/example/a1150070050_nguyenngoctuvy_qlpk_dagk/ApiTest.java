package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk;

import okhttp3.*;
import org.junit.Test;
import java.io.IOException;

public class ApiTest {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "http://192.168.0.207:5179/api/Users";
    // Nếu sau này chạy bằng emulator → đổi localhost thành 10.0.2.2

    // GET ALL USERS
    @Test
    public void testGetUsers() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("GET Users: " + response.body().string());
        }
    }

    // ✅ POST CREATE USER
    @Test
    public void testCreateUser() throws IOException {
        String json = "{"
                + "\"username\":\"testuser\","
                + "\"email\":\"testuser@example.com\","
                + "\"passwordHash\":\"123456\","
                + "\"role\":\"user\""
                + "}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("POST User: " + response.body().string());
        }
    }

    // ✅ PUT UPDATE USER (ví dụ sửa user id = 1)
    @Test
    public void testUpdateUser() throws IOException {
        int id = 1; // đổi id nếu cần
        String json = "{"
                + "\"id\":" + id + ","
                + "\"username\":\"updateduser\","
                + "\"email\":\"updated@example.com\","
                + "\"passwordHash\":\"654321\","
                + "\"role\":\"admin\""
                + "}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("PUT User: " + response.body().string());
        }
    }

    @Test
    public void testDeleteUser() throws IOException {
        int id = 2;
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("DELETE User (id=" + id + "): " + response.code());
        }
    }
}
