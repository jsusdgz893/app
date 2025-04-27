package com.example.app;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AcercaActivity extends AppCompatActivity {

    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca);

        btnVolver = findViewById(R.id.btnVolverAcerca);

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volver directamente a la actividad anterior
                onBackPressed(); // Llama al método que maneja el retroceso
            }
        });
    }
}

