package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.app.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class principal extends AppCompatActivity {

    private Button btnLogOut;
    private GoogleSignInClient googleSignInClient;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(v -> logoutUser());

        }

    private void logoutUser() {
        // 1. Cierra sesión en Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. Cierra sesión en Google (si aplica)
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // 3. Limpia datos locales (opcional)
            getSharedPreferences("MisPreferencias", MODE_PRIVATE).edit().clear().apply();

            // 4. Redirige al login
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Inicio.class));
            finish();
        });
    }

        //button = findViewById(R.id.button);

        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(principal.this, MainActivity.class);
                startActivity(intent);
            }
        });*/
    }

