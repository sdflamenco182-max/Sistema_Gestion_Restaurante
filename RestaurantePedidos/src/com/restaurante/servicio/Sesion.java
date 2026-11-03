package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Usuario;

public final class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
    }

    public static synchronized void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static synchronized void cerrar() {
        usuarioActual = null;
    }

    public static synchronized boolean estaActiva() {
        return usuarioActual != null;
    }

    public static synchronized Usuario usuario() {
        return usuarioActual;
    }

    public static void exigirAutenticado() {
        if (!estaActiva()) {
            throw new ValidacionException("Debe iniciar sesión para realizar esta operación.");
        }
    }
}
