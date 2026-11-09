package com.restaurante.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Funciones para manejar los montos de dinero con dos decimales
public final class Dinero {
    public static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    private Dinero() {
    }

    // Redondea el valor a dos decimales
    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    // Devuelve el monto como texto con el signo de dólar, por ejemplo $3.50
    public static String formato(BigDecimal valor) {
        return "$" + redondear(valor).toPlainString();
    }
}
