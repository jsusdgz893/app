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
import java.util.Map;
import java.util.Set;

public class TeacherProfileActivity extends AppCompatActivity {
    private TextView tvNombreProfesor;
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

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> resena = new HashMap<>();
                            resena.put("materia", document.getString("materia"));
                            resena.put("calificacion", document.getDouble("calificacion"));
                            resena.put("comentario", document.getString("comentario"));
                            resenas.add(resena);
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
}
