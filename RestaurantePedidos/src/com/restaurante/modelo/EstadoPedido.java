package com.restaurante.modelo;

public enum EstadoPedido {
    ABIERTO("Abierto"),
    EN_PREPARACION("En preparación"),
    COMPLETADO("Completado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public boolean esActivo() {
        return this == ABIERTO || this == EN_PREPARACION;
    }

    public boolean permiteModificarDetalle() {
        return this == ABIERTO;
    }

    public boolean puedePasarA(EstadoPedido destino) {
        if (this == ABIERTO) {
            return destino == EN_PREPARACION || destino == CANCELADO;
        }
        if (this == EN_PREPARACION) {
            return destino == COMPLETADO || destino == CANCELADO;
        }
        return false;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
