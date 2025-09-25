package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    public interface Callback {
        void onSms(String from, String body);
    }

    private final Callback cb;

    public SmsReceiver(Callback callback) {
        this.cb = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        // "android.provider.Telephony.SMS_RECEIVED"
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.getString("format"); // for API 23+

        if (pdus == null || pdus.length == 0) return;

        String from = null;
        StringBuilder body = new StringBuilder();

        for (Object pdu : pdus) {
            SmsMessage sms;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sms = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                sms = SmsMessage.createFromPdu((byte[]) pdu);
            }
            if (sms == null) continue;

            if (from == null) from = sms.getDisplayOriginatingAddress();
            body.append(sms.getMessageBody());
        }

        if (from != null) {
            Toast.makeText(context, "Hey! You have a new message", Toast.LENGTH_SHORT).show();
            if (cb != null) cb.onSms(from, body.toString());
        }
    }
}
