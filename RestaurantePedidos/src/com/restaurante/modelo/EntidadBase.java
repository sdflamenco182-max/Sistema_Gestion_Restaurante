package com.restaurante.modelo;

public abstract class EntidadBase {
    private final String codigo;

    protected EntidadBase(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
