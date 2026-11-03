package com.restaurante.modelo;

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
