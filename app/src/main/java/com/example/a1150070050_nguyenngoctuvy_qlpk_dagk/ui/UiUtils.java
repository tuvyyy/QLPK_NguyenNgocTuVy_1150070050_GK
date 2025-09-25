package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class UiUtils {
    public static void toast(Context c, String msg){ Toast.makeText(c, msg, Toast.LENGTH_SHORT).show(); }
    public static void hideKeyboard(Activity a, View v){
        InputMethodManager imm=(InputMethodManager)a.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm!=null) imm.hideSoftInputFromWindow(v.getWindowToken(),0);
    }
}
