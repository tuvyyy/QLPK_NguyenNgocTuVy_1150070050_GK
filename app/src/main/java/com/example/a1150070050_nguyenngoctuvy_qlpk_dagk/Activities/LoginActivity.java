package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.MainActivity;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api.ApiClient;      // ✅ dùng ApiClient ở package api
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.security.TokenStore;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.util.LogSafe;

// ====== GOOGLE SIGN-IN ======
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
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

    // Dùng client từ ApiClient (đã gắn AllowedHostInterceptor)
    private final OkHttpClient http = ApiClient.get();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // BASE lấy từ ApiClient — tránh hard-code IP
    private static final String BASE = ApiClient.BASE_API; // "http://192.168.1.7:5179/"
    private static final String LOGIN_URL = BASE + "api/Users/login";
    private static final String GOOGLE_LOGIN_URL = BASE + "api/Auth/google";

    // GOOGLE
    private GoogleSignInClient googleClient;
    private Button btnGoogle;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    toast("Đăng nhập Google bị hủy.");
                    return;
                }
                Intent data = result.getData();
                if (data == null) {
                    toast("Không nhận được dữ liệu từ Google.");
                    return;
                }
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleGoogleResult(task);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogleSignIn);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Username / Password
        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(user)) { etUsername.setError("Vui lòng nhập tên đăng nhập!"); return; }
            if (TextUtils.isEmpty(pass)) { etPassword.setError("Vui lòng nhập mật khẩu!"); return; }
            loginWithUsername(user, pass);
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotResetActivity.class)));

        // ====== GOOGLE: cấu hình mềm (không phụ thuộc cứng vào R.string.web_client_id) ======
        String webClientId = getStringResIfExists("web_client_id"); // trả null nếu bạn chưa tạo string này
        GoogleSignInOptions.Builder gsoBuilder =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail();

        if (webClientId != null && !webClientId.isEmpty()) {
            gsoBuilder.requestIdToken(webClientId);
        } else {
            // Không có web_client_id → không yêu cầu idToken để tránh crash
            LogSafe.d("GoogleSignIn", "web_client_id chưa cấu hình, sẽ không requestIdToken()");
        }

        googleClient = GoogleSignIn.getClient(this, gsoBuilder.build());
        btnGoogle.setOnClickListener(v -> signInLauncher.launch(googleClient.getSignInIntent()));

        // Nếu backend CHƯA verify Google ID token, bạn có thể tạm tắt nút:
        // if (webClientId == null) { btnGoogle.setEnabled(false); }
    }

    // ====== Username/Password ======
    private void loginWithUsername(String username, String password) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            Request req = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(RequestBody.create(json.toString(), JSON))
                    .build();

            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> toast("Không thể kết nối server: " + e.getMessage()));
                }

                @Override public void onResponse(Call call, Response res) throws IOException {
                    String body = res.body() != null ? res.body().string() : "";
                    if (!res.isSuccessful()) {
                        runOnUiThread(() -> toast("Sai tài khoản hoặc mật khẩu!"));
                        return;
                    }
                    try {
                        JSONObject obj = new JSONObject(body);

                        // Nếu backend trả token thì lưu an toàn (tùy API: "token" hoặc "accessToken")
                        String token = obj.optString("token",
                                obj.optString("accessToken", null));
                        if (token != null && !token.isEmpty()) {
                            TokenStore.save(getApplicationContext(), token);
                        }

                        int id = obj.optInt("id", -1);
                        String uname = obj.optString("username", "");
                        String role = obj.optString("role", "user");
                        String email = obj.optString("email", "");

                        runOnUiThread(() -> goMain(id, uname, role, email));
                    } catch (JSONException e) {
                        runOnUiThread(() -> toast("Lỗi xử lý dữ liệu từ server!"));
                    }
                }
            });
        } catch (JSONException e) {
            toast("Lỗi JSON!");
        }
    }

    // ====== GOOGLE ======
    private void handleGoogleResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount acc = task.getResult(ApiException.class);
            String idToken = acc.getIdToken();
            if (idToken == null || idToken.isEmpty()) {
                toast("idToken=null. Kiểm tra web_client_id & cấu hình SHA1.");
                return;
            }
            sendGoogleIdToken(idToken);
        } catch (ApiException e) {
            LogSafe.e("GoogleSignIn", "Fail code=" + e.getStatusCode(), e);
            toast("Đăng nhập Google thất bại: " + e.getStatusCode());
        }
    }

    private void sendGoogleIdToken(String idToken) {
        JSONObject json = new JSONObject();
        try { json.put("idToken", idToken); } catch (JSONException ignored) {}

        Request req = new Request.Builder()
                .url(GOOGLE_LOGIN_URL)
                .post(RequestBody.create(json.toString(), JSON))
                .build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> toast("Không gọi được server: " + e.getMessage()));
            }

            @Override public void onResponse(Call call, Response res) throws IOException {
                String body = res.body() != null ? res.body().string() : "";
                if (!res.isSuccessful()) {
                    runOnUiThread(() -> toast("Server từ chối Google token: " + res.code()));
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(body);

                    // Lưu token nếu backend trả
                    String token = obj.optString("token",
                            obj.optString("accessToken", null));
                    if (token != null && !token.isEmpty()) {
                        TokenStore.save(getApplicationContext(), token);
                    }

                    String userName = obj.optString("userName",
                            obj.optString("username", "User"));
                    String role = obj.optString("role", "User");

                    runOnUiThread(() -> {
                        toast("Đăng nhập Google thành công!");
                        goMain(-1, userName, role, "");
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> toast("Lỗi dữ liệu Google từ server!"));
                }
            }
        });
    }

    // ====== Helpers ======
    private String getStringResIfExists(String name) {
        int id = getResources().getIdentifier(name, "string", getPackageName());
        return id != 0 ? getString(id) : null;
    }

    private void goMain(int id, String username, String role, String email) {
        // role có thể là "admin" / "user" / null
        Intent intent;
        if (role != null && role.equalsIgnoreCase("admin")) {
            intent = new Intent(LoginActivity.this, AdminActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        intent.putExtra("id", id);
        intent.putExtra("username", username);
        intent.putExtra("role", role);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
