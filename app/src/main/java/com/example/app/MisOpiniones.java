package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class MisOpiniones extends AppCompatActivity implements ReseñaAdapter.OnEditClickListener {
    private RecyclerView recyclerView;
    private ReseñaAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ConstraintLayout cuadroEditar;
    private Button btnVolver;
    private TextView tvNoResenas; // TextView para mostrar cuando no hay reseñas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mis_opiniones);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cuadroEditar = findViewById(R.id.CuadroEditar);
        recyclerView = findViewById(R.id.CajaDeOpiniones);
        tvNoResenas = findViewById(R.id.tvempty); // Asegúrate de tener este TextView en tu layout
        btnVolver = findViewById(R.id.btnVolverOpiniones);

        // Obtener las reseñas del usuario
        getReseñasDelUsuario(reseñas -> {
            adapter.actualizarLista(reseñas);
            // Mostrar u ocultar el mensaje según si hay reseñas
            if (reseñas.isEmpty()) {
                tvNoResenas.setVisibility(View.VISIBLE);
                tvNoResenas.setText("No haz dejado ninguna reseña");
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoResenas.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Configurar RecyclerView
        adapter = new ReseñaAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button btnCerrar = cuadroEditar.findViewById(R.id.btnCancelar); // Asegúrate de tener un botón con este ID en el layout

        // Configura el listener para ocultar el cuadroEditar
        btnCerrar.setOnClickListener(v -> {
            cuadroEditar.setVisibility(View.GONE); // Oculta el cuadro de edición
        });
        btnVolver = findViewById(R.id.btnVolverOpiniones);

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volver directamente a la actividad anterior
                onBackPressed(); // Llama al método que maneja el retroceso
            }
        });



    }
    private void actualizarComentarioEnFirestore(String reseñaId, String nuevoComentario) {
        db.collection("resenas")
                .document(reseñaId)  // Usamos el ID de la reseña para identificar el documento
                .update("comentario", nuevoComentario)  // Actualizamos el campo "comentario"
                .addOnSuccessListener(aVoid -> {
                    // Éxito al guardar el cambio
                    Log.d("MisOpiniones", "Comentario actualizado correctamente");
                })
                .addOnFailureListener(e -> {
                    // Error al guardar el cambio
                    Log.e("MisOpiniones", "Error al actualizar el comentario", e);
                });
    }
    @Override
    public void onEditClick(Reseña reseña) {
        cuadroEditar.setVisibility(View.VISIBLE);

        EditText editComentario = cuadroEditar.findViewById(R.id.editComentario);
        editComentario.setText(reseña.getComentario());

        Button btnGuardar = cuadroEditar.findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> {
            String nuevoComentario = editComentario.getText().toString();

            if (nuevoComentario.isEmpty()) {
                editComentario.setError("El comentario no puede estar vacío");
                return;
            }

            actualizarComentarioEnFirestore(reseña.getIdResenia(), nuevoComentario); // corregido

            cuadroEditar.setVisibility(View.GONE);
            // Recargar la actividad para reflejar los cambios

            reseña.setComentario(nuevoComentario); // ACTUALIZAMOS EL OBJETO EN MEMORIA
            adapter.notifyDataSetChanged();
        });
    }
    private List<Reseña> obtenerListaReseñas() {
        List<Reseña> lista = new ArrayList<>();
//        lista.add(new Reseña(4.0, "Maestros que dejan tareas más de los que explican", "Prof. Juan Pérez", "H9dY6e40GJ1ZBY52mEcG", "PROGRAMACION ORIENTADA A OBJETOS"));
//        lista.add(new Reseña(4.5, "Explica claramente", "Prof. María López", "aPHHUlJDJ7Xg2mcENpjr", "MATEMATICAS"));
//        lista.add(new Reseña(4.7, "Exámenes muy difíciles", "Prof. Carlos Gómez", "bPHHUlJDJ7Xg2mcENpjr", "FISICA"));
//        lista.add(new Reseña(3.9, "No es tan claro", "Prof. Ana Martínez", "cPHHUlJDJ7Xg2mcENpjr", "HISTORIA"));
//        lista.add(new Reseña(4.2, "Es muy detallista en sus explicaciones", "Prof. Luis Fernández", "dPHHUlJDJ7Xg2mcENpjr", "QUIMICA"));
//        lista.add(new Reseña(4.3, "A veces se enfoca mucho en la teoría", "Prof. Laura Sánchez", "ePHHUlJDJ7Xg2mcENpjr", "BIOLOGIA"));
//        lista.add(new Reseña(4.6, "Excelente, muy buen profesor", "Prof. Pablo Ramírez", "fPHHUlJDJ7Xg2mcENpjr", "FILOSOFIA"));
//        lista.add(new Reseña(4.1, "Interesante, pero muy rígido", "Prof. Andrea Díaz", "gPHHUlJDJ7Xg2mcENpjr", "GEOGRAFIA"));
//        lista.add(new Reseña(4.9, "Muy buen enfoque y clara explicación", "Prof. Ricardo Torres", "hPHHUlJDJ7Xg2mcENpjr", "LENGUA Y LITERATURA"));
//        lista.add(new Reseña(4.4, "Hace que los temas sean interesantes", "Prof. Carmen Ruiz", "iPHHUlJDJ7Xg2mcENpjr", "ARTE"));
        return lista;
    }
    private void getReseñasDelUsuario(ReseñasCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.d("User Info", "No user is signed in");
            callback.onReseñasLoaded(new ArrayList<>());
            return;
        }

        String userId = currentUser.getUid();
        List<Reseña> lista = new ArrayList<>();

        db.collection("resenas")
                .whereEqualTo("id_usuario", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("Reseña Error", "Error al obtener reseñas: ", task.getException());
                        callback.onReseñasLoaded(new ArrayList<>());
                        return;
                    }

                    if (task.getResult().isEmpty()) {
                        callback.onReseñasLoaded(new ArrayList<>());
                        return;
                    }

                    // Contador para manejar las llamadas asíncronas
                    final int[] pendingRequests = {task.getResult().size()};

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Convertir datos de forma segura
                        double calificacion = document.getDouble("calificacion");
                        String comentario = document.getString("comentario");
                        String idProfesor = document.getString("id_profesor");
                        String materia = document.getString("materia");
                        String idResenia = document.getId();

                        // Manejo seguro de listas
                        List<String> likedBy = document.get("likedBy") != null ?
                                (List<String>) document.get("likedBy") : new ArrayList<>();
                        List<String> dislikedBy = document.get("dislikedBy") != null ?
                                (List<String>) document.get("dislikedBy") : new ArrayList<>();

                        // Manejo seguro de números
                        String idUsuario = document.getString("id_usuario");

                        // Obtener nombre del profesor
                        db.collection("profesores").document(idProfesor)
                                .get()
                                .addOnCompleteListener(professorTask -> {
                                    String profesorNombre = "Profesor desconocido";

                                    if (professorTask.isSuccessful() && professorTask.getResult() != null) {
                                        profesorNombre = professorTask.getResult().getString("nombre");
                                    }

                                    // Crear y añadir reseña
                                    lista.add(new Reseña(
                                            calificacion,
                                            comentario,
                                            profesorNombre,
                                            idProfesor,
                                            materia,
                                            idResenia,
                                            likedBy,
                                            dislikedBy,
                                            idUsuario
                                    ));

                                    // Verificar si todas las solicitudes han terminado
                                    pendingRequests[0]--;
                                    if (pendingRequests[0] == 0) {
                                        callback.onReseñasLoaded(lista);
                                    }
                                });
                    }
                });
    }
    public interface ReseñasCallback {
        void onReseñasLoaded(List<Reseña> reseñas);
    }


}
