package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khai báo view
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        // Xử lý khi bấm Đăng ký
        btnRegister.setOnClickListener(v -> {
            // TODO: Viết code lưu thông tin đăng ký vào DB
            // Tạm thời cho quay về Login
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Xử lý khi bấm "Đăng nhập ngay"
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
