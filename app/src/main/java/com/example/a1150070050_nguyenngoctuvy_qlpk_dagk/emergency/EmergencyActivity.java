package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.emergency;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat; // <- dùng registerReceiver compat & flags

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;

import java.util.ArrayList;

public class EmergencyActivity extends AppCompatActivity {
    public static boolean isRunning = false;

    private Switch swAutoSOS, swAutoReply;
    private EditText edtHotline, edtSafeMsg, edtSosMsg;
    private Button btnSave, btnCallHotline, btnReplySafe, btnReplySOS;
    private ListView lv;
    private ArrayAdapter<String> ad;
    private final ArrayList<String> sosList = new ArrayList<>();
    private final ArrayList<String> checkList = new ArrayList<>();

    private static final String SP = "clinic_emg";
    private static final int REQ_PERM = 9911;

    private final BroadcastReceiver inApp = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) {
            handleIncoming(
                    i.getStringArrayListExtra(SmsReceiver.EXTRA_ADDRS),
                    i.getStringArrayListExtra(SmsReceiver.EXTRA_TYPES)
            );
        }
    };

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_emergency);

        swAutoSOS = findViewById(R.id.sw_auto_sos);
        swAutoReply = findViewById(R.id.sw_auto_reply);
        edtHotline = findViewById(R.id.edt_hotline);
        edtSafeMsg = findViewById(R.id.edt_safe);
        edtSosMsg  = findViewById(R.id.edt_sos);
        btnSave = findViewById(R.id.btn_save);
        btnCallHotline = findViewById(R.id.btn_call_hotline);
        btnReplySafe = findViewById(R.id.btn_reply_safe);
        btnReplySOS  = findViewById(R.id.btn_reply_sos);
        lv = findViewById(R.id.lv_messages);

        ad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lv.setAdapter(ad);

        boolean autoSOS   = getSharedPreferences(SP, MODE_PRIVATE).getBoolean("auto_sos", true);
        boolean autoReply = getSharedPreferences(SP, MODE_PRIVATE).getBoolean("auto_reply", false);
        String  hotline   = getSharedPreferences(SP, MODE_PRIVATE).getString("hotline", "0900000000");
        String  msgSafe   = getSharedPreferences(SP, MODE_PRIVATE).getString("msg_safe",
                "Em/anh vẫn ổn, đang an toàn. Cảm ơn đã quan tâm.");
        String  msgSos    = getSharedPreferences(SP, MODE_PRIVATE).getString("msg_sos",
                "Bạn đang liên hệ khẩn cấp. Nếu đau ngực/đột quỵ gọi 115 ngay. Hotline cấp cứu: [HOTLINE]. Trả lời 1 nếu cần gọi lại.");

        swAutoSOS.setChecked(autoSOS);
        swAutoReply.setChecked(autoReply);
        edtHotline.setText(hotline);
        edtSafeMsg.setText(msgSafe);
        edtSosMsg.setText(msgSos);

        btnSave.setOnClickListener(v -> {
            getSharedPreferences(SP, MODE_PRIVATE).edit()
                    .putBoolean("auto_sos", swAutoSOS.isChecked())
                    .putBoolean("auto_reply", swAutoReply.isChecked())
                    .putString("hotline", edtHotline.getText().toString().trim())
                    .putString("msg_safe", edtSafeMsg.getText().toString())
                    .putString("msg_sos", edtSosMsg.getText().toString())
                    .apply();
            Toast.makeText(this, "Đã lưu cấu hình", Toast.LENGTH_SHORT).show();
        });

        btnCallHotline.setOnClickListener(v -> {
            String hl = edtHotline.getText().toString().trim();
            if (hl.isEmpty()) { Toast.makeText(this, "Chưa nhập số hotline", Toast.LENGTH_SHORT).show(); return; }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQ_PERM);
                return;
            }
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + hl)));
        });

        btnReplySafe.setOnClickListener(v -> manualReply(true));
        btnReplySOS.setOnClickListener(v -> manualReply(false));

        handleIncoming(
                getIntent().getStringArrayListExtra(SmsReceiver.EXTRA_ADDRS),
                getIntent().getStringArrayListExtra(SmsReceiver.EXTRA_TYPES)
        );

        ensurePerms();
    }

    @Override protected void onResume() {
        super.onResume();
        isRunning = true;

        // ✅ Đăng ký bằng ContextCompat để có flag ở mọi API → hết cảnh báo
        IntentFilter f = new IntentFilter(SmsReceiver.ACTION_FWD);
        ContextCompat.registerReceiver(
                this,
                inApp,
                f,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        refreshList();
    }

    @Override protected void onPause() {
        super.onPause();
        isRunning = false;
        try { unregisterReceiver(inApp); } catch (Exception ignore) {}
    }

    private void handleIncoming(ArrayList<String> addrs, ArrayList<String> types) {
        if (addrs == null || types == null) return;
        for (int i = 0; i < addrs.size(); i++) {
            String a = addrs.get(i), t = types.get(i);
            if ("SOS".equals(t)) { if (!sosList.contains(a)) sosList.add(a); }
            else { if (!checkList.contains(a)) checkList.add(a); }
        }
        refreshList();
        autoRespondIfNeeded();
    }

    private void refreshList() {
        ArrayList<String> all = new ArrayList<>();
        for (String s : sosList)   all.add("SOS • " + s);
        for (String s : checkList) all.add("CHECK • " + s);
        ad.clear(); ad.addAll(all); ad.notifyDataSetChanged();
    }

    private void autoRespondIfNeeded() {
        if (!ensurePerms()) return;
        SmsManager sm = SmsManager.getDefault();
        String hotline = getSharedPreferences(SP, MODE_PRIVATE).getString("hotline", "");
        String msgSafe = getSharedPreferences(SP, MODE_PRIVATE).getString("msg_safe", "");
        String msgSos  = getSharedPreferences(SP, MODE_PRIVATE).getString("msg_sos", "").replace("[HOTLINE]", hotline);

        if (swAutoSOS.isChecked()) {
            for (String to : new ArrayList<>(sosList)) sm.sendTextMessage(to, null, msgSos, null, null);
        }
        if (swAutoReply.isChecked()) {
            for (String to : new ArrayList<>(checkList)) { sm.sendTextMessage(to, null, msgSafe, null, null); checkList.remove(to); }
            refreshList();
        }
    }

    private void manualReply(boolean safe) {
        if (!ensurePerms()) return;
        SmsManager sm = SmsManager.getDefault();
        String hotline = getSharedPreferences(SP, MODE_PRIVATE).getString("hotline", "");
        String msgSafe = edtSafeMsg.getText().toString();
        String msgSos  = edtSosMsg.getText().toString().replace("[HOTLINE]", hotline);

        if (safe) {
            for (String to : new ArrayList<>(checkList)) { sm.sendTextMessage(to, null, msgSafe, null, null); checkList.remove(to); }
            Toast.makeText(this, "Đã trả lời An toàn", Toast.LENGTH_SHORT).show();
        } else {
            for (String to : new ArrayList<>(sosList)) sm.sendTextMessage(to, null, msgSos, null, null);
            Toast.makeText(this, "Đã gửi hướng dẫn SOS", Toast.LENGTH_SHORT).show();
        }
        refreshList();
    }

    private boolean ensurePerms() {
        String[] ps = { Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE };
        ArrayList<String> need = new ArrayList<>();
        for (String p : ps) if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) need.add(p);
        if (!need.isEmpty()) { ActivityCompat.requestPermissions(this, need.toArray(new String[0]), REQ_PERM); return false; }
        return true;
    }

    @Override public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
    }
}
