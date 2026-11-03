package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Usuario;
import com.restaurante.persistencia.UsuarioRepositorio;
import com.restaurante.util.PasswordHasher;

import java.util.Arrays;

public class AuthServicio {

    private static final String MENSAJE_CREDENCIALES = "Usuario o contraseña incorrectos.";
    private static String hashFicticio;

    private final UsuarioRepositorio repositorio = new UsuarioRepositorio();

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
            boolean coincide = PasswordHasher.verificar(clave, hash);
            if (usuario == null || !usuario.isActivo() || !coincide) {
                throw new ValidacionException(MENSAJE_CREDENCIALES);
            }
            Sesion.iniciar(usuario);
            return usuario;
        } finally {
            if (clave != null) {
                Arrays.fill(clave, '\0');
            }
        }
    }

    public void cerrarSesion() {
        Sesion.cerrar();
    }

    private void validarDatosEscritos(String nombreUsuario, char[] clave) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new ValidacionException("Ingrese el nombre de usuario.");
        }
        if (clave == null || clave.length == 0) {
            throw new ValidacionException("Ingrese la contraseña.");
        }
    }

    private static synchronized String hashFicticio() {
        if (hashFicticio == null) {
            hashFicticio = PasswordHasher.hash("relleno".toCharArray());
        }
        return hashFicticio;
    }
}
