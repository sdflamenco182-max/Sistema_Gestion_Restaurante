package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Usuario;

// Guarda quién tiene la sesión iniciada en el programa
public final class Sesion {

    // Usuario que inició sesión (null si no hay nadie)
    private static Usuario usuarioActual;

    private Sesion() {
    }

    // Guarda el usuario que acaba de entrar
    public static synchronized void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    // Quita al usuario actual
    public static synchronized void cerrar() {
        usuarioActual = null;
    }

    // Dice si hay una sesión iniciada
    public static synchronized boolean estaActiva() {
        return usuarioActual != null;
    }

    // Devuelve el usuario actual
    public static synchronized Usuario usuario() {
        return usuarioActual;
    }

    // Lanza ValidacionException si nadie ha iniciado sesión
    public static void exigirAutenticado() {
        if (!estaActiva()) {
            throw new ValidacionException("Debe iniciar sesión para realizar esta operación.");
        }
    }
}
