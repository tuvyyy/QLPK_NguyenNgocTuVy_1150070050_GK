package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters.UserAdapter;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.api.ApiClient;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.User;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.security.TokenStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rv;
    private Button btnAdd;

    private final OkHttpClient http = ApiClient.get();
    private final List<User> all = new ArrayList<>();
    private final List<User> shown = new ArrayList<>();
    private UserAdapter adapter;

    private static String EP(String path){
        String base = ApiClient.BASE_API;
        if (base.endsWith("/")) base = base.substring(0, base.length()-1);
        if (path.startsWith("/")) path = path.substring(1);
        return base + "/" + path;
    }
    private static final String USERS_URL = EP("api/Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        etSearch = findViewById(R.id.etSearchUser);
        btnAdd   = findViewById(R.id.btnAddUser);
        rv       = findViewById(R.id.rvUsers);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(shown, new UserAdapter.Listener() {
            @Override public void onEdit(User u) { showEditDialog(u); }
            @Override public void onDelete(User u) { confirmDelete(u); }
        });
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showCreateDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadUsers();
    }

    private void withAuth(Request.Builder rb){
        String token = TokenStore.get(this);
        if (token != null && !token.isEmpty()) rb.addHeader("Authorization","Bearer "+token);
    }

    private void toast(String m){ Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }

    // ------- Load & filter -------
    private void loadUsers(){
        Request.Builder rb = new Request.Builder().url(USERS_URL);
        withAuth(rb);
        http.newCall(rb.build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> toast("Không tải được danh sách"));
            }
            @Override public void onResponse(Call call, Response res) throws IOException {
                String body = res.body()!=null?res.body().string():"";
                res.close();
                List<User> tmp = new ArrayList<>();
                try{
                    JSONArray arr = new JSONArray(body);
                    for (int i=0;i<arr.length();i++){
                        JSONObject o = arr.getJSONObject(i);
                        tmp.add(new User(
                                o.getInt("id"),
                                o.optString("username",""),
                                o.optString("passwordHash",""),
                                o.optString("email", null),
                                o.optString("role", "user")
                        ));
                    }
                }catch (Exception ignore){}
                runOnUiThread(() -> {
                    all.clear(); all.addAll(tmp);
                    filter(etSearch.getText().toString());
                });
            }
        });
    }

    private void filter(String q){
        String qq = q == null ? "" : q.trim().toLowerCase();
        shown.clear();
        for (User u : all){
            String a = (u.getUsername()==null?"":u.getUsername()).toLowerCase();
            String b = (u.getEmail()==null?"":u.getEmail()).toLowerCase();
            if (qq.isEmpty() || a.contains(qq) || b.contains(qq)) shown.add(u);
        }
        adapter.notifyDataSetChanged();
    }

    // ------- Create -------
    private void showCreateDialog(){
        final var v = getLayoutInflater().inflate(R.layout.dialog_user_edit, null);
        final EditText etUName = v.findViewById(R.id.etUName);
        final EditText etUEmail = v.findViewById(R.id.etUEmail);
        final EditText etURole = v.findViewById(R.id.etURole);
        final EditText etUPass = v.findViewById(R.id.etUPassword);

        etURole.setText("user");

        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Thêm tài khoản")
                .setView(v)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Huỷ", null)
                .create();
        dlg.show();

        Button ok = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(btn -> {
            String name = etUName.getText().toString().trim();
            String email = etUEmail.getText().toString().trim();
            String role  = etURole.getText().toString().trim();
            String pass  = etUPass.getText().toString().trim();

            if (name.isEmpty()){ etUName.setError("Nhập username"); return; }
            if (pass.isEmpty()){ etUPass.setError("Nhập mật khẩu"); return; }
            if (role.isEmpty()) role = "user";

            try{
                JSONObject js = new JSONObject();
                js.put("username", name);
                js.put("passwordHash", pass); // BE đang dùng field này
                js.put("email", email);
                js.put("role", role);

                RequestBody body = RequestBody.create(js.toString(), ApiClient.JSON);
                Request.Builder rb = new Request.Builder().url(USERS_URL).post(body);
                withAuth(rb);

                http.newCall(rb.build()).enqueue(new Callback() {
                    @Override public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> toast("Thêm thất bại"));
                    }
                    @Override public void onResponse(Call call, Response res) throws IOException {
                        res.close();
                        runOnUiThread(() -> {
                            if (res.isSuccessful()){ toast("Đã thêm"); loadUsers(); dlg.dismiss(); }
                            else toast("Thêm lỗi: " + res.code());
                        });
                    }
                });
            }catch (Exception ignore){}
        });
    }

    // ------- Edit -------
    private void showEditDialog(User u){
        final var v = getLayoutInflater().inflate(R.layout.dialog_user_edit, null);
        final EditText etUName = v.findViewById(R.id.etUName);
        final EditText etUEmail = v.findViewById(R.id.etUEmail);
        final EditText etURole = v.findViewById(R.id.etURole);
        final EditText etUPass = v.findViewById(R.id.etUPassword);

        etUName.setText(u.getUsername());
        etUEmail.setText(u.getEmail());
        etURole.setText(u.getRole());
        etUPass.setHint("Mật khẩu (để trống = giữ nguyên)");

        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Sửa tài khoản")
                .setView(v)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Huỷ", null)
                .create();
        dlg.show();

        Button ok = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(btn -> {
            String name = etUName.getText().toString().trim();
            String email = etUEmail.getText().toString().trim();
            String role  = etURole.getText().toString().trim();
            String pass  = etUPass.getText().toString().trim();

            if (name.isEmpty()){ etUName.setError("Nhập username"); return; }
            if (role.isEmpty()) role = "user";

            String passwordToSend = pass.isEmpty()
                    ? (u.getPasswordHash()==null?"":u.getPasswordHash())
                    : pass;

            try{
                JSONObject js = new JSONObject();
                js.put("id", u.getId());
                js.put("username", name);
                js.put("email", email);
                js.put("role", role);
                js.put("passwordHash", passwordToSend);

                RequestBody body = RequestBody.create(js.toString(), ApiClient.JSON);
                Request.Builder rb = new Request.Builder()
                        .url(USERS_URL + "/" + u.getId())
                        .put(body);
                withAuth(rb);

                http.newCall(rb.build()).enqueue(new Callback() {
                    @Override public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> toast("Cập nhật lỗi"));
                    }
                    @Override public void onResponse(Call call, Response res) throws IOException {
                        res.close();
                        runOnUiThread(() -> {
                            if (res.isSuccessful()){ toast("Đã cập nhật"); loadUsers(); dlg.dismiss(); }
                            else toast("Cập nhật lỗi: " + res.code());
                        });
                    }
                });
            }catch (Exception ignore){}
        });
    }

    // ------- Delete -------
    private void confirmDelete(User u){
        new AlertDialog.Builder(this)
                .setMessage("Xoá tài khoản '" + u.getUsername() + "'?")
                .setPositiveButton("Xoá", (d,w)-> {
                    Request.Builder rb = new Request.Builder()
                            .url(USERS_URL + "/" + u.getId())
                            .delete();
                    withAuth(rb);
                    http.newCall(rb.build()).enqueue(new Callback() {
                        @Override public void onFailure(Call call, IOException e) {
                            runOnUiThread(() -> toast("Xoá lỗi"));
                        }
                        @Override public void onResponse(Call call, Response res) throws IOException {
                            res.close();
                            runOnUiThread(() -> {
                                if (res.isSuccessful()){ toast("Đã xoá"); loadUsers(); }
                                else toast("Xoá lỗi: " + res.code());
                            });
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // (tuỳ chọn) Logout
    @SuppressWarnings("unused")
    private void logout(){
        TokenStore.clear(getApplicationContext());
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
