package com.restaurante.modelo;

public class Usuario {
    private final String nombreUsuario;
    private final String nombreCompleto;
    private final String passwordHash;
    private final boolean activo;

    public Usuario(String nombreUsuario, String nombreCompleto, String passwordHash, boolean activo) {
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.passwordHash = passwordHash;
        this.activo = activo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }
}
