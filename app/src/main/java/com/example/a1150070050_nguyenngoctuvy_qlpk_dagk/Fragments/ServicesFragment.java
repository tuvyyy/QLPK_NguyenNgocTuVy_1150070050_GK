package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters.ServiceAdapter;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServicesFragment extends Fragment {

    // DÙNG CHUẨN HARD-CODE GIỐNG DOCTOR
    private static final String SERVICES_URL = "http://192.168.1.9:5179/api/Services";

    private EditText etSearch, etName, etPrice;
    private Button btnAdd;
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ServiceAdapter adapter;
    private final List<Service> data = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    // paging
    private int page = 1;
    private final int pageSize = 10;
    private boolean hasNext = true;
    private boolean isLoading = false;
    private String currentQuery = "";

    private LinearLayoutManager layoutManager;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_services, container, false);

        etSearch = v.findViewById(R.id.etSearchService);
        etName   = v.findViewById(R.id.etServiceName);
        etPrice  = v.findViewById(R.id.etServicePrice);
        btnAdd   = v.findViewById(R.id.btnAddService);
        swipe    = v.findViewById(R.id.swipeRefresh);
        rv       = v.findViewById(R.id.rvServices);

        layoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(layoutManager);

        adapter = new ServiceAdapter(data, new ServiceAdapter.Listener() {
            @Override public void onEdit(Service s) { showEditDialog(s); }
            @Override public void onDelete(Service s) { confirmDelete(s.getId()); }
        });
        rv.setAdapter(adapter);

        // Endless scroll
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) return;
                int total = layoutManager.getItemCount();
                int last  = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && hasNext && last >= total - 3) {
                    loadNextPage();
                }
            }
        });

        // Pull to refresh
        swipe.setOnRefreshListener(this::resetAndLoad);

        // Search debounce
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(ServicesFragment.this::resetAndLoad, 350);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAdd.setOnClickListener(v1 -> createService());

        resetAndLoad(); // first load
        return v;
    }

    private void toast(String msg){ Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show(); }

    private void resetAndLoad(){
        page = 1; hasNext = true; isLoading = false;
        data.clear(); adapter.notifyDataSetChanged();
        loadNextPage();
    }

    private void setLoading(boolean loading){
        isLoading = loading;
        adapter.setShowLoading(loading);
        swipe.setRefreshing(false);
    }

    // ===== PAGE LOAD =====
    private void loadNextPage(){
        if (!hasNext || isLoading) return;
        setLoading(true);

        String url = SERVICES_URL + "?page=" + page + "&pageSize=" + pageSize + "&sort=name_asc";
        if (!currentQuery.isEmpty()) url += "&q=" + currentQuery;

        Request req = new Request.Builder().url(url).build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> { setLoading(false); toast("Lỗi mạng"); });
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                String body = resp.body()!=null?resp.body().string():"";
                if (!resp.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> setLoading(false));
                    return;
                }
                try{
                    JSONObject o = new JSONObject(body);
                    JSONArray arr = o.getJSONArray("items");
                    List<Service> list = new ArrayList<>();
                    for (int i=0;i<arr.length();i++){
                        JSONObject it = arr.getJSONObject(i);
                        list.add(new Service(
                                it.getInt("id"),
                                it.getString("serviceName"),
                                it.getDouble("price")
                        ));
                    }
                    boolean next = o.optBoolean("hasNext", false);

                    requireActivity().runOnUiThread(() -> {
                        int start = data.size();
                        data.addAll(list);
                        adapter.notifyItemRangeInserted(start, list.size());

                        page += 1;
                        hasNext = next;
                        setLoading(false);
                    });
                }catch (Exception ex){
                    requireActivity().runOnUiThread(() -> setLoading(false));
                }
            }
        });
    }

    // ===== CRUD =====
    private void createService(){
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        if (name.isEmpty()){ toast("Nhập tên dịch vụ"); return; }

        double tmpPrice = 0;
        if (!priceStr.isEmpty()){
            try { tmpPrice = Double.parseDouble(priceStr); }
            catch (Exception e){ toast("Giá không hợp lệ"); return; }
        }
        final String nameF = name;
        final double priceF = tmpPrice;

        String checkUrl = SERVICES_URL + "/exists/name?name=" + nameF;
        client.newCall(new Request.Builder().url(checkUrl).build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { doCreate(nameF, priceF); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                String b = resp.body()!=null?resp.body().string():"";
                try{
                    JSONObject o = new JSONObject(b);
                    if (o.optBoolean("exists")){
                        requireActivity().runOnUiThread(() -> toast("Tên dịch vụ đã tồn tại"));
                    } else doCreate(nameF, priceF);
                }catch (Exception e){ doCreate(nameF, priceF); }
            }
        });
    }

    private void doCreate(String name, double price){
        try{
            JSONObject js = new JSONObject();
            js.put("serviceName", name);
            js.put("price", price);

            RequestBody body = RequestBody.create(js.toString(), JSON);
            Request req = new Request.Builder().url(SERVICES_URL).post(body).build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> toast("Lỗi thêm"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body()!=null?response.body().string():"";
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()){
                            toast("Thêm thành công");
                            etName.setText(""); etPrice.setText("");
                            resetAndLoad();
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

    private void showEditDialog(Service s){
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_service, null);
        EditText eName = dv.findViewById(R.id.etEditServiceName);
        EditText ePrice = dv.findViewById(R.id.etEditServicePrice);
        eName.setText(s.getServiceName());
        ePrice.setText(String.valueOf((long)s.getPrice()));

        new AlertDialog.Builder(getContext())
                .setTitle("Cập nhật dịch vụ")
                .setView(dv)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String n = eName.getText().toString().trim();
                    String pStr = ePrice.getText().toString().trim();
                    if (n.isEmpty()){ toast("Nhập tên dịch vụ"); return; }
                    double pVal = 0;
                    if (!pStr.isEmpty()){
                        try { pVal = Double.parseDouble(pStr); }
                        catch (Exception ex){ toast("Giá không hợp lệ"); return; }
                    }
                    s.setServiceName(n);
                    s.setPrice(pVal);
                    updateService(s);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void updateService(Service s){
        try{
            JSONObject js = new JSONObject();
            js.put("id", s.getId());
            js.put("serviceName", s.getServiceName());
            js.put("price", s.getPrice());

            RequestBody body = RequestBody.create(js.toString(), JSON);
            Request req = new Request.Builder().url(SERVICES_URL + "/" + s.getId()).put(body).build();
            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> toast("Lỗi cập nhật"));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String b = response.body()!=null?response.body().string():"";
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()){
                            toast("Đã cập nhật");
                            resetAndLoad();
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
                .setMessage("Xoá dịch vụ này?")
                .setPositiveButton("Xoá", (d, w) -> deleteService(id))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteService(int id){
        Request req = new Request.Builder().url(SERVICES_URL + "/" + id).delete().build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> toast("Lỗi xoá"));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                requireActivity().runOnUiThread(() -> {
                    if (response.code() == 409) {
                        toast("Dịch vụ đang được sử dụng trong lịch hẹn");
                    } else if (response.isSuccessful()){
                        toast("Đã xoá");
                        resetAndLoad();
                    } else {
                        toast("Xoá lỗi");
                    }
                });
            }
        });
    }
}
