package com.example.app;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ProfesorViewHolder> {

    private List<Map<String, Object>> listaProfesores;
    private OnProfesorClickListener listener;

    public interface OnProfesorClickListener {
        void onProfesorClick(Map<String, Object> profesor);
    }

    public SearchAdapter(List<Map<String, Object>> listaProfesores, OnProfesorClickListener listener) {
        this.listaProfesores = listaProfesores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfesorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ProfesorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfesorViewHolder holder, int position) {
        Map<String, Object> profesor = listaProfesores.get(position);
        holder.textView.setText((String) profesor.get("nombre"));
        holder.textView.setTextColor(Color.WHITE);
        holder.itemView.setOnClickListener(v -> {
            listener.onProfesorClick(profesor);
        });
    }

    @Override
    public int getItemCount() {
        return listaProfesores.size();
    }

    static class ProfesorViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ProfesorViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}