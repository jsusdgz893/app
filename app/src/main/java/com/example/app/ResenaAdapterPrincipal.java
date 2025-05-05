package com.example.app;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResenaAdapterPrincipal extends ArrayAdapter<Map<String, Object>> {
    private final FirebaseFirestore db;
    private final FirebaseUser currentUser;

    private long lastClickTime = 0;
    public ResenaAdapterPrincipal(Context context, List<Map<String, Object>> resenas) {
        super(context, R.layout.item_resena_main, resenas);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_resena_main, parent, false);

            ImageButton btnLike = convertView.findViewById(R.id.btnLike);
            ImageButton btnDislike = convertView.findViewById(R.id.btnDislike);

            btnLike.setEnabled(true);
            btnDislike.setEnabled(true);
        }

        Map<String, Object> resena = getItem(position);
        if (resena == null) {
            return convertView;
        }

        // Configurar vistas
        TextView tvProfesor = convertView.findViewById(R.id.tvProfesorMain);
        TextView tvMateria = convertView.findViewById(R.id.tvMateriaMain);
        TextView tvCalificacion = convertView.findViewById(R.id.tvCalificacion);
        TextView tvComentario = convertView.findViewById(R.id.tvComentario);
        TextView textViewLikes = convertView.findViewById(R.id.textViewLikes);
        TextView textViewDislikes = convertView.findViewById(R.id.textViewDislikes);
        ImageButton btnLike = convertView.findViewById(R.id.btnLike);
        ImageButton btnDislike = convertView.findViewById(R.id.btnDislike);

        // Usar el nombre del profesor que ya viene en los datos
        String nombreProfesor = resena.containsKey("nombre_profesor") ?
                resena.get("nombre_profesor").toString() : "Profesor desconocido";
        tvProfesor.setText(nombreProfesor);

        tvMateria.setText(resena.containsKey("materia") ? resena.get("materia").toString() : "");

        float calificacion = resena.containsKey("calificacion") ?
                ((Number)resena.get("calificacion")).floatValue() : 0f;
        tvCalificacion.setText(String.format("⭐ %.1f/5", calificacion));

        tvComentario.setText(resena.containsKey("comentario") ?
                String.format("\"%s\"", resena.get("comentario").toString()) : "");


        String idResena = obtenerIdResena(resena);
        String idUsuarioResena = resena.get("id_usuario").toString();

        boolean esCreador = currentUser != null && currentUser.getUid().equals(idUsuarioResena);

        int likes = getNumberAsInt(resena.get("likes"));
        int dislikes = getNumberAsInt(resena.get("dislikes"));


        textViewLikes.setText(String.valueOf(likes));
        textViewDislikes.setText(String.valueOf(dislikes));


        if (esCreador) {
            // Deshabilitar botones para el creador
            btnLike.setEnabled(false);
            btnDislike.setEnabled(false);
            btnLike.setAlpha(0.3f);
            btnDislike.setAlpha(0.3f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                btnLike.setTooltipText("No puedes votar tu propia reseña");
                btnDislike.setTooltipText("No puedes votar tu propia reseña");
            }
        } else {
            // Configurar para usuarios que no son el creador
            setupVoteButtons(resena, btnLike, btnDislike, idResena);
        }


        return convertView;
    }

    private void setupVoteButtons(Map<String, Object> resena, ImageButton btnLike,
                                  ImageButton btnDislike, String reviewId) {
        if (currentUser == null) {
            btnLike.setEnabled(false);
            btnDislike.setEnabled(false);
            return;
        }

        List<String> likedBy = (List<String>) resena.get("likedBy");
        List<String> dislikedBy = (List<String>) resena.get("dislikedBy");

        boolean hasLiked = likedBy.contains(currentUser.getUid());
        boolean hasDisliked = dislikedBy.contains(currentUser.getUid());

        btnLike.setAlpha(hasLiked ? 0.5f : 1.0f);
        btnDislike.setAlpha(hasDisliked ? 0.5f : 1.0f);

        // Configurar listeners solo si no es el creador
        btnLike.setOnClickListener(v -> handleVoteClick(reviewId, resena, true, btnLike, btnDislike));
        btnDislike.setOnClickListener(v -> handleVoteClick(reviewId, resena, false, btnLike, btnDislike));
    }


    private void handleVoteClick(String reviewId, Map<String, Object> resena, boolean isLike,
                                 ImageButton btnLike, ImageButton btnDislike) {
        //evitar múltiples clicks
        if (System.currentTimeMillis() - lastClickTime < 500) {
            return;
        }
        lastClickTime = System.currentTimeMillis();

        if (currentUser == null || reviewId.isEmpty()) {
            Toast.makeText(getContext(), "Debes iniciar sesión para votar", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        List<String> likedBy = resena.containsKey("likedBy") ? (List<String>) resena.get("likedBy") : new ArrayList<>();
        List<String> dislikedBy = resena.containsKey("dislikedBy") ? (List<String>) resena.get("dislikedBy") : new ArrayList<>();

        boolean hadLiked = likedBy.contains(userId);
        boolean hadDisliked = dislikedBy.contains(userId);

        Map<String, Object> updates = new HashMap<>();

        if (isLike) {
            if (hadLiked) {
                // Quitar like si ya estaba activado
                updates.put("likes", FieldValue.increment(-1));
                updates.put("likedBy", FieldValue.arrayRemove(userId));
            } else {
                // Dar like
                if (hadDisliked) {
                    updates.put("dislikes", FieldValue.increment(-1));
                    updates.put("dislikedBy", FieldValue.arrayRemove(userId));
                }
                updates.put("likes", FieldValue.increment(1));
                updates.put("likedBy", FieldValue.arrayUnion(userId));
            }
        } else {
            if (hadDisliked) {
                // Quitar dislike si ya estaba activado
                updates.put("dislikes", FieldValue.increment(-1));
                updates.put("dislikedBy", FieldValue.arrayRemove(userId));
            } else {
                // Dar dislike
                if (hadLiked) {
                    updates.put("likes", FieldValue.increment(-1));
                    updates.put("likedBy", FieldValue.arrayRemove(userId));
                }
                updates.put("dislikes", FieldValue.increment(1));
                updates.put("dislikedBy", FieldValue.arrayUnion(userId));
            }
        }

        db.collection("resenas").document(reviewId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    updateLocalResenaData(resena, isLike, hadLiked, hadDisliked);


                    boolean nowLiked = resena.containsKey("likedBy") && ((List<String>) resena.get("likedBy")).contains(userId);
                    boolean nowDisliked = resena.containsKey("dislikedBy") && ((List<String>) resena.get("dislikedBy")).contains(userId);

                    btnLike.setAlpha(nowLiked ? 0.5f : 1.0f);
                    btnDislike.setAlpha(nowDisliked ? 0.5f : 1.0f);

                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al registrar voto", Toast.LENGTH_SHORT).show();
                    Log.e("ResenaAdapter", "Error al actualizar voto", e);
                });
    }

    private void updateLocalResenaData(Map<String, Object> resena, boolean isLike,
                                       boolean hadLiked, boolean hadDisliked) {
        int likes = getNumberAsInt(resena.get("likes"));
        int dislikes = getNumberAsInt(resena.get("dislikes"));

        List<String> likedBy = (List<String>) resena.get("likedBy");
        List<String> dislikedBy = (List<String>) resena.get("dislikedBy");
        String userId = currentUser.getUid();

        if (isLike) {
            if (hadLiked) {
                // Quitar like
                likes--;
                likedBy.remove(userId);
            } else {
                // Dar like
                if (hadDisliked) {
                    dislikes--;
                    dislikedBy.remove(userId);
                }
                likes++;
                if (!likedBy.contains(userId)) {
                    likedBy.add(userId);
                }
            }
        } else {
            if (hadDisliked) {
                // Quitar dislike
                dislikes--;
                dislikedBy.remove(userId);
            } else {
                // Dar dislike
                if (hadLiked) {
                    likes--;
                    likedBy.remove(userId);
                }
                dislikes++;
                if (!dislikedBy.contains(userId)) {
                    dislikedBy.add(userId);
                }
            }
        }

        resena.put("likes", likes);
        resena.put("dislikes", dislikes);
    }

    private String obtenerIdResena(Map<String, Object> resena) {
        if (resena.containsKey("id") && resena.get("id") != null) {
            return resena.get("id").toString();
        }
        return "";
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