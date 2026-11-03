package com.restaurante.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final String PREFIJO = "pbkdf2_sha256";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(char[] clave) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] resultado = derivar(clave, sal, ITERACIONES, BITS_HASH);
        String salTexto = Base64.getEncoder().encodeToString(sal);
        String hashTexto = Base64.getEncoder().encodeToString(resultado);
        return PREFIJO + "$" + ITERACIONES + "$" + salTexto + "$" + hashTexto;
    }

    public static boolean verificar(char[] clave, String almacenado) {
        if (almacenado == null) {
            return false;
        }
        try {
            String[] partes = almacenado.split("\\$");
            if (partes.length != 4 || !PREFIJO.equals(partes[0])) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(clave, sal, iteraciones, esperado.length * 8);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] clave, byte[] sal, int iteraciones, int bits) {
        PBEKeySpec especificacion = new PBEKeySpec(clave, sal, iteraciones, bits);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible calcular el hash de la contraseña", e);
        } finally {
            especificacion.clearPassword();
        }
    }
}
