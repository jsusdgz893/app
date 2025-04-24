package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class principal extends AppCompatActivity {

    private Button btnLogOut, btnDescribeProfessor, btnCancelForm, btnSaveForm;
    private EditText editTextDescriptionProfessor;
    private RatingBar ratingBarProfessor;
    private LinearLayout formDescribeProfessor;
    private GoogleSignInClient googleSignInClient;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    


    private AutoCompleteTextView autoCompleteProfesores, autoCompleteMaterias;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        autoCompleteProfesores = findViewById(R.id.autoCompleteProfesores);
        autoCompleteMaterias = findViewById(R.id.autoCompleteMaterias);
        db = FirebaseFirestore.getInstance();


        loadProfesores();
        loadMaterias();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Menú navegación
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_perfil) {
                Toast.makeText(this, "Mi perfil", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_profesores_opiniones) {
                Toast.makeText(this, "Mis profesores y opiniones", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_soporte) {
                Toast.makeText(this, "Soporte", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_configuracion) {
                Toast.makeText(this, "Configuración", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_acerca) {
                Toast.makeText(this, "Acerca de la app", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogOut = findViewById(R.id.btnLogOut);
        btnDescribeProfessor = findViewById(R.id.btnDescribeProfessor);
        btnCancelForm = findViewById(R.id.btnCancelForm);
        formDescribeProfessor = findViewById(R.id.formDescribeProfessor);

        btnLogOut.setOnClickListener(v -> logoutUser());
        btnDescribeProfessor.setOnClickListener(v -> toggleFormVisibility());
        btnSaveForm = findViewById(R.id.btnSaveForm);
        editTextDescriptionProfessor = findViewById(R.id.editTextDescriptionProfessor);
        ratingBarProfessor = findViewById(R.id.ratingBarProfessor);

        btnCancelForm.setOnClickListener(v -> {
            limpiarCamposFormulario(); // limpiamos campos
            formDescribeProfessor.setVisibility(View.GONE);
            btnDescribeProfessor.setVisibility(View.VISIBLE);
            btnLogOut.setVisibility(View.VISIBLE);
        });

        //para guardar la reseña
        btnSaveForm.setOnClickListener(v -> {
            String profesorSeleccionado = autoCompleteProfesores.getText().toString().trim();
            String materiaSeleccionada = autoCompleteMaterias.getText().toString().trim();
            String comentario = editTextDescriptionProfessor.getText().toString().trim();
            float calificacion = ratingBarProfessor.getRating();

            if (profesorSeleccionado.isEmpty() || materiaSeleccionada.isEmpty() || comentario.isEmpty() || calificacion == 0f) {
                Toast.makeText(this, "Completa todos los campos y selecciona una calificación", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            String idUsuario = user.getUid();

            db.collection("profesores")
                    .whereEqualTo("nombre", profesorSeleccionado)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot profDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String idProfesor = profDoc.getId();

                            Map<String, Object> resena = new HashMap<>();
                            resena.put("comentario", comentario);
                            resena.put("calificacion", calificacion);
                            resena.put("id_usuario", idUsuario);
                            resena.put("id_profesor", idProfesor);
                            resena.put("materia", materiaSeleccionada);

                            db.collection("resenas")
                                    .add(resena)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "Reseña enviada con éxito", Toast.LENGTH_SHORT).show();
                                        limpiarCamposFormulario(); // limpiamos campos
                                        formDescribeProfessor.setVisibility(View.GONE);
                                        btnDescribeProfessor.setVisibility(View.VISIBLE);
                                        btnLogOut.setVisibility(View.VISIBLE);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al enviar reseña", Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            Toast.makeText(this, "Profesor no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al buscar profesor", Toast.LENGTH_SHORT).show();
                    });
        });
    }


    private void loadProfesores() {
        CollectionReference profesoresRef = db.collection("profesores");
        profesoresRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> nombres = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String nombre = doc.getString("nombre");
                    if (nombre != null) {
                        nombres.add(nombre);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombres);
                autoCompleteProfesores.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Error al cargar profesores", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMaterias() {
        CollectionReference materiasRef = db.collection("materias");
        materiasRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> nombres = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String nombre = doc.getString("nombre");
                    if (nombre != null) {
                        nombres.add(nombre);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombres);
                autoCompleteMaterias.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Error al cargar materias", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            getSharedPreferences("MisPreferencias", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Inicio.class));
            finish();
        });
    }


    private void toggleFormVisibility() {
        formDescribeProfessor.setVisibility(View.VISIBLE);
        btnDescribeProfessor.setVisibility(View.GONE);
        btnLogOut.setVisibility(View.GONE);
    }
    private void limpiarCamposFormulario() {
        autoCompleteProfesores.setText("");
        autoCompleteMaterias.setText("");
        editTextDescriptionProfessor.setText("");
        ratingBarProfessor.setRating(0f);
    }



}

