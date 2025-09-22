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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.SearchView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters.PatientAdapter;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Patient;

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

public class PatientsFragment extends Fragment {

    private EditText etName, etDob, etGender, etPhone, etAddress;
    private Button btnAdd;
    private RecyclerView rvPatients;
    private PatientAdapter adapter;
    private List<Patient> patientList = new ArrayList<>();
    private SearchView svPatients;

    private OkHttpClient client;
    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 👉 chỉnh IP theo backend LAN/ngrok
    private final String BASE_URL = "http://192.168.1.4:5179/api/Patients";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patients, container, false);

        etName = view.findViewById(R.id.etPatientName);
        etDob = view.findViewById(R.id.etPatientDob);
        etGender = view.findViewById(R.id.etPatientGender);
        etPhone = view.findViewById(R.id.etPatientPhone);
        etAddress = view.findViewById(R.id.etPatientAddress);
        btnAdd = view.findViewById(R.id.btnAddPatient);
        svPatients = view.findViewById(R.id.svPatients);

        rvPatients = view.findViewById(R.id.rvPatients);
        rvPatients.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PatientAdapter(getContext(), patientList, new PatientAdapter.OnPatientActionListener() {
            @Override
            public void onUpdatePatient(Patient patient) {
                updatePatient(patient);
            }

            @Override
            public void onDeletePatient(Patient patient) {
                deletePatient(patient.getId());
            }
        });
        rvPatients.setAdapter(adapter);

        client = new OkHttpClient();

        btnAdd.setOnClickListener(v -> createPatient());

        // search
        svPatients.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPatients(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    getPatients(); // reset khi xóa input
                }
                return false;
            }
        });

        // load dữ liệu ban đầu
        getPatients();

        return view;
    }

    // ==================== API CALLS ====================

    private void getPatients() {
        Request request = new Request.Builder().url(BASE_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Không kết nối được server!", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray arr = new JSONArray(respBody);
                        patientList.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Patient p = new Patient(
                                    obj.getInt("id"),
                                    obj.getString("fullName"),
                                    obj.optString("dob", ""),
                                    obj.optString("gender", ""),
                                    obj.optString("phone", ""),
                                    obj.optString("address", "")
                            );
                            patientList.add(p);
                        }
                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void searchPatients(String keyword) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/search?keyword=" + keyword)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Không kết nối được server!", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JSONArray arr = new JSONArray(respBody);
                        patientList.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Patient p = new Patient(
                                    obj.getInt("id"),
                                    obj.getString("fullName"),
                                    obj.optString("dob", ""),
                                    obj.optString("gender", ""),
                                    obj.optString("phone", ""),
                                    obj.optString("address", "")
                            );
                            patientList.add(p);
                        }
                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void createPatient() {
        try {
            JSONObject json = new JSONObject();
            json.put("fullName", etName.getText().toString());
            json.put("dob", etDob.getText().toString());
            json.put("gender", etGender.getText().toString());
            json.put("phone", etPhone.getText().toString());
            json.put("address", etAddress.getText().toString());

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder().url(BASE_URL).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi thêm bệnh nhân!", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call call, Response response) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show());
                    getPatients();
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updatePatient(Patient patient) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", patient.getId());
            json.put("fullName", patient.getFullName());
            json.put("dob", patient.getDob());
            json.put("gender", patient.getGender());
            json.put("phone", patient.getPhone());
            json.put("address", patient.getAddress());

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + patient.getId())
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
                    getPatients();
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deletePatient(int id) {
        Request request = new Request.Builder().url(BASE_URL + "/" + id).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Không xóa được!", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show());
                getPatients();
            }
        });
    }
}
