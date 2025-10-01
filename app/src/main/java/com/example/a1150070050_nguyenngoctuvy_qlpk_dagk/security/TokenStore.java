package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.security;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

public final class TokenStore {
    private static final String PREF_FILE = "secure_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private TokenStore() {}

    private static SharedPreferences getSecurePrefs(Context ctx) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREF_FILE,
                    masterKeyAlias,
                    ctx,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback cho API 21-22 hoặc khi thiết bị không hỗ trợ
            return ctx.getSharedPreferences(PREF_FILE + "_fallback", Context.MODE_PRIVATE);
        }
    }

    public static void save(Context ctx, String token) {
        getSecurePrefs(ctx).edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public static String get(Context ctx) {
        return getSecurePrefs(ctx).getString(KEY_ACCESS_TOKEN, null);
    }

    public static void clear(Context ctx) {
        getSecurePrefs(ctx).edit().remove(KEY_ACCESS_TOKEN).apply();
    }
}
