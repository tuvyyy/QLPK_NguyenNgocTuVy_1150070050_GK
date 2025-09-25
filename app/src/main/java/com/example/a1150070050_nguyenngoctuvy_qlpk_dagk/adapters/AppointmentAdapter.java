package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface Listener {
        void onChangeStatus(Appointment appt);
        void onDelete(Appointment appt);
    }

    private final List<Appointment> data;
    private final Listener listener;

    public AppointmentAdapter(List<Appointment> data, Listener l) {
        this.data = data; this.listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Appointment a = data.get(position);
        h.tvTime.setText(a.getAppointmentDate().replace('T', ' '));
        h.tvDoctor.setText("BS: " + a.getDoctorName());
        h.tvPatient.setText("BN: " + a.getPatientName());
        h.tvService.setText("DV: " + a.getServiceName());
        h.tvStatus.setText(a.getStatus());

        h.btnStatus.setOnClickListener(v -> { if (listener != null) listener.onChangeStatus(a); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(a); });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTime, tvDoctor, tvPatient, tvService, tvStatus;
        ImageButton btnStatus, btnDelete;
        VH(@NonNull View v) {
            super(v);
            tvTime = v.findViewById(R.id.tvApptTime);
            tvDoctor = v.findViewById(R.id.tvApptDoctor);
            tvPatient = v.findViewById(R.id.tvApptPatient);
            tvService = v.findViewById(R.id.tvApptService);
            tvStatus = v.findViewById(R.id.tvApptStatus);
            btnStatus = v.findViewById(R.id.btnApptStatus);
            btnDelete = v.findViewById(R.id.btnApptDelete);
        }
    }
}
