package com.restaurante.modelo;

// Estados por los que pasa un pedido
public enum EstadoPedido {
    ABIERTO("Abierto"),
    EN_PREPARACION("En preparación"),
    COMPLETADO("Completado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    // Un pedido activo todavía no se completa ni se cancela
    public boolean esActivo() {
        return this == ABIERTO || this == EN_PREPARACION;
    }

    // Solo un pedido abierto deja cambiar sus productos
    public boolean permiteModificarDetalle() {
        return this == ABIERTO;
    }

    // Dice si el cambio de estado está permitido (abierto, preparación y luego completado)
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
