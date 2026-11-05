package com.restaurante.util;

import com.restaurante.excepcion.ValidacionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

// Validaciones de los datos que escribe el usuario en los formularios
public final class Validador {
    private static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9_-]{1,20}$");
    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\- ]{7,20}$");

    private Validador() {
    }

    // El campo no puede ir vacío; devuelve el texto sin espacios de los lados
    public static String requerido(String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
        }
        return valor.trim();
    }

    // El código solo admite letras, números, guion y guion bajo (máximo 20)
    public static String codigo(String campo, String valor) {
        String limpio = requerido(campo, valor);
        if (!CODIGO.matcher(limpio).matches()) {
            throw new ValidacionException("El campo \"" + campo
                    + "\" solo admite letras, números, guion y guion bajo (máximo 20 caracteres, sin espacios).");
        }
        return limpio;
    }

    // Convierte el texto a entero y exige que sea mayor que cero
    public static int enteroPositivo(String campo, String valor) {
        String limpio = requerido(campo, valor);
        int numero;
        try {
            numero = Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            throw new ValidacionException("El campo \"" + campo + "\" debe ser un número entero (sin decimales ni letras).");
        }
        if (numero <= 0) {
            throw new ValidacionException("El campo \"" + campo + "\" debe ser mayor que cero.");
        }
        return numero;
    }

    // Convierte el texto a monto: no negativo, máximo dos decimales y un tope de tamaño
    public static BigDecimal montoNoNegativo(String campo, String valor) {
        String limpio = requerido(campo, valor);
        BigDecimal monto = convertirAMonto(campo, limpio);
        if (monto.signum() < 0) {
            throw new ValidacionException("El campo \"" + campo + "\" no puede ser negativo.");
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new ValidacionException("El campo \"" + campo + "\" admite como máximo dos decimales.");
        }
        if (monto.precision() - monto.scale() > 8) {
            throw new ValidacionException("El campo \"" + campo + "\" excede el valor máximo permitido.");
        }
        return Dinero.redondear(monto);
    }

    // Pasa el texto a BigDecimal; error si no es un número
    private static BigDecimal convertirAMonto(String campo, String texto) {
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new ValidacionException("El campo \"" + campo + "\" debe ser un valor numérico (ejemplo: 3.50).");
        }
    }

    // El correo puede ir vacío, pero si se escribe debe tener formato válido
    public static String correoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String limpio = valor.trim();
        if (!CORREO.matcher(limpio).matches()) {
            throw new ValidacionException("El correo electrónico no tiene un formato válido (ejemplo: nombre@dominio.com).");
        }
        return limpio;
    }

    // El teléfono puede ir vacío, pero si se escribe debe tener de 7 a 20 caracteres válidos
    public static String telefonoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String limpio = valor.trim();
        if (!TELEFONO.matcher(limpio).matches()) {
            throw new ValidacionException("El teléfono no es válido (use de 7 a 20 dígitos; se permiten +, -, espacios y paréntesis).");
        }
        return limpio;
    }

    // Convierte el texto a fecha con formato AAAA-MM-DD
    public static LocalDate fecha(String campo, String valor) {
        String limpio = requerido(campo, valor);
        try {
            return LocalDate.parse(limpio);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("El campo \"" + campo + "\" debe tener formato AAAA-MM-DD (ejemplo: 2026-11-15).");
        }
    }
}
