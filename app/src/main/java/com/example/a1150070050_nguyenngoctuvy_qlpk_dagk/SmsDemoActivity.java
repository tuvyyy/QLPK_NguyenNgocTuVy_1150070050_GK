package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.sms.SmsReceiver;

public class SmsDemoActivity extends AppCompatActivity {

    private TextView tvPerm, tvFrom, tvBody;
    private SmsReceiver smsReceiver;
    private boolean isRegistered = false;

    private final ActivityResultLauncher<String> requestSmsPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    tvPerm.setText("Trạng thái quyền: ĐÃ CẤP");
                    registerIfNeeded();
                } else {
                    tvPerm.setText("Trạng thái quyền: CHƯA CẤP (không thể nhận SMS)");
                    Toast.makeText(this, "Bạn cần cấp quyền RECEIVE_SMS để nhận tin nhắn", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_demo);

        tvPerm = findViewById(R.id.tvPerm);
        tvFrom = findViewById(R.id.tvFrom);
        tvBody = findViewById(R.id.tvBody);

        smsReceiver = new SmsReceiver((from, body) -> runOnUiThread(() -> {
            tvFrom.setText("SĐT gửi: " + from);
            tvBody.setText("Nội dung: " + body);
        }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Xin quyền runtime nếu chưa có
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {   // <-- sửa dòng này
            tvPerm.setText("Trạng thái quyền: CHƯA CẤP");
            requestSmsPerm.launch(Manifest.permission.RECEIVE_SMS);
        } else {
            tvPerm.setText("Trạng thái quyền: ĐÃ CẤP");
            registerIfNeeded();
        }
    }

    private void registerIfNeeded() {
        if (isRegistered) return;
        IntentFilter f = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        // Không cần thêm CATEGORY_DEFAULT
        registerReceiver(smsReceiver, f);
        isRegistered = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy để Stop/Destroy sẽ không còn lắng nghe (đúng yêu cầu bài)
        if (isRegistered) {
            try { unregisterReceiver(smsReceiver); } catch (Exception ignored) {}
            isRegistered = false;
        }
    }
}
