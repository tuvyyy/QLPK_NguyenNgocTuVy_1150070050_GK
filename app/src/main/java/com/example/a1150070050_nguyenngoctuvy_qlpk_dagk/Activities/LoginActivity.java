package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.MainActivity;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private OkHttpClient client;

    private final String BASE_URL = "http://192.168.1.4:5179/api/Users/login";
    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        client = new OkHttpClient();

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(user)) {
                etUsername.setError("Vui lòng nhập tên đăng nhập!");
                return;
            }
            if (TextUtils.isEmpty(pass)) {
                etPassword.setError("Vui lòng nhập mật khẩu!");
                return;
            }

            checkLogin(user, pass);
        });

        // Chuyển sang Đăng ký
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Chuyển sang Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void checkLogin(String username, String password) {
        try {
            // ✅ JSON body đúng với LoginDto bên backend
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);   // Đúng với LoginDto

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL)  // POST /api/Users/login
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, "Không thể kết nối server!", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respBody = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    try {
                        JSONObject obj = new JSONObject(respBody);

                        int id = obj.optInt("id", -1);
                        String username = obj.optString("username", "");
                        String role = obj.optString("role", "user");
                        String email = obj.optString("email", "");

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // 👉 Chuyển sang MainActivity và truyền thông tin user
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("id", id);
                            intent.putExtra("username", username);
                            intent.putExtra("role", role);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Lỗi xử lý dữ liệu từ server!", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi JSON!", Toast.LENGTH_SHORT).show();
        }
    }

}
