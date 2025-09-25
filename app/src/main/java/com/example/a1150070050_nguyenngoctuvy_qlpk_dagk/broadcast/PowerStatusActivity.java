// app/src/main/java/com/example/a1150070050_nguyenngoctuvy_qlpk_dagk/PowerStatusActivity.java
package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.broadcast.PowerReceiver;

public class PowerStatusActivity extends AppCompatActivity {

    private TextView tvStatus;
    private PowerReceiver powerReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_status);

        tvStatus = findViewById(R.id.tvStatus);

        // Khởi tạo receiver, đẩy text lên UI mỗi khi có sự kiện
        powerReceiver = new PowerReceiver(message -> runOnUiThread(() -> tvStatus.setText(message)));

        // Lấy trạng thái pin hiện tại (lúc mới mở màn hình)
        showInitialBatteryState();
    }

    private void showInitialBatteryState() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter); // sticky broadcast

        if (batteryStatus == null) {
            tvStatus.setText("Không đọc được trạng thái pin ban đầu");
            return;
        }

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;

        String plugStr;
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_USB:  plugStr = "USB"; break;
            case BatteryManager.BATTERY_PLUGGED_AC:   plugStr = "AC";  break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS: plugStr = "Wireless"; break;
            default: plugStr = "Không cắm sạc";
        }

        tvStatus.setText(isCharging ? ("Đang sạc (" + plugStr + ")") : "Không sạc");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ĐĂNG KÝ BẰNG CODE: chỉ lắng nghe khi màn hình này đang mở
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_POWER_CONNECTED);
        f.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, f);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // HỦY ĐĂNG KÝ: đúng yêu cầu “Stop/Destroy thì không lắng nghe nữa”
        try { unregisterReceiver(powerReceiver); } catch (Exception ignored) {}
    }
}
