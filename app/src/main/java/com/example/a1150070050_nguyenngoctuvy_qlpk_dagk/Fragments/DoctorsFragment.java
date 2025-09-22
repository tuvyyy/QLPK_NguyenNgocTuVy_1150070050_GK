package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments;

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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters.DoctorAdapter;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Doctor;

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

    private EditText etName, etSpecialty, etPhone;
    private Button btnAdd;
    private RecyclerView rvDoctors;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList = new ArrayList<>();

    private OkHttpClient client;
    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String BASE_URL = "http://192.168.1.4:5179/api/Doctors";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctors, container, false);

        etName = view.findViewById(R.id.etDoctorName);
        etSpecialty = view.findViewById(R.id.etDoctorSpecialty);
        etPhone = view.findViewById(R.id.etDoctorPhone);
        btnAdd = view.findViewById(R.id.btnAddDoctor);

        rvDoctors = view.findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DoctorAdapter(getContext(), doctorList, new DoctorAdapter.OnDoctorActionListener() {
            @Override
            public void onUpdateDoctor(Doctor doctor) {
                updateDoctor(doctor);
            }

            @Override
            public void onDeleteDoctor(Doctor doctor) {
                deleteDoctor(doctor.getId());
            }
        });
        rvDoctors.setAdapter(adapter);

        client = new OkHttpClient();

        btnAdd.setOnClickListener(v -> createDoctor());

        // Swipe để xoá
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int pos = viewHolder.getAdapterPosition();
                        Doctor doctor = doctorList.get(pos);
                        deleteDoctor(doctor.getId());
                    }
                };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvDoctors);

        getDoctors();
        return view;
    }

    private void getDoctors() {
        Request request = new Request.Builder().url(BASE_URL).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
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
                            Doctor d = new Doctor(
                                    obj.getInt("id"),
                                    obj.getString("fullName"),
                                    obj.getString("specialty"),
                                    obj.getString("phone")
                            );
                            doctorList.add(d);
                        }
                        System.out.println(">>> Số bác sĩ load được: " + doctorList.size()); // 👈 thêm log
                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }

        });
    }

    private void createDoctor() {
        try {
            JSONObject json = new JSONObject();
            json.put("fullname", etName.getText().toString());
            json.put("specialty", etSpecialty.getText().toString());
            json.put("phone", etPhone.getText().toString());

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder().url(BASE_URL).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi thêm bác sĩ!", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call call, Response response) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show());
                    getDoctors();
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateDoctor(Doctor doctor) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", doctor.getId());
            json.put("fullname", doctor.getFullname());
            json.put("specialty", doctor.getSpecialty());
            json.put("phone", doctor.getPhone());

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + doctor.getId())
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call call, Response response) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show());
                    getDoctors();
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteDoctor(int id) {
        Request request = new Request.Builder().url(BASE_URL + "/" + id).delete().build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Không xóa được!", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show());
                getDoctors();
            }
        });
    }
}
