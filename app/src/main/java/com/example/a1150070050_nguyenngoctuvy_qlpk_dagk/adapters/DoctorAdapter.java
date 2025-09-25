package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Doctor;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.VH> {
    public interface Listener { void onEdit(Doctor d); void onDelete(Doctor d); }
    private final List<Doctor> data; private final Listener listener;

    public DoctorAdapter(List<Doctor> data, Listener listener){ this.data=data; this.listener=listener; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_doctor, p, false);
        return new VH(view);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int i){
        Doctor d = data.get(i);
        h.name.setText(d.getFullname());
        h.spec.setText(d.getSpecialty());
        h.phone.setText(d.getPhone()==null?"—":d.getPhone());
        h.btnEdit.setOnClickListener(v -> listener.onEdit(d));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(d));
    }
    @Override public int getItemCount(){ return data.size(); }

    static class VH extends RecyclerView.ViewHolder{
        TextView name, spec, phone; ImageView btnEdit, btnDelete;
        VH(View v){ super(v);
            name=v.findViewById(R.id.tvDoctorName);
            spec=v.findViewById(R.id.tvDoctorSpecialty);
            phone=v.findViewById(R.id.tvDoctorPhone);
            btnEdit=v.findViewById(R.id.btnEdit);
            btnDelete=v.findViewById(R.id.btnDelete);
        }
    }
}
