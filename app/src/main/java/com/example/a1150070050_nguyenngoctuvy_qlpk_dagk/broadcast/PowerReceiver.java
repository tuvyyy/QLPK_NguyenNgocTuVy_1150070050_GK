// app/src/main/java/com/example/a1150070050_nguyenngoctuvy_qlpk_dagk/broadcast/PowerReceiver.java
package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PowerReceiver extends BroadcastReceiver {

    public interface Callback {
        void onPowerEvent(String message);
    }

    private final Callback callback;

    public PowerReceiver(Callback cb) {
        this.callback = cb;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        String msg = null;

        switch (action) {
            case Intent.ACTION_POWER_CONNECTED:
                msg = "Đang cắm sạc (POWER_CONNECTED)";
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                msg = "Đã rút sạc (POWER_DISCONNECTED)";
                break;
        }

        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onPowerEvent(msg);
        }
    }
}
