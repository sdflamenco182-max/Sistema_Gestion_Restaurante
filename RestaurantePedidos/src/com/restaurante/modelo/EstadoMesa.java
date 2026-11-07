package com.restaurante.modelo;

// Estados en que puede estar una mesa
public enum EstadoMesa {
    DISPONIBLE("Disponible"),
    OCUPADA("Ocupada");

    private final String etiqueta;

    EstadoMesa(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
