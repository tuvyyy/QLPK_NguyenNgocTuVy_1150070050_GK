package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.R;
import com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model.Service;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onEdit(Service s);
        void onDelete(Service s);
    }

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private final List<Service> data;
    private final Listener listener;
    private boolean showLoadingRow = false;

    public ServiceAdapter(List<Service> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    public void setShowLoading(boolean show){
        if (show == showLoadingRow) return;
        showLoadingRow = show;
        if (show) {
            notifyItemInserted(getItemCount());
        } else {
            notifyItemRemoved(getItemCount());
        }
    }

    @Override public int getItemViewType(int position) {
        return (position < data.size()) ? TYPE_ITEM : TYPE_LOADING;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_service_loading, parent, false);
            return new LoadingVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_service, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_LOADING) return;
        ItemVH h = (ItemVH) holder;
        Service s = data.get(position);
        h.tvName.setText(s.getServiceName());
        h.tvPrice.setText(s.getPriceFormatted());
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(s); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(s); });
    }

    @Override
    public int getItemCount() {
        return data.size() + (showLoadingRow ? 1 : 0);
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageButton btnEdit, btnDelete;
        ItemVH(@NonNull View v) {
            super(v);
            tvName   = v.findViewById(R.id.tvServiceName);
            tvPrice  = v.findViewById(R.id.tvServicePrice);
            btnEdit  = v.findViewById(R.id.btnEditService);
            btnDelete= v.findViewById(R.id.btnDeleteService);
        }
    }
    static class LoadingVH extends RecyclerView.ViewHolder {
        LoadingVH(@NonNull View v) { super(v); }
    }
}
