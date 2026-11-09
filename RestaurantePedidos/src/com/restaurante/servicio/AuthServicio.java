package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Usuario;
import com.restaurante.persistencia.UsuarioRepositorio;
import com.restaurante.util.PasswordHasher;

import java.util.Arrays;

// Maneja el inicio y cierre de sesión de los usuarios
public class AuthServicio {

    private static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos.";
    private static String hashFicticio;

    private final UsuarioRepositorio repositorio = new UsuarioRepositorio();

    // Valida usuario y contraseña; si están mal lanza ValidacionException con un mensaje general
    public Usuario iniciarSesion(String nombreUsuario, char[] clave) {
        try {
            validarDatosEscritos(nombreUsuario, clave);
            Usuario usuario = repositorio.buscarPorNombreUsuario(nombreUsuario.trim());
            String hash;
            if (usuario != null) {
                hash = usuario.getPasswordHash();
            } else {
                hash = hashFicticio();
            }
            // Compara igual aunque el usuario no exista, para no dar pistas
            boolean coincide = PasswordHasher.verificar(clave, hash);
            if (usuario == null || !usuario.isActivo() || !coincide) {
                throw new ValidacionException(MENSAJE_CREDENCIALES);
            }
            Sesion.iniciar(usuario);
            return usuario;
        } finally {
            // Borra la contraseña de la memoria
            if (clave != null) {
                Arrays.fill(clave, '\0');
            }
        }
    }

    // Cierra la sesión del usuario actual
    public void cerrarSesion() {
        Sesion.cerrar();
    }

    // Revisa que el usuario y la contraseña no vengan vacíos
    private void validarDatosEscritos(String nombreUsuario, char[] clave) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new ValidacionException("Ingrese el nombre de usuario.");
        }
        if (clave == null || clave.length == 0) {
            throw new ValidacionException("Ingrese la contraseña.");
        }
    }

    // Crea una sola vez un hash de relleno para usarlo cuando el usuario no existe
    private static synchronized String hashFicticio() {
        if (hashFicticio == null) {
            hashFicticio = PasswordHasher.hash("relleno".toCharArray());
        }
        return hashFicticio;
    }
}
