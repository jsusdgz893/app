package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class principal extends AppCompatActivity {

    private Button btnLogOut, btnDescribeProfessor, btnCancelForm;
    private LinearLayout formDescribeProfessor;
    private GoogleSignInClient googleSignInClient;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    

    private Spinner spinnerProfesores, spinnerMaterias;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        spinnerProfesores = findViewById(R.id.spinnerProfesores);
        spinnerMaterias = findViewById(R.id.spinnerMaterias);
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
        btnCancelForm.setOnClickListener(v -> formDescribeProfessor.setVisibility(View.GONE));
    }

    private void loadProfesores() {
        CollectionReference profesoresRef = db.collection("profesores");
        profesoresRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String nombre = doc.getString("nombre");
                    if (nombre != null) {
                        adapter.add(nombre);
                    }
                }
                spinnerProfesores.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Error al cargar profesores", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMaterias() {
        CollectionReference materiasRef = db.collection("materias");
        materiasRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String nombre = doc.getString("nombre");
                    if (nombre != null) {
                        adapter.add(nombre);
                    }
                }
                spinnerMaterias.setAdapter(adapter);
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
        if (formDescribeProfessor.getVisibility() == View.GONE) {
            formDescribeProfessor.setVisibility(View.VISIBLE);
        } else {
            formDescribeProfessor.setVisibility(View.GONE);
        }
    }


}

