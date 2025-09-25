package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.emergency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.ArrayList;

public class SmsReceiver extends BroadcastReceiver {
    public static final String ACTION_FWD = "clinic.emergency.FWD";
    public static final String EXTRA_ADDRS = "addrs";
    public static final String EXTRA_TYPES = "types"; // "SOS" | "CHECKIN"

    @Override
    public void onReceive(Context ctx, Intent it) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(it.getAction())) return;
        Bundle b = it.getExtras(); if (b == null) return;

        Object[] pdus = (Object[]) b.get("pdus");
        String format = b.getString("format");
        if (pdus == null || pdus.length == 0) return;

        ArrayList<String> addrs = new ArrayList<>();
        ArrayList<String> types = new ArrayList<>();

        for (Object p : pdus) {
            SmsMessage sms;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sms = SmsMessage.createFromPdu((byte[]) p, format);
            } else {
                sms = SmsMessage.createFromPdu((byte[]) p);
            }
            if (sms == null) continue;

            String from = sms.getOriginatingAddress();
            String body = (sms.getMessageBody() == null ? "" : sms.getMessageBody()).toLowerCase();

            String type = classify(body);
            if (type != null && from != null && !addrs.contains(from)) {
                addrs.add(from);
                types.add(type);
            }
        }

        if (addrs.isEmpty()) return;

        Intent fwd = new Intent(ACTION_FWD);
        fwd.putStringArrayListExtra(EXTRA_ADDRS, addrs);
        fwd.putStringArrayListExtra(EXTRA_TYPES, types);

        if (EmergencyActivity.isRunning) {
            ctx.sendBroadcast(fwd);
        } else {
            Intent open = new Intent(ctx, EmergencyActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            open.putExtras(fwd);
            ctx.startActivity(open);
        }
    }

    private String classify(String body) {
        String[] SOS = {"cứu","khẩn","khẩn cấp","đau ngực","khó thở","chảy máu","đột quỵ","co giật",
                "help","emergency","chest pain","stroke","bleeding","seizure"};
        for (String k : SOS) if (body.contains(k)) return "SOS";

        String[] CHECK = {"ổn không","bạn ổn chứ","ok không","are you ok","you ok","u ok"};
        for (String k : CHECK) if (body.contains(k)) return "CHECKIN";

        return null;
    }
}
