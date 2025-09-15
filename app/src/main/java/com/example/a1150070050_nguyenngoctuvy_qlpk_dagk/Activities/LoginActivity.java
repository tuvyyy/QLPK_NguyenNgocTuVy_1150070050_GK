package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Click nút đăng nhập
        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();

            if (user.equals("admin") && pass.equals("123")) {
                Intent intent = new Intent(LoginActivity.this, com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                etPassword.setError("Sai tài khoản hoặc mật khẩu!");
            }
        });

        // Click "Đăng ký ngay"
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Click "Quên mật khẩu"
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}
