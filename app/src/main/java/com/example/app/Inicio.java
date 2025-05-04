package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.HashMap;

public class Inicio extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button google_sign_in;

    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private GoogleSignInClient googleSignInClient;
    private int RC_SIGN_IN = 20;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo(): null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }


    @Override
    protected void onStart() {
        super.onStart();

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(Inicio.this, Principal.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBarLogin);

        TextView cuceiIA = findViewById(R.id.cuceiIA);

        String fullText = "CUCEI IA";

        SpannableString spannableString = new SpannableString(fullText);

        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int lightblue = Color.parseColor("#0091EA");

        spannableString.setSpan(new ForegroundColorSpan(lightblue), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        cuceiIA.setText(spannableString);

        mAuth = FirebaseAuth.getInstance();

        // Configuración de Firebase
        database = FirebaseDatabase.getInstance();

        // Configuración de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        google_sign_in = findViewById(R.id.btnSignInGoogle);
        google_sign_in.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Error en inicio de sesión", e);
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        google_sign_in.setEnabled(false);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        google_sign_in.setEnabled(true);
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (isEmailallowed(user.getEmail())) {
                            // Guardar el usuario en Firestore usando el correo como nombre del documento
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("uid", user.getUid());
                            userData.put("nombre", user.getDisplayName());
                            userData.put("correo", user.getEmail());
                            userData.put("fotoURL", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

                            db.collection("usuarios").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario guardado correctamente con correo"))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error al guardar usuario", e));
                            updateUI(user);
                            Toast.makeText(Inicio.this, "Bienvenido " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        } else {
                            signOutInvalidUser(user);
                            Toast.makeText(Inicio.this, "Solo es posible ingresar con un cuenta de google de la UDG", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        updateUI(null);
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(Inicio.this, Principal.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(Inicio.this, "Error en la autenticación",
                    Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isEmailallowed(String email) {
        String allowed_domain = "@alumnos.udg.mx";
        if (email != null) {
            return email.endsWith(allowed_domain);
        }
        return false;
    }

    private void signOutInvalidUser(FirebaseUser user) {
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        googleSignInClient.signOut().addOnCompleteListener(task1 -> {
                            mAuth.signOut();
                            google_sign_in.setEnabled(true);
                        });
                    }).addOnFailureListener(e -> {
                        Log.e("Auth", "Error al eliminar usuario", e);
                        googleSignInClient.signOut();
                        mAuth.signOut();
                        google_sign_in.setEnabled(true);
                        updateUI(null);
                    });
        } else {
            googleSignInClient.signOut();
            mAuth.signOut();
            updateUI(null);
        }
    }
}





