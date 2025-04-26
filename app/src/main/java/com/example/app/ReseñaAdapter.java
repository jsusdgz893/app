package com.example.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class ReseñaAdapter extends RecyclerView.Adapter<ReseñaAdapter.ReseñaViewHolder> {

    private List<Reseña> lista; // Lista de reseñas a mostrar

    // Constructor que recibe la lista de reseñas
    public ReseñaAdapter(List<Reseña> lista) {
        this.lista = lista;
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
            // Aquí puedes implementar la acción para editar la reseña
            Toast.makeText(holder.itemView.getContext(), "Editar reseña de: " + reseña.getIdProfesor(), Toast.LENGTH_SHORT).show();
        });

        // Acción para el botón de eliminar (puedes personalizar la acción)
        holder.btnEliminar.setOnClickListener(v -> {
            // Aquí puedes implementar la acción para eliminar la reseña
            Toast.makeText(holder.itemView.getContext(), "Eliminar reseña de: " + reseña.getIdProfesor(), Toast.LENGTH_SHORT).show();
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
}