package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.MainActivity;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;

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

    private final OkHttpClient http = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String LOGIN_URL = "http://192.168.1.6:5179/api/Users/login"; // Đảm bảo URL backend đúng
    private static final String GOOGLE_LOGIN_URL = "http://192.168.1.6:5179/api/Auth/google"; // Google Login API

    private GoogleSignInClient googleClient;
    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
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
        Button btnGoogle = findViewById(R.id.btnGoogleSignIn);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Đăng nhập với username/password
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
            loginWithUsername(user, pass);
        });

        // Đăng ký và quên mật khẩu
        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotResetActivity.class)));

        // Google Sign-In (dùng WEB CLIENT ID trong strings.xml)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.web_client_id))
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> signInLauncher.launch(googleClient.getSignInIntent()));
    }

    // Đăng nhập qua username và password
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
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> toast("Không thể kết nối server: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response res) throws IOException {
                    String body = res.body() != null ? res.body().string() : "";
                    if (!res.isSuccessful()) {
                        runOnUiThread(() -> toast("Sai tài khoản hoặc mật khẩu!"));
                        return;
                    }
                    try {
                        JSONObject obj = new JSONObject(body);
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

    // Xử lý kết quả đăng nhập qua Google
    private void handleGoogleResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount acc = task.getResult(ApiException.class);
            String idToken = acc.getIdToken();
            if (idToken == null) {
                toast("idToken=null. Kiểm tra WEB_CLIENT_ID & Android client (package+SHA1).");
                return;
            }
            sendGoogleIdToken(idToken);
        } catch (ApiException e) {
            Log.e("GoogleSignIn", "Fail code=" + e.getStatusCode(), e);
            toast("Đăng nhập Google thất bại: " + e.getStatusCode());
        }
    }

    // Gửi Google idToken lên backend để đăng nhập
    private void sendGoogleIdToken(String idToken) {
        JSONObject json = new JSONObject();
        try {
            json.put("idToken", idToken);
        } catch (JSONException ignored) {}

        Request req = new Request.Builder()
                .url(GOOGLE_LOGIN_URL)
                .post(RequestBody.create(json.toString(), JSON))
                .build();

        new Thread(() -> {
            try (Response res = http.newCall(req).execute()) {
                String body = res.body() != null ? res.body().string() : "";
                if (!res.isSuccessful()) {
                    runOnUiThread(() -> toast("Server từ chối Google token: " + res.code()));
                    return;
                }
                JSONObject obj = new JSONObject(body);
                String userName = obj.optString("userName", "User");
                String role = obj.optString("role", "User");
                runOnUiThread(() -> {
                    toast("Đăng nhập Google thành công!");
                    goMain(-1, userName, role, "");
                });
            } catch (Exception e) {
                runOnUiThread(() -> toast("Không gọi được server: " + e.getMessage()));
            }
        }).start();
    }

    // Chuyển đến màn hình chính sau khi đăng nhập thành công
    private void goMain(int id, String username, String role, String email) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
