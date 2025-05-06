package com.example.app;

import static com.example.app.R.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.ArrayAdapter;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import android.widget.ImageView;
import android.widget.TextView;



public class Principal extends AppCompatActivity {

    private RecyclerView recyclerResenas;
    private ResenaRecyclerAdapter resenaAdapter;

    private Button btnDescribeProfessor, btnCancelForm, btnSaveForm;
    private EditText editTextDescriptionProfessor;
    private RatingBar ratingBarProfessor;
    private LinearLayout formDescribeProfessor;
    private GoogleSignInClient googleSignInClient;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView emptyResenas2, textViewLastReviews;


    private AutoCompleteTextView autoCompleteProfesores, autoCompleteMaterias;
    private FirebaseFirestore db;

    //para la busqueda del profesor
    private SearchView searchView;
    private RecyclerView recyclerResultadosBusqueda;
    private List<Map<String, Object>> listaProfesores = new ArrayList<>();
    private SearchAdapter searchAdapter;

    //para que cada profesor tenga sus materias
    private List<String> materiasDelProfesorIds = new ArrayList<>(); // Almacena IDs
    private Map<String, String> mapMaterias = new HashMap<>(); // ID -> Nombre
    private ArrayAdapter<String> materiasAdapter; // Adaptador específico para materias

    public static void hideKeyboard(Activity activity) { //Funcion para esconder el teclado despues de una accion
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        hideKeyboard(Principal.this);

        recyclerResenas = findViewById(R.id.recyclerResenas);
        recyclerResenas.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Estilizar el título "CUCEI IA" (mismo estilo que en Inicio.java)
        styleToolbarTitle(toolbar);

        textViewLastReviews = findViewById(id.textViewLastReviews);

        autoCompleteProfesores = findViewById(R.id.autoCompleteProfesores);
        autoCompleteMaterias = findViewById(R.id.autoCompleteMaterias);
        db = FirebaseFirestore.getInstance();

        cargarUltimasResena();

        loadProfesores();
        loadMaterias();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        //para que cada profesor tenga sus materias
        autoCompleteMaterias.setEnabled(false); // Deshabilitar inicialmente
        materiasAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteMaterias.setAdapter(materiasAdapter);


        // Obtener los elementos del header
        View headerView = navigationView.getHeaderView(0);
        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewProfile);
        TextView textViewName = headerView.findViewById(R.id.textViewName);
        TextView textViewEmail = headerView.findViewById(R.id.textViewEmail);

        emptyResenas2 = findViewById(R.id.emptyResenas3);

// Obtener información del usuario actual
        AtomicReference<FirebaseUser> user = new AtomicReference<>(FirebaseAuth.getInstance().getCurrentUser());
        if (user.get() != null) {
            String name = user.get().getDisplayName();
            String email = user.get().getEmail();
            if (name != null) {
                textViewName.setText(name);
            }
            if (email != null) {
                textViewEmail.setText(email);
            }


        }

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Menú navegación
        navigationView.setNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_profesores_opiniones) {
                startActivity(new Intent(this, MisOpiniones.class));
            } else if (itemId == R.id.nav_soporte) {
                startActivity(new Intent(this, SoporteActivity.class));
            } else if (itemId == R.id.nav_configuracion) {
                startActivity(new Intent(this, ConfiguracionActivity.class));
            } else if (itemId == R.id.nav_acerca) {
                startActivity(new Intent(this, AcercaActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });

      //para buscar profesor
        setupSearchView();

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnDescribeProfessor = findViewById(R.id.btnDescribeProfessor);
        btnCancelForm = findViewById(R.id.btnCancelForm);
        formDescribeProfessor = findViewById(R.id.formDescribeProfessor);

        btnDescribeProfessor.setOnClickListener(v -> toggleFormVisibility());
        btnSaveForm = findViewById(R.id.btnSaveForm);
        editTextDescriptionProfessor = findViewById(R.id.editTextDescriptionProfessor);
        ratingBarProfessor = findViewById(R.id.ratingBarProfessor);

        btnCancelForm.setOnClickListener(v -> {
            hideKeyboard(Principal.this);
            limpiarCamposFormulario(); // limpiamos campos
            searchView.setVisibility(View.VISIBLE);
            formDescribeProfessor.setVisibility(View.GONE);
            btnDescribeProfessor.setVisibility(View.VISIBLE);
            recyclerResenas.setVisibility(View.VISIBLE);
            textViewLastReviews.setVisibility(View.VISIBLE);
            emptyResenas2.setVisibility(View.GONE);
        });
        //cargar materias del profesor seleccionado
        autoCompleteProfesores.setOnItemClickListener((parent, view, position, id) -> {
            String nombreProfesor = (String) parent.getItemAtPosition(position);
            cargarMateriasDelProfesor(nombreProfesor);
        });
        //para guardar la reseña
        btnSaveForm.setOnClickListener(v -> {
            String profesorSeleccionado = autoCompleteProfesores.getText().toString().trim();
            String comentario = editTextDescriptionProfessor.getText().toString().trim();
            Integer likes = 0, dislikes = 0;
            float calificacion = ratingBarProfessor.getRating();

            String nombreMateriaSeleccionada = autoCompleteMaterias.getText().toString().trim();

            // Guardar normalmente nombreMateriaSeleccionada
            if (profesorSeleccionado.isEmpty() || nombreMateriaSeleccionada.isEmpty() || comentario.isEmpty() || calificacion == 0f) {
                Toast.makeText(this, "Completa todos los campos y selecciona una calificación", Toast.LENGTH_SHORT).show();
                return;
            }

            user.set(FirebaseAuth.getInstance().getCurrentUser());
            if (user.get() == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            String idUsuario = user.get().getUid();

            db.collection("profesores")
                    .whereEqualTo("nombre", profesorSeleccionado)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot profDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String idProfesor = profDoc.getId();

                            List<String> likedBy = new ArrayList<>();
                            List<String> dislikedBy = new ArrayList<>();

                            Map<String, Object> resena = new HashMap<>();
                            resena.put("id_profesor", idProfesor); // Asegúrate de guardar el ID correcto
                            resena.put("nombre_profesor", profesorSeleccionado);
                            resena.put("comentario", comentario);
                            resena.put("calificacion", calificacion);
                            resena.put("id_usuario", idUsuario);
                            resena.put("materia", nombreMateriaSeleccionada);
                            resena.put("likes", likes);
                            resena.put("dislikes", dislikes);
                            resena.put("likedBy", likedBy);
                            resena.put("dislikedBy", dislikedBy);
                            resena.put("fecha", FieldValue.serverTimestamp());


                            db.collection("resenas")
                                    .add(resena)
                                    .addOnSuccessListener(documentReference -> {
                                        String idResena = documentReference.getId();
                                        documentReference.update("id", idResena)
                                                .addOnSuccessListener(aVoid -> {
                                                    runOnUiThread(() -> {
                                                        cargarUltimasResena();
                                                        limpiarCamposFormulario(); // limpiamos campos
                                                        searchView.setVisibility(View.VISIBLE);
                                                        hideKeyboard(Principal.this);
                                                        formDescribeProfessor.setVisibility(View.GONE);
                                                        btnDescribeProfessor.setVisibility(View.VISIBLE);
                                                        textViewLastReviews.setVisibility(View.VISIBLE);
                                                        emptyResenas2.setVisibility(View.GONE);
                                                        recyclerResenas.setVisibility(View.VISIBLE);
                                                        Toast.makeText(this, "Reseña enviada con éxito", Toast.LENGTH_SHORT).show();
                                                    });
                                                });
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

    @Override
    protected void onResume() {
        super.onResume();
        cargarUltimasResena();
    }

    private void cargarUltimasResena() {
        db.collection("resenas")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            findViewById(R.id.emptyResenas3).setVisibility(View.VISIBLE);
                            recyclerResenas.setVisibility(View.GONE);
                            return;
                        }

                        List<Map<String, Object>> resenas = new ArrayList<>();
                        List<String> profesorIds = new ArrayList<>();

                        // Primera pasada: recolectar datos básicos y IDs de profesores
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> resena = new HashMap<>();
                            resena.put("id", document.getId());
                            resena.put("materia", document.getString("materia"));
                            resena.put("comentario", document.getString("comentario"));
                            resena.put("calificacion", document.getDouble("calificacion"));
                            resena.put("id_profesor", document.getString("id_profesor"));
                            resena.put("id_usuario", document.getString("id_usuario"));
                            resena.put("likes", document.get("likes"));
                            resena.put("dislikes", document.get("dislikes"));
                            resena.put("likedBy", document.get("likedBy"));
                            resena.put("dislikedBy", document.get("dislikedBy"));

                            String idProfesor = document.getString("id_profesor");
                            if (idProfesor != null && !profesorIds.contains(idProfesor)) {
                                profesorIds.add(idProfesor);
                            }
                            resenas.add(resena);
                        }


                        // Consulta batch de nombres de profesores
                        db.collection("profesores")
                                .whereIn(FieldPath.documentId(), profesorIds)
                                .get()
                                .addOnCompleteListener(profTask -> {
                                    if (profTask.isSuccessful()) {
                                        Map<String, String> profesorNombres = new HashMap<>();
                                        for (DocumentSnapshot doc : profTask.getResult()) {
                                            profesorNombres.put(doc.getId(), doc.getString("nombre"));
                                        }

                                        // Asignar nombres a las reseñas
                                        for (Map<String, Object> resena : resenas) {
                                            String profId = (String) resena.get("id_profesor");
                                            resena.put("nombre_profesor", profesorNombres.getOrDefault(profId, "Profesor desconocido"));
                                        }

                                        resenaAdapter = new ResenaRecyclerAdapter(resenas, Principal.this);
                                        recyclerResenas.setAdapter(resenaAdapter);
                                        recyclerResenas.setVisibility(View.VISIBLE);
                                        findViewById(R.id.emptyResenas3).setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Error al cargar reseñas", Toast.LENGTH_SHORT).show();
                    }
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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, nombres);
                autoCompleteProfesores.setAdapter(adapter);

                // Listener para cuando seleccionen un profesor
                autoCompleteProfesores.setOnItemClickListener((parent, view, position, id) -> {
                    String nombreProfesor = (String) parent.getItemAtPosition(position);
                    cargarMateriasDelProfesor(nombreProfesor);
                    autoCompleteMaterias.setText(""); // Limpiar selección previa
                });
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

    private void toggleFormVisibility() {
        formDescribeProfessor.setVisibility(View.VISIBLE);
        btnDescribeProfessor.setVisibility(View.GONE);
        searchView.setVisibility(View.INVISIBLE);
        recyclerResenas.setVisibility(View.GONE);
        textViewLastReviews.setVisibility(View.GONE);
        emptyResenas2.setVisibility(View.GONE);
    }
    private void limpiarCamposFormulario() {
        autoCompleteProfesores.setText("");
        autoCompleteMaterias.setText("");
        editTextDescriptionProfessor.setText("");
        ratingBarProfessor.setRating(0f);
        autoCompleteMaterias.setEnabled(false);
        materiasAdapter.clear();
    }


//para la busqueda de profesor
    private void setupSearchView() {
        searchView = findViewById(R.id.searchView);

        recyclerResultadosBusqueda = findViewById(R.id.recyclerResultadosBusqueda);


        // Configurar RecyclerView
        recyclerResultadosBusqueda.setLayoutManager(new LinearLayoutManager(this));


        searchAdapter = new SearchAdapter(listaProfesores, profesor -> {
            Intent intent = new Intent(Principal.this, TeacherProfileActivity.class);
            intent.putExtra("id_profesor", (String) profesor.get("id"));
            intent.putExtra("nombre_profesor", (String) profesor.get("nombre"));
            startActivity(intent);
        });
        recyclerResultadosBusqueda.setAdapter(searchAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                textViewLastReviews = findViewById(R.id.textViewLastReviews);
                textViewLastReviews.setVisibility(View.GONE);
                recyclerResenas.setVisibility(View.GONE);
                btnDescribeProfessor.setVisibility(View.GONE);
                buscarProfesores(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    textViewLastReviews = findViewById(R.id.textViewLastReviews);
                    textViewLastReviews.setVisibility(View.VISIBLE);
                    recyclerResenas.setVisibility(View.VISIBLE);
                    btnDescribeProfessor.setVisibility(View.VISIBLE);
                    recyclerResultadosBusqueda.setVisibility(View.GONE);
                }
                return false;
            }
        });
    }

    private void buscarProfesores(String nombre) {
        // Convertir la búsqueda a minúsculas para hacerla case-insensitive
        String busqueda = nombre.toLowerCase();

        db.collection("profesores")
               .orderBy("nombre")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaProfesores.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombreProfesor = document.getString("nombre");
                            if (nombreProfesor != null && nombreProfesor.toLowerCase().contains(busqueda)) {
                                Map<String, Object> profesor = new HashMap<>();
                                profesor.put("id", document.getId());
                                profesor.put("nombre", nombreProfesor);
                                // Agregar más campos si se necesitan despues
                                listaProfesores.add(profesor);
                            }
                        }

                        if (listaProfesores.isEmpty()) {
                            Toast.makeText(this, "No se encontraron profesores", Toast.LENGTH_SHORT).show();
                            recyclerResultadosBusqueda.setVisibility(View.GONE);
                        } else {
                            searchAdapter.notifyDataSetChanged();
                            recyclerResultadosBusqueda.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Error al buscar profesores", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error al buscar profesores", task.getException());
                    }
                });
    }

    //para que cada profesor tenga sus materias
    private void cargarMateriasDelProfesor(String nombreProfesor) {
        db.collection("profesores")
                .whereEqualTo("nombre", nombreProfesor)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        materiasDelProfesorIds = (List<String>) task.getResult().getDocuments().get(0).get("materias");
                        //validar que tenga materias el profesor
                        if (materiasDelProfesorIds != null && !materiasDelProfesorIds.isEmpty()) {
                            // 1. Limpiar el campo
                            autoCompleteMaterias.setText("");

                            // 2. Cargar SOLO las materias válidas
                            db.collection("materias")
                                    .whereIn(FieldPath.documentId(), materiasDelProfesorIds)
                                    .get()
                                    .addOnCompleteListener(materiasTask -> {
                                        if (materiasTask.isSuccessful()) {
                                            List<String> nombresMaterias = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : materiasTask.getResult()) {
                                                nombresMaterias.add(doc.getString("nombre"));
                                            }

                                            // 3. Usar un adaptador NUEVO (no el global)
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                                    Principal.this,
                                                    android.R.layout.simple_dropdown_item_1line,
                                                    nombresMaterias
                                            );
                                            autoCompleteMaterias.setAdapter(adapter);
                                            autoCompleteMaterias.setEnabled(true);
                                            autoCompleteMaterias.setFocusable(true);
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Este profesor no tiene materias asignadas aún", Toast.LENGTH_SHORT).show();
                            autoCompleteMaterias.setEnabled(false);
                        }

                    }
                });
    }

    private void styleToolbarTitle(Toolbar toolbar) {
        String fullText = "CUCEI IA";
        SpannableString spannableString = new SpannableString(fullText);

        // Color blanco para "CUCEI"
        spannableString.setSpan(
                new ForegroundColorSpan(Color.WHITE),
                0, 5,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Color azul claro para "IA" (mismo color que en Inicio.java)
        int lightblue = Color.parseColor("#0091EA");
        spannableString.setSpan(
                new ForegroundColorSpan(lightblue),
                6, 8,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Establecer el título estilizado
        getSupportActionBar().setTitle(spannableString);
    }


}

