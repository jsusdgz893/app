package com.example.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TeacherProfileActivity extends AppCompatActivity {
    private TextView tvNombreProfesor,tvCalPromedio, tvTotalResenias;//linea agregada
    private ListView listViewResenas;
    private FirebaseFirestore db;
    private String idProfesor, nombreProfesor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_profile);

        db = FirebaseFirestore.getInstance();
        idProfesor = getIntent().getStringExtra("id_profesor");
        nombreProfesor = getIntent().getStringExtra("nombre_profesor");

        tvNombreProfesor = findViewById(R.id.textView);
        listViewResenas = findViewById(R.id.listViewResenas);

        tvCalPromedio = findViewById(R.id.tvCalPromedio);//linea agregada
        tvTotalResenias = findViewById(R.id.tvTotalResenias);//linea agregada

        tvNombreProfesor.setText(nombreProfesor);
        cargarResenasDelProfesor();

        findViewById(R.id.backButtonTeacherProfile).setOnClickListener(v -> finish());
    }

    private void cargarResenasDelProfesor() {
        db.collection("resenas")
                .whereEqualTo("id_profesor", idProfesor)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> resenas = new ArrayList<>();
                        int totalResenias=0;//linea agregada
                        float sumaCalificaciones=0;//linea agregada

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int likes = getNumberAsInt(document.get("likes"));
                            int dislikes = getNumberAsInt(document.get("dislikes"));
                            Double calificacion = document.getDouble("calificacion");//linea agregada
                            totalResenias++;//linea agregada
                            sumaCalificaciones+=calificacion;//linea agregada
                            Map<String, Object> resena = new HashMap<>();
                            resena.put("materia", document.getString("materia"));
                            resena.put("calificacion", calificacion);
                            resena.put("comentario", document.getString("comentario"));
                            resena.put("id", document.getId());
                            resena.put("likes", likes);
                            resena.put("dislikes", dislikes);
                            resena.put("likedBy", document.get("likedBy"));
                            resena.put("dislikedBy", document.get("dislikedBy"));
                            resena.put("id_usuario", document.getString("id_usuario"));
                            resenas.add(resena);
                        }

                        // Calcular y mostrar promedio
                        if (totalResenias > 0) {
                            float promedio = sumaCalificaciones / totalResenias;
                            String promedioFormateado = String.format(Locale.getDefault(), "%.1f", promedio);
                            tvCalPromedio.setText(String.format("Calificación promedio: ⭐ %s/5",
                                    promedioFormateado));
                            tvTotalResenias.setText(String.format("Total de reseñas: %d", totalResenias));
                        } else {
                            tvCalPromedio.setText("Calificación promedio: Sin reseñas aún");
                        }

                        ResenaAdapter adapter = new ResenaAdapter(this, resenas);
                        listViewResenas.setAdapter(adapter);

                        if (resenas.isEmpty()) {
                            Toast.makeText(this, "No hay reseñas disponibles", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar reseñas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int getNumberAsInt(Object number) {
        if (number == null) {
            return 0;
        }
        if (number instanceof Long) {
            return ((Long) number).intValue();
        } else if (number instanceof Integer) {
            return (Integer) number;
        } else if (number instanceof Double) {
            return ((Double) number).intValue();
        }
        return 0;
    }

}
