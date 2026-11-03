package com.restaurante.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Dinero {
    public static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    private Dinero() {
    }

    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static String formato(BigDecimal valor) {
        return "$" + redondear(valor).toPlainString();
    }
}
