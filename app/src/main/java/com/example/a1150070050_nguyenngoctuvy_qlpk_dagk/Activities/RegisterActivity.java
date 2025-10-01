package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword, etEmail;
    private Button btnRegister;
    private TextView tvLogin;

    // Dùng OkHttp client chung (đã gắn AllowedHostInterceptor)
    private final OkHttpClient http = ApiClient.get();

    // Join endpoint an toàn (không tạo "//")
    private static String api(String pathNoLeadingSlash) {
        String base = ApiClient.BASE_API;                  // vd: http://192.168.1.7:5179/
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        while (pathNoLeadingSlash.startsWith("/")) {
            pathNoLeadingSlash = pathNoLeadingSlash.substring(1);
        }
        return base + "/" + pathNoLeadingSlash;            // -> http://.../api/Users/register
    }

    private static final String EP_REGISTER = api("api/Users/register");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername        = findViewById(R.id.etUsername);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmail           = findViewById(R.id.etEmail);
        btnRegister       = findViewById(R.id.btnRegister);
        tvLogin           = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String username = text(etUsername);
        String password = text(etPassword);
        String confirm  = text(etConfirmPassword);
        String email    = text(etEmail);

        // ===== Validate cơ bản =====
        if (TextUtils.isEmpty(username)) { etUsername.setError("Vui lòng nhập tên đăng nhập!"); return; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ!"); return;
        }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Vui lòng nhập mật khẩu!"); return; }
        if (!password.equals(confirm)) { etConfirmPassword.setError("Mật khẩu không khớp!"); return; }

        // ===== Body JSON theo backend UsersController.Register =====
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            // Server của bạn đang nhận field "passwordHash" (theo UsersController)
            // -> hiện gửi plaintext để server tự hash, hoặc bạn đổi server nhận "password".
            json.put("passwordHash", password);
            json.put("email", email);
            json.put("role", "user");
        } catch (JSONException ignored) {}

        Request req = new Request.Builder()
                .url(EP_REGISTER)
                .post(RequestBody.create(json.toString(), ApiClient.JSON))
                .build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Không thể kết nối server: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(Call call, Response res) throws IOException {
                String body = res.body() != null ? res.body().string() : "";
                boolean ok = res.isSuccessful();
                res.close();

                runOnUiThread(() -> {
                    if (ok) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        // Thông điệp thân thiện cho một số lỗi thường gặp
                        String msg;
                        if (res.code() == 409) msg = "Tên đăng nhập đã tồn tại!";
                        else if (res.code() == 400) msg = "Dữ liệu không hợp lệ!";
                        else msg = "Đăng ký thất bại (" + res.code() + "): " + body;
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private static String text(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
