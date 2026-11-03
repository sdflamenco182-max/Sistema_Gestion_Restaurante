package com.restaurante.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void verificaClaveCorrectaEIncorrecta() {
        String hash = PasswordHasher.hash("Secreta1".toCharArray());
        assertTrue(PasswordHasher.verificar("Secreta1".toCharArray(), hash));
        assertFalse(PasswordHasher.verificar("secreta1".toCharArray(), hash));
    }

    @Test
    void noGuardaTextoPlanoYUsaSalDistinta() {
        String h1 = PasswordHasher.hash("Secreta1".toCharArray());
        String h2 = PasswordHasher.hash("Secreta1".toCharArray());
        assertFalse(h1.contains("Secreta1"));
        assertNotEquals(h1, h2);
    }

    @Test
    void hashMalFormadoNoVerifica() {
        assertFalse(PasswordHasher.verificar("x".toCharArray(), "texto-plano"));
        assertFalse(PasswordHasher.verificar("x".toCharArray(), null));
    }
}
