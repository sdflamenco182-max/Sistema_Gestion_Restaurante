package com.restaurante.modelo;

// Clase base de las entidades que se identifican por un código
public abstract class EntidadBase {
    private final String codigo;

    protected EntidadBase(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
