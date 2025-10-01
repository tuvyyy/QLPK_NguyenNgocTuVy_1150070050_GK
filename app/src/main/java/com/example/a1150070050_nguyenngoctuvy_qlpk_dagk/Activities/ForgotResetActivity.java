package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api.ApiClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotResetActivity extends AppCompatActivity {

    private View pageEmail, pageOtp;
    private TextInputEditText etEmail, etOtp, etNewPass, etConfirmPass;
    private TextInputLayout tilOtp, tilNewPass, tilConfirmPass;
    private Button btnSendOtp, btnReset;

    private String rememberedEmail = "";
    private boolean otpValid = false;

    private static final int OTP_LENGTH = 6;

    // Build URL an toàn (không tạo "//")
    private static String api(String pathNoLeadingSlash) {
        String base = ApiClient.BASE_API; // ví dụ "http://192.168.1.7:5179/"
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        while (pathNoLeadingSlash.startsWith("/")) {
            pathNoLeadingSlash = pathNoLeadingSlash.substring(1);
        }
        return base + "/" + pathNoLeadingSlash;
    }

    private static final String EP_FORGOT = api("api/Users/forgot-password");
    private static final String EP_VERIFY = api("api/Users/verify-otp");
    private static final String EP_RESET  = api("api/Users/reset-password");

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_reset);

        pageEmail = findViewById(R.id.pageEmail);
        pageOtp   = findViewById(R.id.pageOtp);

        etEmail       = findViewById(R.id.etEmail);
        btnSendOtp    = findViewById(R.id.btnSendOtp);

        tilOtp        = findViewById(R.id.tilOtp);
        etOtp         = findViewById(R.id.etOtp);

        tilNewPass    = findViewById(R.id.tilNewPass);
        etNewPass     = findViewById(R.id.etNewPass);

        tilConfirmPass = findViewById(R.id.tilConfirmPass);
        etConfirmPass  = findViewById(R.id.etConfirmPass);

        btnReset      = findViewById(R.id.btnReset);

        etOtp.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        etOtp.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(OTP_LENGTH) });

        lockPasswordInputs(true);
        showEmailPage();

        btnSendOtp.setOnClickListener(v -> {
            String email = safeText(etEmail);
            if (email.isEmpty()) { tilOtp.setError("Nhập email"); return; }
            tilOtp.setError(null);
            sendOtp(email);
        });

        etOtp.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                otpValid = false;
                lockPasswordInputs(true);
                if (s.length() == OTP_LENGTH && !rememberedEmail.isEmpty()) {
                    verifyOtp(rememberedEmail, s.toString());
                }
            }
        });

        btnReset.setOnClickListener(v -> {
            if (!otpValid) { toast("OTP chưa hợp lệ"); return; }
            String p1 = safeText(etNewPass);
            String p2 = safeText(etConfirmPass);
            if (p1.isEmpty()) { tilNewPass.setError("Nhập mật khẩu mới"); return; }
            if (!p1.equals(p2)) { tilConfirmPass.setError("Xác nhận không khớp"); return; }
            tilNewPass.setError(null);
            tilConfirmPass.setError(null);
            resetPassword(rememberedEmail, safeText(etOtp), p1);
        });
    }

    private void showEmailPage() { pageEmail.setVisibility(View.VISIBLE); pageOtp.setVisibility(View.GONE); lockPasswordInputs(true); }
    private void showOtpPage() { pageEmail.setVisibility(View.GONE); pageOtp.setVisibility(View.VISIBLE); }
    private void lockPasswordInputs(boolean lock) {
        etNewPass.setEnabled(!lock); etConfirmPass.setEnabled(!lock); btnReset.setEnabled(!lock);
        float a = lock ? 0.5f : 1f; tilNewPass.setAlpha(a); tilConfirmPass.setAlpha(a); btnReset.setAlpha(a);
    }

    private void sendOtp(String email) {
        rememberedEmail = email;

        JSONObject body = new JSONObject();
        try { body.put("email", email); } catch (JSONException ignored) {}

        Request req = new Request.Builder()
                .url(EP_FORGOT) // <-- đã chuẩn, không có //
                .post(RequestBody.create(body.toString(), ApiClient.JSON))
                .build();

        ApiClient.get().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> toast("Lỗi gửi OTP: " + e.getMessage()));
            }
            @Override public void onResponse(Call call, Response res) {
                boolean ok = res.isSuccessful();
                try { if (res.body() != null) res.body().close(); } catch (Exception ignore) {}
                res.close();
                runOnUiThread(() -> {
                    if (ok) { toast("OTP đã gửi"); showOtpPage(); }
                    else { toast("Lỗi server: " + res.code()); }
                });
            }
        });
    }

    private void verifyOtp(String email, String otp) {
        JSONObject body = new JSONObject();
        try { body.put("email", email); body.put("otpCode", otp); } catch (JSONException ignored) {}

        Request req = new Request.Builder()
                .url(EP_VERIFY)
                .post(RequestBody.create(body.toString(), ApiClient.JSON))
                .build();

        ApiClient.get().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> toast("Không kiểm tra được OTP: " + e.getMessage()));
            }
            @Override public void onResponse(Call call, Response res) throws IOException {
                String bodyStr = res.body() != null ? res.body().string() : "";
                res.close();
                runOnUiThread(() -> {
                    if (res.isSuccessful()) { otpValid = true; lockPasswordInputs(false); tilOtp.setError(null); }
                    else { otpValid = false; lockPasswordInputs(true); tilOtp.setError("OTP không đúng"); }
                });
            }
        });
    }

    private void resetPassword(String email, String otp, String newPass) {
        JSONObject body = new JSONObject();
        try { body.put("email", email); body.put("otpCode", otp); body.put("newPassword", newPass); } catch (JSONException ignored) {}

        Request req = new Request.Builder()
                .url(EP_RESET)
                .post(RequestBody.create(body.toString(), ApiClient.JSON))
                .build();

        ApiClient.get().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> toast("Đổi mật khẩu lỗi: " + e.getMessage()));
            }
            @Override public void onResponse(Call call, Response res) {
                boolean ok = res.isSuccessful();
                try { if (res.body() != null) res.body().close(); } catch (Exception ignore) {}
                res.close();
                runOnUiThread(() -> {
                    if (ok) { toast("Đổi mật khẩu thành công"); finish(); }
                    else { toast("Lỗi server: " + res.code()); }
                });
            }
        });
    }

    private static String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    private void toast(String msg){ Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}
