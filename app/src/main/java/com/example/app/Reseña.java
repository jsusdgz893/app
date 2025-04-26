package com.example.app;


public class Reseña {
    private double calificacion;
    private String comentario;
    private String idProfesor;
    private String idUsuario;
    private String materia;

    // Constructor
    public Reseña(double calificacion, String comentario, String idProfesor, String idUsuario, String materia) {
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.idProfesor = idProfesor;
        this.idUsuario = idUsuario;
        this.materia = materia;
    }

    // Getters
    public double getCalificacion() {
        return calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public String getIdProfesor() {
        return idProfesor;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getMateria() {
        return materia;
    }

    // Setters
    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public void setIdProfesor(String idProfesor) {
        this.idProfesor = idProfesor;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    // Método para mostrar los datos de la reseña
    @Override
    public String toString() {
        return "Reseña{" +
                "calificacion=" + calificacion +
                ", comentario='" + comentario + '\'' +
                ", idProfesor='" + idProfesor + '\'' +
                ", idUsuario='" + idUsuario + '\'' +
                ", materia='" + materia + '\'' +
                '}';
    }
}
