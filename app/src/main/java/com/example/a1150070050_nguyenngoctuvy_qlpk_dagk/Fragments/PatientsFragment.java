package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters.PatientAdapter;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Patient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PatientsFragment extends Fragment {

    // ĐỔI IP/PORT cho máy bạn
    private static final String PATIENTS_URL = "http://192.168.1.9:5179/api/Patients";

    private EditText etSearch, etName, etDob, etPhone, etAddress;
    private Spinner spGender;
    private Button btnAdd;
    private RecyclerView rv;
    private PatientAdapter adapter;
    private final List<Patient> data = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private String currentQuery = "";

    // Hiển thị cho người dùng (VN)
    private static final String[] GENDER_DISPLAY = {"(Giới tính)", "Nam", "Nữ", "Không xác định"};
    // Code gửi backend (giữ M/F/O để tương thích)
    private static final String[] GENDER_CODE = {"", "M", "F", "O"};

    private String codeFromDisplay(String display) {
        switch (display) {
            case "Nam": return "M";
            case "Nữ": return "F";
            case "Không xác định": return "O";
            default: return "";
        }
    }
    private String displayFromCode(String code) {
        if (code == null) return "(Giới tính)";
        switch (code.toUpperCase()) {
            case "M": return "Nam";
            case "F": return "Nữ";
            case "O": return "Không xác định";
            default:  return "(Giới tính)";
        }
    }
    private int displayIndex(String display) {
        for (int i=0;i<GENDER_DISPLAY.length;i++) if (GENDER_DISPLAY[i].equals(display)) return i;
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_patients, container, false);

        etSearch = v.findViewById(R.id.etSearch);
        etName = v.findViewById(R.id.etName);
        etDob = v.findViewById(R.id.etDob);
        etPhone = v.findViewById(R.id.etPhone);
        etAddress = v.findViewById(R.id.etAddress);
        spGender = v.findViewById(R.id.spGender);
        btnAdd = v.findViewById(R.id.btnAdd);
        rv = v.findViewById(R.id.rv);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PatientAdapter(data, new PatientAdapter.Listener() {
            @Override public void onEdit(Patient p) { showEditDialog(p); }
            @Override public void onDelete(Patient p) { confirmDelete(p.getId()); }
        });
        rv.setAdapter(adapter);

        // Spinner giới tính (VN)
        spGender.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, GENDER_DISPLAY));

        etDob.setOnClickListener(v1 -> showDatePicker(etDob));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                debounce();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAdd.setOnClickListener(v12 -> createPatient());
        load();
        return v;
    }

    private void toast(String m){ Toast.makeText(getContext(), m, Toast.LENGTH_SHORT).show(); }
    private void debounce(){ handler.removeCallbacksAndMessages(null); handler.postDelayed(this::load, 350); }

    private void showDatePicker(EditText target){
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(getContext(), (DatePicker view, int y, int m, int d) -> {
            String mm = (m+1<10?("0"+(m+1)):(m+1+""));
            String dd = (d<10?("0"+d):(d+""));
            target.setText(y+"-"+mm+"-"+dd);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void load(){
        String url = PATIENTS_URL + "?page=1&pageSize=200&sort=name_asc";
        if (!currentQuery.trim().isEmpty()) url += "&q=" + currentQuery.trim();

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
                    List<Patient> tmp = new ArrayList<>();
                    for (int i=0;i<arr.length();i++){
                        JSONObject it = arr.getJSONObject(i);
                        tmp.add(new Patient(
                                it.getInt("id"),
                                it.optString("fullName",""),
                                it.isNull("dob")?null:it.getString("dob"),
                                it.optString("gender",null),
                                it.isNull("phone")?null:it.getString("phone"),
                                it.optString("address",null)
                        ));
                    }
                    requireActivity().runOnUiThread(() -> { data.clear(); data.addAll(tmp); adapter.notifyDataSetChanged(); });
                }catch (Exception ignore){}
            }
        });
    }

    private void createPatient(){
        String name = etName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String addr = etAddress.getText().toString().trim();

        if (name.isEmpty()){ toast("Nhập họ tên"); return; }

        String genderDisplay = (String) spGender.getSelectedItem();
        String gender = codeFromDisplay(genderDisplay); // map VN -> M/F/O

        // chuẩn hoá & final cho inner class
        String p = etPhone.getText().toString().trim().replaceAll("\\s+","");
        final String phoneNorm = p;

        if (!phoneNorm.isEmpty()){
            String url = PATIENTS_URL + "/exists/phone?phone=" + phoneNorm;
            client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    doCreate(name, dob, gender, phoneNorm, addr);
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                    String b = resp.body()!=null?resp.body().string():"";
                    try{
                        JSONObject o = new JSONObject(b);
                        if (o.optBoolean("valid") && o.optBoolean("exists")){
                            requireActivity().runOnUiThread(() -> toast("SĐT đã tồn tại"));
                        } else doCreate(name, dob, gender, phoneNorm, addr);
                    }catch (Exception e){ doCreate(name, dob, gender, phoneNorm, addr); }
                }
            });
        } else {
            doCreate(name, dob, gender, "", addr);
        }
    }

    private void doCreate(String name, String dob, String gender, String phone, String addr){
        try{
            JSONObject js = new JSONObject();
            js.put("fullName", name);
            if (!dob.isEmpty()) js.put("dob", dob);
            if (!gender.isEmpty()) js.put("gender", gender);          // "M"/"F"/"O"
            if (!phone.isEmpty()) js.put("phone", phone);
            if (!addr.isEmpty()) js.put("address", addr);

            RequestBody body = RequestBody.create(js.toString(), JSON);
            Request req = new Request.Builder().url(PATIENTS_URL).post(body).build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> toast("Lỗi thêm"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body()!=null?response.body().string():"";
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()){
                            toast("Thêm thành công");
                            etName.setText(""); etDob.setText(""); etPhone.setText(""); etAddress.setText("");
                            spGender.setSelection(0);
                            load();
                        } else {
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

    private void showEditDialog(Patient p){
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_patient, null);
        EditText eName = dv.findViewById(R.id.etEditName);
        EditText eDob  = dv.findViewById(R.id.etEditDob);
        Spinner spEditGender = dv.findViewById(R.id.spEditGender);
        EditText ePhone = dv.findViewById(R.id.etEditPhone);
        EditText eAddr  = dv.findViewById(R.id.etEditAddress);

        eName.setText(p.getFullName());
        eDob.setText(p.getDob());

        ArrayAdapter<String> genderEditAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, GENDER_DISPLAY);
        spEditGender.setAdapter(genderEditAdapter);
        String display = displayFromCode(p.getGender());
        spEditGender.setSelection(displayIndex(display));

        ePhone.setText(p.getPhone());
        eAddr.setText(p.getAddress());
        eDob.setOnClickListener(v -> showDatePicker(eDob));

        new AlertDialog.Builder(getContext())
                .setTitle("Cập nhật bệnh nhân")
                .setView(dv)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    p.setFullName(eName.getText().toString().trim());
                    p.setDob(eDob.getText().toString().trim());
                    String genderDispSel = (String) spEditGender.getSelectedItem();
                    p.setGender(codeFromDisplay(genderDispSel));  // M/F/O
                    p.setPhone(ePhone.getText().toString().trim());
                    p.setAddress(eAddr.getText().toString().trim());
                    updatePatient(p);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void updatePatient(Patient p){
        try{
            JSONObject js = new JSONObject();
            js.put("id", p.getId());
            js.put("fullName", p.getFullName());
            if (p.getDob()!=null && !p.getDob().isEmpty()) js.put("dob", p.getDob());
            if (p.getGender()!=null && !p.getGender().isEmpty()) js.put("gender", p.getGender());
            if (p.getPhone()!=null && !p.getPhone().isEmpty()) js.put("phone", p.getPhone().replaceAll("\\s+",""));
            if (p.getAddress()!=null && !p.getAddress().isEmpty()) js.put("address", p.getAddress());

            RequestBody body = RequestBody.create(js.toString(), JSON);
            Request req = new Request.Builder().url(PATIENTS_URL + "/" + p.getId()).put(body).build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> toast("Lỗi cập nhật"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body()!=null?response.body().string():"";
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()){
                            toast("Đã cập nhật");
                            load();
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
                .setMessage("Xoá bệnh nhân này?")
                .setPositiveButton("Xoá", (d, w) -> deletePatient(id))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deletePatient(int id){
        Request req = new Request.Builder().url(PATIENTS_URL + "/" + id).delete().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> toast("Lỗi xoá"));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.code()==409) {
                        toast("Bệnh nhân còn lịch – huỷ/xoá lịch trước");
                    } else if (response.isSuccessful()){
                        toast("Đã xoá");
                        load();
                    } else {
                        toast("Xoá lỗi");
                    }
                });
            }
        });
    }
}
