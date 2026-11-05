package com.restaurante.modelo;

import com.restaurante.util.Dinero;

import java.math.BigDecimal;

// Una línea del pedido: un producto con su precio y la cantidad pedida
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

    // Getters y setter de los datos de la línea
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

    // Precio unitario por cantidad
    public BigDecimal subtotal() {
        return Dinero.redondear(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
    }
}
