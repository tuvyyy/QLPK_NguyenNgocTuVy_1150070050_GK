package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters.DoctorAdapter;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DoctorsFragment extends Fragment {

    private EditText etId, etName, etSpecialty, etPhone;
    private Button btnAdd, btnUpdate, btnDelete, btnGet;
    private RecyclerView rvDoctors;
    private DoctorAdapter adapter;
    private List<String> doctorList = new ArrayList<>();

    private OkHttpClient client;
    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 👉 Đổi sang ngrok hoặc IP LAN backend
    private final String BASE_URL = "https://4f438d1f500c.ngrok-free.app/api/Doctors";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctors, container, false);

        etId = view.findViewById(R.id.etDoctorId);
        etName = view.findViewById(R.id.etDoctorName);
        etSpecialty = view.findViewById(R.id.etDoctorSpecialty);
        etPhone = view.findViewById(R.id.etDoctorPhone);

        btnAdd = view.findViewById(R.id.btnAddDoctor);
        btnUpdate = view.findViewById(R.id.btnUpdateDoctor);
        btnDelete = view.findViewById(R.id.btnDeleteDoctor);
        btnGet = view.findViewById(R.id.btnGetDoctors);

        rvDoctors = view.findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DoctorAdapter(doctorList);
        rvDoctors.setAdapter(adapter);

        client = new OkHttpClient();

        btnAdd.setOnClickListener(v -> createDoctor());
        btnUpdate.setOnClickListener(v -> updateDoctor());
        btnDelete.setOnClickListener(v -> deleteDoctor());
        btnGet.setOnClickListener(v -> getDoctors());

        return view;
    }

    private void getDoctors() {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Không kết nối được server!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray arr = new JSONArray(respBody);
                        doctorList.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String info = obj.getInt("id") + " - " +
                                    obj.getString("name") + " (" +
                                    obj.getString("specialty") + ")";
                            doctorList.add(info);
                        }
                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void createDoctor() {
        try {
            JSONObject json = new JSONObject();
            json.put("name", etName.getText().toString());
            json.put("specialty", etSpecialty.getText().toString());
            json.put("phone", etPhone.getText().toString());

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi thêm bác sĩ!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show());
                    getDoctors();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDoctor() {
        try {
            int id = Integer.parseInt(etId.getText().toString());
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", etName.getText().toString());
            json.put("specialty", etSpecialty.getText().toString());
            json.put("phone", etPhone.getText().toString());

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + id)
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show());
                    getDoctors();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteDoctor() {
        int id = Integer.parseInt(etId.getText().toString());
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Không xóa được!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show());
                getDoctors();
            }
        });
    }
}
