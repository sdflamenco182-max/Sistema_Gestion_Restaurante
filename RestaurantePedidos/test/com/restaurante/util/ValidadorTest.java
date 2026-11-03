package com.restaurante.util;

import com.restaurante.excepcion.ValidacionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorTest {

    @Test
    void requeridoRechazaVacioYEspacios() {
        assertThrows(ValidacionException.class, () -> Validador.requerido("Nombre", "   "));
        assertThrows(ValidacionException.class, () -> Validador.requerido("Nombre", null));
        assertEquals("Ana", Validador.requerido("Nombre", "  Ana "));
    }

    @Test
    void enteroPositivoRechazaCeroNegativosYNoEnteros() {
        assertEquals(3, Validador.enteroPositivo("Cantidad", " 3 "));
        assertThrows(ValidacionException.class, () -> Validador.enteroPositivo("Cantidad", "0"));
        assertThrows(ValidacionException.class, () -> Validador.enteroPositivo("Cantidad", "-2"));
        assertThrows(ValidacionException.class, () -> Validador.enteroPositivo("Cantidad", "2.5"));
        assertThrows(ValidacionException.class, () -> Validador.enteroPositivo("Cantidad", "abc"));
    }

    @Test
    void montoAceptaCeroYRechazaNegativosYDemasiadosDecimales() {
        assertEquals(new BigDecimal("0.00"), Validador.montoNoNegativo("Precio", "0"));
        assertEquals(new BigDecimal("3.50"), Validador.montoNoNegativo("Precio", "3.5"));
        assertThrows(ValidacionException.class, () -> Validador.montoNoNegativo("Precio", "-1"));
        assertThrows(ValidacionException.class, () -> Validador.montoNoNegativo("Precio", "1.999"));
        assertThrows(ValidacionException.class, () -> Validador.montoNoNegativo("Precio", "diez"));
    }

    @Test
    void codigoRechazaEspaciosYSimbolos() {
        assertEquals("P001", Validador.codigo("Código", "P001"));
        assertThrows(ValidacionException.class, () -> Validador.codigo("Código", "P 001"));
        assertThrows(ValidacionException.class, () -> Validador.codigo("Código", "P#1"));
    }

    @Test
    void correoYTelefonoOpcionales() {
        assertEquals("", Validador.correoOpcional(" "));
        assertEquals("a@b.com", Validador.correoOpcional("a@b.com"));
        assertThrows(ValidacionException.class, () -> Validador.correoOpcional("sin-arroba"));
        assertEquals("7000-1111", Validador.telefonoOpcional("7000-1111"));
        assertThrows(ValidacionException.class, () -> Validador.telefonoOpcional("12ab"));
    }

    @Test
    void fechaDebeTenerFormatoIso() {
        assertEquals(2026, Validador.fecha("Fecha", "2026-11-15").getYear());
        assertThrows(ValidacionException.class, () -> Validador.fecha("Fecha", "15/11/2026"));
    }
}
