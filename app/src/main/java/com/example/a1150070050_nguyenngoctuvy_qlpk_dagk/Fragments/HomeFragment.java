package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Liên kết với layout fragment_home.xml
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}
