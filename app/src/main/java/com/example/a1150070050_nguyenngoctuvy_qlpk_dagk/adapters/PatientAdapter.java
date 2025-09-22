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
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Patient;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {

    private List<Patient> patients;
    private OnPatientActionListener listener;
    private Context context;

    public interface OnPatientActionListener {
        void onUpdatePatient(Patient patient);
        void onDeletePatient(Patient patient);
    }

    public PatientAdapter(Context context, List<Patient> patients, OnPatientActionListener listener) {
        this.context = context;
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Patient patient = patients.get(position);

        holder.tvName.setText(patient.getFullName());
        holder.tvPhone.setText("📞 " + patient.getPhone());
        holder.tvGender.setText("Giới tính: " + patient.getGender());
        holder.tvDob.setText("Ngày sinh: " + patient.getDob());
        holder.tvAddress.setText("🏠 " + patient.getAddress());

        // click để sửa
        holder.itemView.setOnClickListener(v -> showEditDialog(patient));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    private void showEditDialog(Patient patient) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_patient, null);
        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etDob = dialogView.findViewById(R.id.etEditDob);
        EditText etGender = dialogView.findViewById(R.id.etEditGender);
        EditText etPhone = dialogView.findViewById(R.id.etEditPhone);
        EditText etAddress = dialogView.findViewById(R.id.etEditAddress);

        etName.setText(patient.getFullName());
        etDob.setText(patient.getDob());
        etGender.setText(patient.getGender());
        etPhone.setText(patient.getPhone());
        etAddress.setText(patient.getAddress());

        new AlertDialog.Builder(context)
                .setTitle("Cập nhật bệnh nhân")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    patient.setFullName(etName.getText().toString());
                    patient.setDob(etDob.getText().toString());
                    patient.setGender(etGender.getText().toString());
                    patient.setPhone(etPhone.getText().toString());
                    patient.setAddress(etAddress.getText().toString());
                    listener.onUpdatePatient(patient);
                })
                .setNegativeButton("Xóa", (dialog, which) -> listener.onDeletePatient(patient))
                .setNeutralButton("Hủy", null)
                .show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvGender, tvDob, tvAddress;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPatientName);
            tvPhone = itemView.findViewById(R.id.tvPatientPhone);
            tvGender = itemView.findViewById(R.id.tvPatientGender);
            tvDob = itemView.findViewById(R.id.tvPatientDob);
            tvAddress = itemView.findViewById(R.id.tvPatientAddress);
        }
    }
}
