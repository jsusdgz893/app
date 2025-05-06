package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ConfiguracionActivity extends AppCompatActivity {
    private Button btnCerrarSesion, btnVolverPrincipal, btnAyuda;
    private TextView txtEliminarCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // Bind views
        btnCerrarSesion = findViewById(R.id.btnCerrarSesionConfig);
        btnVolverPrincipal = findViewById(R.id.btnVolverPrincipal);
        btnAyuda = findViewById(R.id.btnAyuda);
        txtEliminarCuenta = findViewById(R.id.txtEliminarCuenta);

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Inicio.class));
            finish();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        });

        // Eliminar cuenta
        txtEliminarCuenta.setOnClickListener(v -> {
            FirebaseAuth.getInstance().getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, Inicio.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Error al eliminar cuenta", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Volver al principal
        btnVolverPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volver directamente a la actividad anterior
                onBackPressed(); // Llama al método que maneja el retroceso
            }
        });

        // Ir a SoporteActivity
        btnAyuda.setOnClickListener(v -> {
            startActivity(new Intent(this, SoporteActivity.class));
        });
    }
}
