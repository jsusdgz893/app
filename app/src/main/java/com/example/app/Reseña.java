package com.example.app;

import java.util.ArrayList;
import java.util.List;

public class Reseña {
    private double calificacion;
    private String comentario;
    private String profesorNombre;
    private String idProfesor;
    private String materia;
    private String idResenia;
    private List<String> likedBy;
    private List<String> dislikedBy;
    private String idUsuario;

    public Reseña(double calificacion, String comentario, String profesorNombre,
                  String idProfesor, String materia, String idResenia,
                  List<String> likedBy, List<String> dislikedBy,
                  String idUsuario) {
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.profesorNombre = profesorNombre;
        this.idProfesor = idProfesor;
        this.materia = materia;
        this.idResenia = idResenia;
        this.likedBy = likedBy != null ? likedBy : new ArrayList<>();
        this.dislikedBy = dislikedBy != null ? dislikedBy : new ArrayList<>();
        this.idUsuario = idUsuario;
    }

    // Getters y setters existentes
    public double getCalificacion() {
        return calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public String getProfesorNombre() {
        return profesorNombre;
    }

    public String getIdProfesor() {
        return idProfesor;
    }

    public String getMateria() {
        return materia;
    }

    public String getIdResenia() {
        return idResenia;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public void setProfesorNombre(String profesorNombre) {
        this.profesorNombre = profesorNombre;
    }

    public void setIdProfesor(String idProfesor) {
        this.idProfesor = idProfesor;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public void setIdResenia(String idResenia) {
        this.idResenia = idResenia;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    // Nuevos métodos para likes/dislikes

    public List<String> getLikedBy() {
        return likedBy != null ? likedBy : new ArrayList<>();
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy != null ? likedBy : new ArrayList<>();
    }

    public List<String> getDislikedBy() {
        return dislikedBy != null ? dislikedBy : new ArrayList<>();
    }

    public void setDislikedBy(List<String> dislikedBy) {
        this.dislikedBy = dislikedBy != null ? dislikedBy : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Reseña{" +
                "calificacion=" + calificacion +
                ", comentario='" + comentario + '\'' +
                ", profesorNombre='" + profesorNombre + '\'' +
                ", idProfesor='" + idProfesor + '\'' +
                ", materia='" + materia + '\'' +
                ", idResenia='" + idResenia + '\'' +
                ", likedBy=" + likedBy +
                ", dislikedBy=" + dislikedBy +
                ", idUsuario='" + idUsuario + '\'' +
                '}';
    }
}