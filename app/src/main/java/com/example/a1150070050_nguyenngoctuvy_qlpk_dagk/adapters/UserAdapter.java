package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface Listener {
        void onEdit(User u);
        void onDelete(User u);
    }

    private final List<User> data;
    private final Listener listener;

    public UserAdapter(List<User> data, Listener listener) {
        this.data = data; this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = data.get(pos);
        h.tvName.setText(u.getUsername());
        h.tvEmail.setText(u.getEmail() == null ? "" : u.getEmail());
        h.tvRole.setText(u.getRole() == null ? "user" : u.getRole());
        h.btnEdit.setOnClickListener(v -> listener.onEdit(u));
        h.btnDel.setOnClickListener(v -> listener.onDelete(u));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        ImageButton btnEdit, btnDel;
        VH(@NonNull View v){
            super(v);
            tvName = v.findViewById(R.id.tvUserName);
            tvEmail = v.findViewById(R.id.tvUserEmail);
            tvRole = v.findViewById(R.id.tvUserRole);
            btnEdit = v.findViewById(R.id.btnEditUser);
            btnDel = v.findViewById(R.id.btnDeleteUser);
        }
    }
}
