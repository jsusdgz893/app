package com.example.app;

import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;



public class ResenaAdapter extends ArrayAdapter<Map<String, Object>> {
    public ResenaAdapter(Context context, List<Map<String, Object>> resenas) {
        super(context, R.layout.item_resena, resenas);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_resena, parent, false);
        }

        Map<String, Object> resena = getItem(position);

        TextView tvMateria = convertView.findViewById(R.id.tvMateria);
        TextView tvCalificacion = convertView.findViewById(R.id.tvCalificacion);
        TextView tvComentario = convertView.findViewById(R.id.tvComentario);

        tvMateria.setText(resena.get("materia").toString());

        float calificacion = ((Double) resena.get("calificacion")).floatValue();
        tvCalificacion.setText(String.format("⭐ %.1f/5", calificacion));

        tvComentario.setText(String.format("\"%s\"", resena.get("comentario").toString()));

        return convertView;
    }
}