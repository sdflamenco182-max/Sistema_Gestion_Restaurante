package com.restaurante.modelo;

import com.restaurante.util.Dinero;

import java.math.BigDecimal;

public class DetallePedido {
    private final String codigoProducto;
    private final String nombreProducto;
    private final BigDecimal precioUnitario;
    private int cantidad;

    public DetallePedido(String codigoProducto, String nombreProducto, BigDecimal precioUnitario, int cantidad) {
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = Dinero.redondear(precioUnitario);
        this.cantidad = cantidad;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal subtotal() {
        return Dinero.redondear(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
    }
}
