package com.example.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResenaRecyclerAdapter extends RecyclerView.Adapter<ResenaRecyclerAdapter.ResenaViewHolder> {
    private final Context context;
    private final List<Map<String, Object>> resenas;
    private final FirebaseFirestore db;
    private final FirebaseUser currentUser;
    private long lastClickTime = 0;

    public ResenaRecyclerAdapter(List<Map<String, Object>> resenas, Context context) {
        this.resenas = resenas;
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.context = context; // Inicializa el contexto
    }


    @NonNull
    @Override
    public ResenaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resena_main, parent, false);
        return new ResenaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResenaViewHolder holder, int position) {

        Map<String, Object> resena = resenas.get(position);

        // Configurar vistas
        holder.tvProfesor.setText(resena.containsKey("nombre_profesor") ?
                resena.get("nombre_profesor").toString() : "Profesor desconocido");

        holder.tvMateria.setText(resena.containsKey("materia") ?
                resena.get("materia").toString() : "");

        float calificacion = resena.containsKey("calificacion") ?
                ((Number)resena.get("calificacion")).floatValue() : 0f;
        holder.tvCalificacion.setText(String.format("⭐ %.1f/5", calificacion));

        holder.tvComentario.setText(resena.containsKey("comentario") ?
                String.format("\"%s\"", resena.get("comentario").toString()) : "");

        holder.btnLike.setOnClickListener(null);
        holder.btnDislike.setOnClickListener(null);

        holder.btnDislike.setEnabled(true);
        holder.btnLike.setEnabled(true);

        String idResena = obtenerIdResena(resena);
        String idUsuarioResena = resena.get("id_usuario").toString();
        boolean esCreador = currentUser != null && currentUser.getUid().equals(idUsuarioResena);

        int likes = getNumberAsInt(resena.get("likes"));
        int dislikes = getNumberAsInt(resena.get("dislikes"));

        holder.textViewLikes.setText(String.valueOf(likes));
        holder.textViewDislikes.setText(String.valueOf(dislikes));

        if (esCreador) {
            holder.btnLike.setEnabled(false);
            holder.btnDislike.setEnabled(false);
            holder.btnLike.setAlpha(0.3f);
            holder.btnDislike.setAlpha(0.3f);
        } else {
            setupVoteButtons(resena, holder.btnLike, holder.btnDislike, idResena);
        }
    }

    @Override
    public int getItemCount() {
        return resenas.size();
    }

    static class ResenaViewHolder extends RecyclerView.ViewHolder {
        TextView tvProfesor, tvMateria, tvCalificacion, tvComentario;
        TextView textViewLikes, textViewDislikes;
        ImageButton btnLike, btnDislike;

        public ResenaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProfesor = itemView.findViewById(R.id.tvProfesorMain);
            tvMateria = itemView.findViewById(R.id.tvMateriaMain);
            tvCalificacion = itemView.findViewById(R.id.tvCalificacion);
            tvComentario = itemView.findViewById(R.id.tvComentario);
            textViewLikes = itemView.findViewById(R.id.textViewLikes);
            textViewDislikes = itemView.findViewById(R.id.textViewDislikes);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
        }
    }

    private void setupVoteButtons(Map<String, Object> resena, ImageButton btnLike,
                                  ImageButton btnDislike, String reviewId) {
        if (currentUser == null) {
            btnLike.setEnabled(false);
            btnDislike.setEnabled(false);
            return;
        }

        if (resena == null) {
            btnLike.setEnabled(false);
            btnDislike.setEnabled(false);
            return;
        }

        btnLike.setOnClickListener(null);
        btnDislike.setOnClickListener(null);

        List<String> likedBy = (List<String>) resena.get("likedBy");
        List<String> dislikedBy = (List<String>) resena.get("dislikedBy");

        boolean hasLiked = likedBy.contains(currentUser.getUid());
        boolean hasDisliked = dislikedBy.contains(currentUser.getUid());

        btnLike.setAlpha(hasLiked ? 0.5f : 1.0f);
        btnDislike.setAlpha(hasDisliked ? 0.5f : 1.0f);

        // Configurar nuevos listeners
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
            Toast.makeText(context, "Debes iniciar sesión para votar", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, "Error al registrar voto", Toast.LENGTH_SHORT).show();
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