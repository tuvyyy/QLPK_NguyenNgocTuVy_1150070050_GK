package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Patient;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.VH> {

    public interface Listener {
        void onEdit(Patient p);
        void onDelete(Patient p);
    }

    private final List<Patient> data;
    private final Listener listener;

    public PatientAdapter(List<Patient> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Patient p = data.get(pos);

        h.tvName.setText(p.getFullName() == null ? "" : p.getFullName());

        // map M/F/O -> VN
        String genderCode = p.getGender();
        String genderDisplay = displayFromCode(genderCode);
        h.tvGender.setText(genderDisplay);

        h.tvPhone.setText(p.getPhone() == null ? "" : p.getPhone());
        h.tvAddress.setText(p.getAddress() == null ? "" : p.getAddress());

        String dob = p.getDob();
        h.tvDob.setText(dob == null ? "" : dob);

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(p);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // ==== helpers ====
    private static String displayFromCode(String code) {
        if (code == null) return "—";
        switch (code.toUpperCase()) {
            case "M": return "Nam";
            case "F": return "Nữ";
            case "O": return "Không xác định";
            default:  return "—";
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvGender, tvPhone, tvAddress, tvDob;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvPatientName);
            tvGender  = v.findViewById(R.id.tvPatientGender);
            tvPhone   = v.findViewById(R.id.tvPatientPhone);
            tvAddress = v.findViewById(R.id.tvPatientAddress);
            tvDob     = v.findViewById(R.id.tvPatientDob);
            btnEdit   = v.findViewById(R.id.btnEditPatient);
            btnDelete = v.findViewById(R.id.btnDeletePatient);
        }
    }
}
