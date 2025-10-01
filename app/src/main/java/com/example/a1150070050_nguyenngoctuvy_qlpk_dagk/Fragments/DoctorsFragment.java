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
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api.ApiClient;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Doctor;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.security.TokenStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DoctorsFragment extends Fragment {

    // --------- BASE + endpoints (an toàn, không sinh "//") ----------
    private static String api(String pathNoLeadingSlash) {
        String base = ApiClient.BASE_API; // vd: http://192.168.1.7:5179/
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        while (pathNoLeadingSlash.startsWith("/")) {
            pathNoLeadingSlash = pathNoLeadingSlash.substring(1);
        }
        return base + "/" + pathNoLeadingSlash;
    }

    private static final String EP_DOCTORS       = api("api/Doctors");
    private static final String EP_SPECIALTIES   = api("api/Doctors/specialties");
    private static final String EP_EXISTS_PHONE  = api("api/Doctors/exists/phone");

    private EditText etSearch, etName, etPhone;
    private Spinner spFilterSpecialty, spInputSpecialty;
    private Button btnSearch, btnAdd;
    private RecyclerView rv;
    private DoctorAdapter adapter;

    private final List<Doctor> data = new ArrayList<>();
    private final List<String> specialties = new ArrayList<>();

    // Dùng client chung (đã có AllowedHostInterceptor)
    private final OkHttpClient http = ApiClient.get();

    private String currentQuery = "";
    private String currentFilterSpecialty = ""; // "" = tất cả

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctors, container, false);

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

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        ArrayAdapter<String> inputAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());

        spFilterSpecialty.setAdapter(filterAdapter);
        spInputSpecialty.setAdapter(inputAdapter);

        loadSpecialties(list -> {
            List<String> filterList = new ArrayList<>();
            filterList.add("Tất cả");
            filterList.addAll(list);
            filterAdapter.clear();
            filterAdapter.addAll(filterList);
            filterAdapter.notifyDataSetChanged();

            List<String> inputList = new ArrayList<>();
            inputList.add("(Chọn chuyên khoa)");
            inputList.addAll(list);
            inputAdapter.clear();
            inputAdapter.addAll(inputList);
            inputAdapter.notifyDataSetChanged();

            spFilterSpecialty.setSelection(0);
            spInputSpecialty.setSelection(0);

            triggerSearch();
        });

        btnSearch.setOnClickListener(v1 -> {
            currentQuery = text(etSearch);
            currentFilterSpecialty = (spFilterSpecialty.getSelectedItemPosition() <= 0)
                    ? "" : String.valueOf(spFilterSpecialty.getSelectedItem());
            triggerSearch();
        });

        btnAdd.setOnClickListener(v12 -> createDoctor());

        return v;
    }

    private void toast(String m) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), m, Toast.LENGTH_SHORT).show();
    }

    private void withAuth(Request.Builder rb) {
        try {
            String token = TokenStore.get(requireContext());
            if (token != null && !token.isEmpty()) {
                rb.addHeader("Authorization", "Bearer " + token);
            }
        } catch (Throwable ignore) { /* nếu chưa có TokenStore.get cũng không sao */ }
    }

    // ==== API: specialties ====
    private interface OnSpecialtyLoaded { void done(List<String> list); }
    private void loadSpecialties(OnSpecialtyLoaded cb) {
        Request.Builder rb = new Request.Builder().url(EP_SPECIALTIES);
        withAuth(rb);
        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    toast("Không tải được danh sách chuyên khoa");
                    cb.done(Collections.emptyList());
                });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response res) throws IOException {
                String s = res.body() != null ? res.body().string() : "";
                res.close();
                List<String> arr = new ArrayList<>();
                if (res.isSuccessful()) {
                    try {
                        JSONArray j = new JSONArray(s);
                        for (int i = 0; i < j.length(); i++) arr.add(j.getString(i));
                    } catch (Exception ignored) {}
                }
                specialties.clear();
                specialties.addAll(arr);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> cb.done(arr));
            }
        });
    }

    // ==== API: list/search + filter client-side ====
    private void triggerSearch() {
        HttpUrl.Builder ub = HttpUrl.parse(EP_DOCTORS).newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("pageSize", "500")
                .addQueryParameter("sort", "name_asc");
        if (!currentQuery.isEmpty()) ub.addQueryParameter("q", currentQuery);

        Request.Builder rb = new Request.Builder().url(ub.build());
        withAuth(rb);

        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> toast("Lỗi mạng"));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                String body = resp.body() != null ? resp.body().string() : "";
                resp.close();
                if (!resp.isSuccessful()) return;
                try {
                    JSONObject o = new JSONObject(body);
                    JSONArray arr = o.getJSONArray("items");
                    List<Doctor> all = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject it = arr.getJSONObject(i);
                        all.add(new Doctor(
                                it.getInt("id"),
                                it.optString("fullName", ""),
                                it.optString("specialty", ""),
                                it.isNull("phone") ? null : it.getString("phone")
                        ));
                    }
                    List<Doctor> filtered = new ArrayList<>();
                    for (Doctor d : all) {
                        if (currentFilterSpecialty.isEmpty()
                                || currentFilterSpecialty.equalsIgnoreCase(d.getSpecialty())) {
                            filtered.add(d);
                        }
                    }
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        data.clear();
                        data.addAll(filtered);
                        adapter.notifyDataSetChanged();
                    });
                } catch (Exception ignore) {}
            }
        });
    }

    // ==== Create ====
    private void createDoctor() {
        String name = text(etName);
        String spec = (spInputSpecialty.getSelectedItemPosition() <= 0)
                ? "" : String.valueOf(spInputSpecialty.getSelectedItem());
        if (name.isEmpty()) { toast("Nhập tên bác sĩ"); return; }

        String phoneNorm = text(etPhone).replaceAll("\\s+", "");

        if (!phoneNorm.isEmpty()) {
            HttpUrl url = HttpUrl.parse(EP_EXISTS_PHONE).newBuilder()
                    .addQueryParameter("phone", phoneNorm)
                    .build();
            Request.Builder rb = new Request.Builder().url(url);
            withAuth(rb);
            http.newCall(rb.build()).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    doCreate(name, spec, phoneNorm);
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                    String b = resp.body() != null ? resp.body().string() : "";
                    resp.close();
                    try {
                        JSONObject o = new JSONObject(b);
                        if (o.optBoolean("valid") && o.optBoolean("exists")) {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> toast("SĐT đã tồn tại"));
                        } else {
                            doCreate(name, spec, phoneNorm);
                        }
                    } catch (Exception e) {
                        doCreate(name, spec, phoneNorm);
                    }
                }
            });
        } else {
            doCreate(name, spec, "");
        }
    }

    private void doCreate(String name, String spec, String phone) {
        try {
            JSONObject js = new JSONObject();
            js.put("fullName", name);
            js.put("specialty", spec);
            if (!phone.isEmpty()) js.put("phone", phone);

            RequestBody body = RequestBody.create(js.toString(), ApiClient.JSON);
            Request.Builder rb = new Request.Builder().url(EP_DOCTORS).post(body);
            withAuth(rb);
            http.newCall(rb.build()).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> toast("Lỗi thêm"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body() != null ? response.body().string() : "";
                    response.close();
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            toast("Thêm thành công");
                            etName.setText(""); etPhone.setText("");
                            spInputSpecialty.setSelection(0);
                            triggerSearch();
                        } else {
                            try {
                                String msg = new JSONObject(b).optString("message", "Thêm lỗi");
                                toast(msg);
                            } catch (Exception ex) { toast("Thêm lỗi"); }
                        }
                    });
                }
            });
        } catch (Exception ignore) {}
    }

    // ==== Update/Delete ====
    private void showEditDialog(Doctor d) {
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_doctor, null);
        EditText eName  = dv.findViewById(R.id.etEditName);
        EditText eSpec  = dv.findViewById(R.id.etEditSpecialty);
        EditText ePhone = dv.findViewById(R.id.etEditPhone);
        eName.setText(d.getFullname());
        eSpec.setText(d.getSpecialty());
        ePhone.setText(d.getPhone());

        new AlertDialog.Builder(getContext())
                .setTitle("Cập nhật bác sĩ")
                .setView(dv)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    d.setFullname(text(eName));
                    d.setSpecialty(text(eSpec));
                    d.setPhone(text(ePhone));
                    updateDoctor(d);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void updateDoctor(Doctor d) {
        try {
            JSONObject js = new JSONObject();
            js.put("id", d.getId());
            js.put("fullName", d.getFullname());
            if (d.getSpecialty() != null) js.put("specialty", d.getSpecialty());
            if (d.getPhone() != null) js.put("phone", d.getPhone().replaceAll("\\s+", ""));

            RequestBody body = RequestBody.create(js.toString(), ApiClient.JSON);
            Request.Builder rb = new Request.Builder()
                    .url(EP_DOCTORS + "/" + d.getId())
                    .put(body);
            withAuth(rb);
            http.newCall(rb.build()).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> toast("Lỗi cập nhật"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body() != null ? response.body().string() : "";
                    response.close();
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            toast("Đã cập nhật");
                            triggerSearch();
                        } else {
                            try {
                                String msg = new JSONObject(b).optString("message", "Cập nhật lỗi");
                                toast(msg);
                            } catch (Exception ex) { toast("Cập nhật lỗi"); }
                        }
                    });
                }
            });
        } catch (Exception ignore) {}
    }

    private void confirmDelete(int id) {
        new AlertDialog.Builder(getContext())
                .setMessage("Xoá bác sĩ này?")
                .setPositiveButton("Xoá", (d, w) -> deleteDoctor(id))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteDoctor(int id) {
        Request.Builder rb = new Request.Builder().url(EP_DOCTORS + "/" + id).delete();
        withAuth(rb);
        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> toast("Lỗi xoá"));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                response.close();
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (response.code() == 409) {
                        toast("Bác sĩ còn lịch – chuyển/huỷ lịch rồi xoá");
                    } else if (response.isSuccessful()) {
                        toast("Đã xoá");
                        triggerSearch();
                    } else {
                        toast("Xoá lỗi");
                    }
                });
            }
        });
    }

    // -------- utils ----------
    private static String text(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
