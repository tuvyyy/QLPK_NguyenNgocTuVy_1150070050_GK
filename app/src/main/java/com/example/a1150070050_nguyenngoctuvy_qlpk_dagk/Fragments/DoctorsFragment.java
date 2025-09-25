package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters.DoctorAdapter;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Doctor;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class DoctorsFragment extends Fragment {

    // ===== ĐỔI IP/PORT Ở ĐÂY =====
    private static final String DOCTORS_URL = "http://192.168.1.9:5179/api/Doctors";

    private EditText etSearch, etName, etPhone;
    private Spinner spFilterSpecialty, spInputSpecialty;
    private Button btnSearch, btnAdd;
    private RecyclerView rv;
    private DoctorAdapter adapter;

    private final List<Doctor> data = new ArrayList<>();
    private final List<String> specialties = new ArrayList<>(); // nguồn cho cả 2 spinner

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private String currentQuery = "";
    private String currentFilterSpecialty = ""; // "" = tất cả

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctors, container, false);

        // bind views
        etSearch = v.findViewById(R.id.etSearch);
        btnSearch = v.findViewById(R.id.btnSearch);
        spFilterSpecialty = v.findViewById(R.id.spFilterSpecialty);

        etName = v.findViewById(R.id.etName);
        etPhone = v.findViewById(R.id.etPhone);
        spInputSpecialty = v.findViewById(R.id.spInputSpecialty);
        btnAdd = v.findViewById(R.id.btnAdd);

        rv = v.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DoctorAdapter(data, new DoctorAdapter.Listener() {
            @Override public void onEdit(Doctor d) { showEditDialog(d); }
            @Override public void onDelete(Doctor d) { confirmDelete(d.getId()); }
        });
        rv.setAdapter(adapter);

        // setup 2 dropdown từ cùng 1 danh sách "specialties"
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        ArrayAdapter<String> inputAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());

        spFilterSpecialty.setAdapter(filterAdapter);
        spInputSpecialty.setAdapter(inputAdapter);

        // load specialties -> đổ vào cả 2 adapter
        loadSpecialties(new OnSpecialtyLoaded() {
            @Override public void done(List<String> list) {
                // cho filter: thêm "Tất cả" đầu tiên
                List<String> filterList = new ArrayList<>();
                filterList.add("Tất cả");
                filterList.addAll(list);

                filterAdapter.clear();
                filterAdapter.addAll(filterList);
                filterAdapter.notifyDataSetChanged();

                // cho input: thêm "(Chọn chuyên khoa)" đầu tiên
                List<String> inputList = new ArrayList<>();
                inputList.add("(Chọn chuyên khoa)");
                inputList.addAll(list);

                inputAdapter.clear();
                inputAdapter.addAll(inputList);
                inputAdapter.notifyDataSetChanged();

                spFilterSpecialty.setSelection(0);
                spInputSpecialty.setSelection(0);

                // sau khi có specialties thì load list
                triggerSearch();
            }
        });

        btnSearch.setOnClickListener(v1 -> {
            currentQuery = etSearch.getText().toString().trim();
            currentFilterSpecialty = (spFilterSpecialty.getSelectedItemPosition() <= 0)
                    ? "" : spFilterSpecialty.getSelectedItem().toString();
            triggerSearch();
        });

        btnAdd.setOnClickListener(v12 -> createDoctor());

        return v;
    }

    private void toast(String m){ Toast.makeText(getContext(), m, Toast.LENGTH_SHORT).show(); }

    // ==== API: specialties ====
    private interface OnSpecialtyLoaded { void done(List<String> list); }
    private void loadSpecialties(OnSpecialtyLoaded cb) {
        Request req = new Request.Builder().url(DOCTORS_URL + "/specialties").build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // nếu lỗi mạng, vẫn cho tạo tay: dùng list rỗng
                requireActivity().runOnUiThread(() -> {
                    toast("Không tải được danh sách chuyên khoa");
                    cb.done(Collections.emptyList());
                });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String s = response.body()!=null?response.body().string():"";
                List<String> arr = new ArrayList<>();
                if (response.isSuccessful()) {
                    try {
                        JSONArray j = new JSONArray(s);
                        for (int i=0;i<j.length();i++) arr.add(j.getString(i));
                    } catch (Exception ignored) {}
                }
                specialties.clear(); specialties.addAll(arr);
                requireActivity().runOnUiThread(() -> cb.done(arr));
            }
        });
    }

    // ==== API: list/search + filter client-side ====
    private void triggerSearch() {
        String url = DOCTORS_URL + "?page=1&pageSize=500&sort=name_asc";
        if (!currentQuery.isEmpty()) url += "&q=" + currentQuery;

        Request req = new Request.Builder().url(url).build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> toast("Lỗi mạng"));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                String body = resp.body()!=null?resp.body().string():"";
                if (!resp.isSuccessful()) return;
                try{
                    JSONObject o = new JSONObject(body);
                    JSONArray arr = o.getJSONArray("items");
                    List<Doctor> all = new ArrayList<>();
                    for (int i=0;i<arr.length();i++){
                        JSONObject it = arr.getJSONObject(i);
                        all.add(new Doctor(
                                it.getInt("id"),
                                it.optString("fullName",""),
                                it.optString("specialty",""),
                                it.isNull("phone")?null:it.getString("phone")
                        ));
                    }
                    // filter theo chuyên khoa ở spinner (nếu có chọn)
                    List<Doctor> filtered = new ArrayList<>();
                    for (Doctor d: all){
                        if (currentFilterSpecialty.isEmpty() ||
                                currentFilterSpecialty.equalsIgnoreCase(d.getSpecialty()))
                            filtered.add(d);
                    }
                    requireActivity().runOnUiThread(() -> {
                        data.clear(); data.addAll(filtered); adapter.notifyDataSetChanged();
                    });
                }catch (Exception ignore){}
            }
        });
    }

    // ==== Create ====
    private void createDoctor(){
        String name = etName.getText().toString().trim();
        String spec = (spInputSpecialty.getSelectedItemPosition() <= 0)
                ? "" : spInputSpecialty.getSelectedItem().toString();

        if (name.isEmpty()){ toast("Nhập tên bác sĩ"); return; }

        // chuẩn hoá phone
        String p = etPhone.getText().toString().trim().replaceAll("\\s+","");
        final String phoneNorm = p;   // <<< biến final để dùng trong inner class

        if (!phoneNorm.isEmpty()){
            String url = DOCTORS_URL + "/exists/phone?phone=" + phoneNorm;
            client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    doCreate(name, spec, phoneNorm); // dùng phoneNorm
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                    String b = resp.body()!=null?resp.body().string():"";
                    try{
                        JSONObject o = new JSONObject(b);
                        if (o.optBoolean("valid") && o.optBoolean("exists")){
                            requireActivity().runOnUiThread(() -> toast("SĐT đã tồn tại"));
                        } else {
                            doCreate(name, spec, phoneNorm);
                        }
                    }catch (Exception e){
                        doCreate(name, spec, phoneNorm);
                    }
                }
            });
        } else {
            doCreate(name, spec, "");
        }
    }


    private void doCreate(String name, String spec, String phone){
        try{
            JSONObject js = new JSONObject();
            js.put("fullName", name);
            js.put("specialty", spec);                 // có thể là ""
            if (!phone.isEmpty()) js.put("phone", phone);

            RequestBody body = RequestBody.create(js.toString(), JSON);
            Request req = new Request.Builder().url(DOCTORS_URL).post(body).build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> toast("Lỗi thêm"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body()!=null?response.body().string():"";
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()){
                            toast("Thêm thành công");
                            etName.setText(""); etPhone.setText("");
                            spInputSpecialty.setSelection(0);
                            triggerSearch();
                        } else {
                            // hiển thị message backend (nếu có)
                            try {
                                String msg = new JSONObject(b).optString("message","Thêm lỗi");
                                toast(msg);
                            } catch (Exception ex) { toast("Thêm lỗi"); }
                        }
                    });
                }
            });
        }catch (Exception ignore){}
    }

    // ==== Update/Delete như cũ ====
    private void showEditDialog(Doctor d){
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_doctor, null);
        EditText eName = dv.findViewById(R.id.etEditName);
        EditText eSpec = dv.findViewById(R.id.etEditSpecialty);
        EditText ePhone = dv.findViewById(R.id.etEditPhone);
        eName.setText(d.getFullname());
        eSpec.setText(d.getSpecialty());
        ePhone.setText(d.getPhone());

        new AlertDialog.Builder(getContext())
                .setTitle("Cập nhật bác sĩ")
                .setView(dv)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    d.setFullname(eName.getText().toString().trim());
                    d.setSpecialty(eSpec.getText().toString().trim());
                    d.setPhone(ePhone.getText().toString().trim());
                    updateDoctor(d);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void updateDoctor(Doctor d){
        try{
            JSONObject js = new JSONObject();
            js.put("id", d.getId());
            js.put("fullName", d.getFullname());
            if (d.getSpecialty()!=null) js.put("specialty", d.getSpecialty());
            if (d.getPhone()!=null) js.put("phone", d.getPhone().replaceAll("\\s+",""));

            RequestBody body = RequestBody.create(js.toString(), JSON);
            Request req = new Request.Builder().url(DOCTORS_URL + "/" + d.getId()).put(body).build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> toast("Lỗi cập nhật"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body()!=null?response.body().string():"";
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            toast("Đã cập nhật");
                            triggerSearch();
                        } else {
                            try {
                                String msg = new JSONObject(b).optString("message","Cập nhật lỗi");
                                toast(msg);
                            } catch (Exception ex) { toast("Cập nhật lỗi"); }
                        }
                    });
                }
            });
        }catch (Exception ignore){}
    }

    private void confirmDelete(int id){
        new AlertDialog.Builder(getContext())
                .setMessage("Xoá bác sĩ này?")
                .setPositiveButton("Xoá", (d, w) -> deleteDoctor(id))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteDoctor(int id){
        Request req = new Request.Builder().url(DOCTORS_URL + "/" + id).delete().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> toast("Lỗi xoá"));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.code()==409) {
                        toast("Bác sĩ còn lịch – chuyển/huỷ lịch rồi xoá");
                    } else if (response.isSuccessful()){
                        toast("Đã xoá");
                        triggerSearch();
                    } else {
                        toast("Xoá lỗi");
                    }
                });
            }
        });
    }
}
