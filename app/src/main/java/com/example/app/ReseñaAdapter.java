package com.example.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class ReseñaAdapter extends RecyclerView.Adapter<ReseñaAdapter.ReseñaViewHolder> {

    private List<Reseña> lista; // Lista de reseñas a mostrar
    private OnEditClickListener onEditClickListener;

    public ReseñaAdapter(List<Reseña> lista, OnEditClickListener listener) {
        this.lista = lista;
        this.onEditClickListener = listener;
    }
    // Constructor que recibe la lista de reseñas
    public ReseñaAdapter(List<Reseña> lista) {
        this.lista = lista;
    }
    public interface OnEditClickListener {
        void onEditClick(Reseña reseña); // Recibimos la reseña como parámetro
    }
    // ViewHolder: representa cada elemento individual en el RecyclerView
    public class ReseñaViewHolder extends RecyclerView.ViewHolder {
        TextView textoProfesor;   // Texto para mostrar el nombre del profesor
        TextView textoDescripcion; // Texto para mostrar la materia y el comentario
        TextView textoCalificacion; // Texto para mostrar la calificación
        View contenedor;           // Contenedor del elemento completo
        Button btnEditar;          // Botón para editar la reseña
        Button btnEliminar;        // Botón para eliminar la reseña

        public ReseñaViewHolder(View itemView) {
            super(itemView);
            // Asocia las variables con las vistas del layout
            textoProfesor = itemView.findViewById(R.id.itemTexto);
            textoDescripcion = itemView.findViewById(R.id.textoDescripcion);
            textoCalificacion = itemView.findViewById(R.id.TextoCalificacion);
            contenedor = itemView.findViewById(R.id.ContDeTexto);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    // Crea nuevas vistas (invocado por el LayoutManager)
    @Override
    public ReseñaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Infla el layout que va a usar cada elemento de la lista
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resultado, parent, false);
        return new ReseñaViewHolder(view);
    }

    // Asocia los datos de una reseña a las vistas del ViewHolder
    @Override
    public void onBindViewHolder(ReseñaViewHolder holder, int position) {
        Reseña reseña = lista.get(position);

        // Muestra el nombre del profesor (título)
        holder.textoProfesor.setText(reseña.getIdProfesor());

        // Muestra la materia y el comentario en la descripción
        String descripcion = "Materia: \n" + reseña.getMateria() + "\n\nComentario: \n" + reseña.getComentario();
        holder.textoDescripcion.setText(descripcion);

        // Muestra la calificación del profesor
        String calificacion = "Calificación: " + reseña.getCalificacion();
        holder.textoCalificacion.setText(calificacion);

        // Si se hace clic en el contenedor, muestra un Toast con la información de la reseña
        holder.contenedor.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Toast.makeText(context, "Profesor: " + reseña.getIdProfesor(), Toast.LENGTH_SHORT).show();
        });

        // Acción para el botón de editar (puedes personalizar la acción)
        holder.btnEditar.setOnClickListener(v -> {
            // Notificar a la actividad que se hizo clic en el botón de editar y pasar la reseña
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(reseña); // Pasar la reseña al hacer clic
            }
        });

        // Acción para el botón de eliminar (puedes personalizar la acción)
        holder.btnEliminar.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();

            Log.d("FirestoreDelete", "Click en eliminar de: " + reseña.getIdProfesor());

            // Comprobamos que el ID de la reseña no sea nulo ni vacío
            String idResenia = reseña.getIdResenia();
            if (idResenia == null || idResenia.isEmpty()) {
                Log.e("FirestoreDelete", "ID de la reseña es nulo o vacío");
                Toast.makeText(context, "Error: El ID de la reseña no es válido", Toast.LENGTH_SHORT).show();
                return;  // Si el ID no es válido, no realizamos la eliminación
            }

            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar la reseña de " + reseña.getIdProfesor() + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Log.d("FirestoreDelete", "Intentando eliminar documento con ID: " + idResenia);

                        db.collection("resenas") // Nombre correcto de tu colección
                                .document(idResenia) // ID del documento
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirestoreDelete", "Documento eliminado exitosamente");

                                    Toast.makeText(context, "Reseña eliminada exitosamente", Toast.LENGTH_SHORT).show();

                                    lista.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    notifyItemRangeChanged(holder.getAdapterPosition(), lista.size());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreDelete", "Error al eliminar documento: " + e.getMessage(), e);

                                    Toast.makeText(context, "Error al eliminar reseña: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        Log.d("FirestoreDelete", "Eliminación cancelada por el usuario");
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    // Devuelve el número total de elementos que hay en la lista
    @Override
    public int getItemCount() {
        return lista.size();
    }

    // Método para actualizar la lista de reseñas
    public void actualizarLista(List<Reseña> nuevaLista) {
        lista = nuevaLista;
        notifyDataSetChanged(); // Notifica que los datos cambiaron para que se actualice el RecyclerView
    }
    interface OnReseñaDeletedListener {
        void onReseñaDeleted();
        void onError(Exception e);
    }
}

