package com.restaurante.modelo;

public class Cliente extends EntidadBase {
    private String nombre;
    private String telefono;
    private String correo;
    private boolean activo;

    public Cliente(String codigo, String nombre, String telefono, String correo, boolean activo) {
        super(codigo);
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return getCodigo() + " - " + nombre;
    }
}
