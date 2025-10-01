package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters.AppointmentAdapter;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api.ApiClient;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Appointment;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.security.TokenStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppointmentsFragment extends Fragment {

    // ===== BASE + endpoints (không sinh "//") =====
    private static String api(String pathNoLeadingSlash) {
        String base = ApiClient.BASE_API; // ví dụ: http://192.168.1.7:5179/
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        while (pathNoLeadingSlash.startsWith("/")) {
            pathNoLeadingSlash = pathNoLeadingSlash.substring(1);
        }
        return base + "/" + pathNoLeadingSlash;
    }

    private static final String EP_APPTS    = api("api/Appointments");
    private static final String EP_DOCTORS  = api("api/Doctors");
    private static final String EP_PATIENTS = api("api/Patients");
    private static final String EP_SERVICES = api("api/Services");

    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ProgressBar progress;
    private EditText etDate;
    private AutoCompleteTextView ddDoctorFilter;
    private Button btnPickDate, btnAdd;

    private AppointmentAdapter adapter;
    private final List<Appointment> data = new ArrayList<>();

    // Dùng OkHttp chung
    private final OkHttpClient http = ApiClient.get();

    // filter state
    private String selectedDateIso = todayIsoDate(); // yyyy-MM-dd
    private Integer selectedDoctorId = null;

    // caches dropdown
    private final List<Integer> doctorIds = new ArrayList<>();
    private final List<String>  doctorNames = new ArrayList<>();
    private final List<Integer> patientIds = new ArrayList<>();
    private final List<String>  patientNames = new ArrayList<>();
    private final List<Integer> serviceIds = new ArrayList<>();
    private final List<String>  serviceNames = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_appointments, container, false);

        swipe = v.findViewById(R.id.swipeAppt);
        rv = v.findViewById(R.id.rvAppointments);
        progress = v.findViewById(R.id.progressAppt);
        etDate = v.findViewById(R.id.etApptDate);
        ddDoctorFilter = v.findViewById(R.id.ddDoctorFilter);
        btnPickDate = v.findViewById(R.id.btnPickApptDate);
        btnAdd = v.findViewById(R.id.btnAddAppointment);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentAdapter(data, new AppointmentAdapter.Listener() {
            @Override public void onChangeStatus(Appointment appt) { if (appt!=null) showChangeStatusDialog(appt); }
            @Override public void onDelete(Appointment appt) { if (appt!=null) confirmDelete(appt.getId()); }
        });
        rv.setAdapter(adapter);

        etDate.setText(selectedDateIso);
        btnPickDate.setOnClickListener(v1 -> showDatePicker());
        swipe.setOnRefreshListener(this::loadAppointments);
        btnAdd.setOnClickListener(v12 -> openCreateDialog());

        // nạp bác sĩ cho filter → rồi load danh sách
        loadDoctorsForFilter(this::loadAppointments);

        return v;
    }

    private void toast(String s){ if(isAdded()) Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show(); }
    private void runOnUi(Runnable r){ if (isAdded()) requireActivity().runOnUiThread(r); }

    private void withAuth(Request.Builder rb) {
        try {
            String token = TokenStore.get(requireContext());
            if (token != null && !token.isEmpty()) {
                rb.addHeader("Authorization", "Bearer " + token);
            }
        } catch (Throwable ignore) {}
    }

    // ------------ Filter Doctor ------------
    private interface Done { void ok(); }

    private void loadDoctorsForFilter(Done done){
        Request.Builder rb = new Request.Builder().url(EP_DOCTORS);
        withAuth(rb);
        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUi(() -> {
                    ArrayList<String> labels = new ArrayList<>();
                    labels.add("Tất cả bác sĩ");
                    ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, labels);
                    ddDoctorFilter.setAdapter(ad);
                    ddDoctorFilter.setOnItemClickListener((p, v, pos, id) -> { selectedDoctorId = null; loadAppointments(); });
                    done.ok();
                });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body()!=null?response.body().string():"";
                response.close();
                final ArrayList<String> labels = new ArrayList<>();
                final ArrayList<Integer> ids = new ArrayList<>();
                labels.add("Tất cả bác sĩ"); ids.add(-1);
                try{
                    JSONArray arr = new JSONArray(body); // endpoint này trả array
                    for (int i=0;i<arr.length();i++){
                        JSONObject o = arr.getJSONObject(i);
                        ids.add(o.getInt("id"));
                        labels.add(o.optString("fullName","(Không tên)"));
                    }
                }catch (Exception ignore){}
                runOnUi(() -> {
                    ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, labels);
                    ddDoctorFilter.setAdapter(ad);
                    ddDoctorFilter.setOnItemClickListener((parent, view, position, id) -> {
                        int picked = ids.get(position);
                        selectedDoctorId = (picked == -1) ? null : picked;
                        loadAppointments();
                    });
                    done.ok();
                });
            }
        });
    }

    // ------------ Load danh sách ------------
    private void loadAppointments(){
        progress.setVisibility(View.VISIBLE);
        swipe.setRefreshing(false);

        HttpUrl.Builder ub = HttpUrl.parse(EP_APPTS).newBuilder()
                .addQueryParameter("date", selectedDateIso);
        if (selectedDoctorId != null) ub.addQueryParameter("doctorId", String.valueOf(selectedDoctorId));

        Request.Builder rb = new Request.Builder().url(ub.build());
        withAuth(rb);

        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUi(() -> { progress.setVisibility(View.GONE); toast("Không tải được lịch"); });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body()!=null?response.body().string():"";
                response.close();
                final ArrayList<Appointment> tmp = new ArrayList<>();
                try{
                    JSONArray items;
                    if (body.trim().startsWith("[")) items = new JSONArray(body);
                    else {
                        JSONObject root = new JSONObject(body);
                        items = root.optJSONArray("items");
                        if (items == null) items = new JSONArray();
                    }
                    for (int i=0;i<items.length();i++){
                        JSONObject o = items.getJSONObject(i);
                        tmp.add(new Appointment(
                                o.optInt("id"),
                                o.optInt("patientId", 0),
                                o.optInt("doctorId", 0),
                                o.optInt("serviceId", 0),
                                o.optString("appointmentDate",""),
                                o.optString("status",""),
                                o.optString("patientName",""),
                                o.optString("doctorName",""),
                                o.optString("serviceName","")
                        ));
                    }
                }catch (Exception ignore){}
                runOnUi(() -> {
                    data.clear(); data.addAll(tmp); adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                });
            }
        });
    }

    // ------------ Dialog tạo lịch (dropdown bắt buộc) ------------
    private void openCreateDialog(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_appointment, null);
        AutoCompleteTextView ddPatient = view.findViewById(R.id.ddPatient);
        AutoCompleteTextView ddDoctor  = view.findViewById(R.id.ddDoctor);
        AutoCompleteTextView ddService = view.findViewById(R.id.ddService);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etTime = view.findViewById(R.id.etTime);

        final int[] selPatientId = {-1};
        final int[] selDoctorId  = {-1};
        final int[] selServiceId = {-1};

        etDate.setText(selectedDateIso);
        etDate.setOnClickListener(v -> showDatePick(etDate));
        etTime.setOnClickListener(v -> showTimePick(etTime));

        ddPatient.setOnClickListener(v -> ddPatient.showDropDown());
        ddDoctor.setOnClickListener(v -> ddDoctor.showDropDown());
        ddService.setOnClickListener(v -> ddService.showDropDown());

        loadPatients(ddPatient, (pos) -> selPatientId[0] = patientIds.get(pos));
        loadDoctors (ddDoctor , (pos) -> selDoctorId [0] = doctorIds .get(pos));
        loadServices(ddService, (pos) -> selServiceId[0] = serviceIds.get(pos));

        AlertDialog dlg = new AlertDialog.Builder(getContext())
                .setTitle("Đặt lịch hẹn")
                .setView(view)
                .setPositiveButton("Tạo", null)
                .setNegativeButton("Huỷ", null)
                .create();
        dlg.show();

        Button btnOk = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
        btnOk.setOnClickListener(v -> {
            if (selPatientId[0] <= 0 || selDoctorId[0] <= 0 || selServiceId[0] <= 0){
                toast("Vui lòng chọn đủ BN/BS/DV (từ danh sách).");
                return;
            }
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time)){
                toast("Chọn ngày/giờ."); return;
            }
            String iso = date + "T" + toTime24h(time) + ":00";
            doCreateAppointment(selPatientId[0], selDoctorId[0], selServiceId[0], iso);
            dlg.dismiss();
        });
    }

    // ------------ Gọi API tạo ------------
    private void doCreateAppointment(int patientId, int doctorId, int serviceId, String iso){
        try{
            JSONObject js = new JSONObject();
            js.put("patientId", patientId);
            js.put("doctorId", doctorId);
            js.put("serviceId", serviceId);
            js.put("appointmentDate", iso);
            js.put("status", "Scheduled");

            RequestBody body = RequestBody.create(js.toString(), ApiClient.JSON);
            Request.Builder rb = new Request.Builder().url(EP_APPTS).post(body);
            withAuth(rb);

            http.newCall(rb.build()).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUi(() -> toast("Lỗi tạo lịch"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body()!=null?response.body().string():"";
                    response.close();
                    runOnUi(() -> {
                        if (response.isSuccessful()) { toast("Đã tạo lịch"); loadAppointments(); }
                        else {
                            String msg = "Tạo lịch lỗi";
                            try { msg = new JSONObject(b).optString("message", msg); } catch (Exception ignore){}
                            toast(msg);
                        }
                    });
                }
            });
        }catch (Exception ignore){}
    }

    // ------------ Đổi trạng thái ------------
    private void showChangeStatusDialog(Appointment appt){
        final String[] statuses = {"Scheduled","CheckedIn","InExam","Completed","Canceled"};
        new AlertDialog.Builder(getContext())
                .setTitle("Đổi trạng thái")
                .setItems(statuses, (dialog, which) -> {
                    String value = statuses[which];

                    HttpUrl url = HttpUrl.parse(EP_APPTS + "/" + appt.getId() + "/status")
                            .newBuilder()
                            .addQueryParameter("value", value)
                            .build();

                    Request.Builder rb = new Request.Builder()
                            .url(url)
                            .put(RequestBody.create(new byte[0], null));
                    withAuth(rb);

                    http.newCall(rb.build()).enqueue(new Callback() {
                        @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            runOnUi(() -> toast("Lỗi cập nhật"));
                        }
                        @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                            response.close();
                            runOnUi(() -> {
                                if (response.isSuccessful()) { toast("Đã cập nhật: " + value); loadAppointments(); }
                                else toast("Cập nhật lỗi");
                            });
                        }
                    });
                })
                .show();
    }

    // ------------ Huỷ lịch ------------
    private void confirmDelete(int id){
        new AlertDialog.Builder(getContext())
                .setMessage("Huỷ lịch này?")
                .setPositiveButton("Huỷ", (d,w)->{
                    Request.Builder rb = new Request.Builder().url(EP_APPTS + "/" + id).delete();
                    withAuth(rb);
                    http.newCall(rb.build()).enqueue(new Callback() {
                        @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            runOnUi(() -> toast("Lỗi huỷ"));
                        }
                        @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                            response.close();
                            runOnUi(() -> {
                                if (response.isSuccessful()){ toast("Đã huỷ"); loadAppointments(); }
                                else toast("Huỷ lỗi");
                            });
                        }
                    });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // ------------ Load dropdowns (trả vị trí chọn) ------------
    private interface OnPickPos { void pick(int position); }

    private void loadPatients(AutoCompleteTextView dd, OnPickPos cb){
        Request.Builder rb = new Request.Builder().url(EP_PATIENTS);
        withAuth(rb);
        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body()!=null?response.body().string():"";
                response.close();
                patientIds.clear(); patientNames.clear();
                try{
                    JSONArray arr = new JSONArray(body); // endpoint này trả array
                    for (int i=0;i<arr.length();i++){
                        JSONObject o = arr.getJSONObject(i);
                        patientIds.add(o.getInt("id"));
                        patientNames.add(o.optString("fullName","(Không tên)"));
                    }
                    runOnUi(() -> {
                        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_dropdown_item_1line, patientNames);
                        dd.setAdapter(ad);
                        dd.setOnItemClickListener((parent, view, pos, id) -> {
                            dd.setText(patientNames.get(pos), false);
                            cb.pick(pos);
                        });
                    });
                }catch (Exception ignore){}
            }
        });
    }
    private void loadDoctors(AutoCompleteTextView dd, OnPickPos cb){
        Request.Builder rb = new Request.Builder().url(EP_DOCTORS);
        withAuth(rb);
        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body()!=null?response.body().string():"";
                response.close();
                doctorIds.clear(); doctorNames.clear();
                try{
                    JSONArray arr = new JSONArray(body);
                    for (int i=0;i<arr.length();i++){
                        JSONObject o = arr.getJSONObject(i);
                        doctorIds.add(o.getInt("id"));
                        doctorNames.add(o.optString("fullName","(Không tên)"));
                    }
                    runOnUi(() -> {
                        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_dropdown_item_1line, doctorNames);
                        dd.setAdapter(ad);
                        dd.setOnItemClickListener((parent, view, pos, id) -> {
                            dd.setText(doctorNames.get(pos), false);
                            cb.pick(pos);
                        });
                    });
                }catch (Exception ignore){}
            }
        });
    }
    private void loadServices(AutoCompleteTextView dd, OnPickPos cb){
        Request.Builder rb = new Request.Builder().url(EP_SERVICES);
        withAuth(rb);
        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body()!=null?response.body().string():"";
                response.close();
                serviceIds.clear(); serviceNames.clear();
                try{
                    JSONArray arr = new JSONArray(body);
                    for (int i=0;i<arr.length();i++){
                        JSONObject o = arr.getJSONObject(i);
                        serviceIds.add(o.getInt("id"));
                        serviceNames.add(o.optString("serviceName","(Không tên)"));
                    }
                    runOnUi(() -> {
                        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_dropdown_item_1line, serviceNames);
                        dd.setAdapter(ad);
                        dd.setOnItemClickListener((parent, view, pos, id) -> {
                            dd.setText(serviceNames.get(pos), false);
                            cb.pick(pos);
                        });
                    });
                }catch (Exception ignore){}
            }
        });
    }

    // ------------ Helpers ------------
    private void showDatePicker(){
        final Calendar c = Calendar.getInstance();
        String[] d = selectedDateIso.split("-");
        if (d.length==3){
            try{
                c.set(Calendar.YEAR, Integer.parseInt(d[0]));
                c.set(Calendar.MONTH, Integer.parseInt(d[1])-1);
                c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(d[2]));
            }catch (Exception ignore){}
        }
        DatePickerDialog dp = new DatePickerDialog(getContext(), (DatePicker view, int y, int m, int day)->{
            selectedDateIso = String.format("%04d-%02d-%02d", y, m+1, day);
            etDate.setText(selectedDateIso);
            loadAppointments();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void showDatePick(EditText et){
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(getContext(), (view, y, m, d)->{
            et.setText(String.format("%04d-%02d-%02d", y, m+1, d));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void showTimePick(EditText et){
        final Calendar c = Calendar.getInstance();
        TimePickerDialog tp = new TimePickerDialog(getContext(), (TimePicker view, int h, int min)->{
            et.setText(String.format("%02d:%02d", h, min));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        tp.show();
    }

    private static String todayIsoDate(){
        Calendar c = Calendar.getInstance();
        return String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH));
    }

    private String toTime24h(String timeHHmm){
        if (timeHHmm == null) return "09:00";
        String[] p = timeHHmm.split(":");
        try{
            int h = Integer.parseInt(p[0].trim());
            int m = Integer.parseInt(p[1].trim());
            return String.format("%02d:%02d", h, m);
        }catch (Exception e){ return "09:00"; }
    }
}
