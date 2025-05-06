package com.example.app;

import static com.google.common.collect.ComparisonChain.start;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReseñaAdapter extends RecyclerView.Adapter<ReseñaAdapter.ReseñaViewHolder> {

    private final FirebaseUser currentUser;
    private final FirebaseFirestore db;
    private List<Reseña> lista;
    private OnEditClickListener onEditClickListener;

    public ReseñaAdapter(List<Reseña> lista, OnEditClickListener listener) {
        this.lista = lista;
        this.onEditClickListener = listener;
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public interface OnEditClickListener {
        void onEditClick(Reseña reseña);
    }

    public class ReseñaViewHolder extends RecyclerView.ViewHolder {
        TextView textoProfesor;
        TextView textoDescripcion;
        TextView textoCalificacion;
        View contenedor;
        Button btnEditar;
        Button btnEliminar;

        public ReseñaViewHolder(View itemView) {
            super(itemView);
            textoProfesor = itemView.findViewById(R.id.itemTexto);
            textoDescripcion = itemView.findViewById(R.id.textoDescripcion);
            textoCalificacion = itemView.findViewById(R.id.TextoCalificacion);
            contenedor = itemView.findViewById(R.id.ContDeTexto);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            // Eliminamos las referencias a los botones de like/dislike si existen
        }
    }

    @Override
    public ReseñaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resultado, parent, false);
        return new ReseñaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReseñaViewHolder holder, int position) {
        Reseña reseña = lista.get(position);

        holder.textoProfesor.setText(reseña.getProfesorNombre());
        String descripcion = "Materia: \n" + reseña.getMateria() + "\n\nComentario: \n" + reseña.getComentario();
        holder.textoDescripcion.setText(descripcion);
        holder.textoCalificacion.setText("Calificación: " + reseña.getCalificacion());

        holder.contenedor.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Toast.makeText(context, "Profesor: " + reseña.getIdProfesor(), Toast.LENGTH_SHORT).show();
        });

        holder.btnEditar.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(reseña);
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            String idResenia = reseña.getIdResenia();

            if (idResenia == null || idResenia.isEmpty()) {
                Toast.makeText(context, "Error: El ID de la reseña no es válido", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar la reseña de " + reseña.getProfesorNombre() + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        db.collection("resenas").document(idResenia)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Reseña eliminada exitosamente", Toast.LENGTH_SHORT).show();
                                    lista.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    notifyItemRangeChanged(holder.getAdapterPosition(), lista.size());
                                    if (lista.isEmpty()) {
                                        Toast.makeText(context, "No hay más reseñas para mostrar", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error al eliminar reseña: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void actualizarLista(List<Reseña> nuevaLista) {
        lista = nuevaLista;
        notifyDataSetChanged();
    }

    interface OnReseñaDeletedListener {
        void onReseñaDeleted();
        void onError(Exception e);
    }
}