package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Doctor;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private List<Doctor> doctors;
    private OnDoctorActionListener listener;
    private Context context;

    public interface OnDoctorActionListener {
        void onUpdateDoctor(Doctor doctor);
        void onDeleteDoctor(Doctor doctor);
    }

    public DoctorAdapter(Context context, List<Doctor> doctors, OnDoctorActionListener listener) {
        this.context = context;
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.tvId.setVisibility(View.GONE);
        holder.tvName.setText("Tên: " + doctor.getFullname());
        holder.tvSpecialty.setText("Chuyên khoa: " + doctor.getSpecialty());
        holder.tvPhone.setText("SĐT: " + doctor.getPhone());
        holder.itemView.setOnClickListener(v -> showEditDialog(doctor));
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    private void showEditDialog(Doctor doctor) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_doctor, null);
        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etSpecialty = dialogView.findViewById(R.id.etEditSpecialty);
        EditText etPhone = dialogView.findViewById(R.id.etEditPhone);

        etName.setText(doctor.getFullname());
        etSpecialty.setText(doctor.getSpecialty());
        etPhone.setText(doctor.getPhone());

        new AlertDialog.Builder(context)
                .setTitle("Cập nhật bác sĩ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    doctor.setFullname(etName.getText().toString());
                    doctor.setSpecialty(etSpecialty.getText().toString());
                    doctor.setPhone(etPhone.getText().toString());
                    listener.onUpdateDoctor(doctor);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvSpecialty, tvPhone;
        ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvDoctorId);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvDoctorSpecialty);
            tvPhone = itemView.findViewById(R.id.tvDoctorPhone);
        }
    }
}
