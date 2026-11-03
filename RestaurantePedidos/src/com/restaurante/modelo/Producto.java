package com.restaurante.modelo;

import com.restaurante.util.Dinero;

import java.math.BigDecimal;

public class Producto extends EntidadBase {
    private String nombre;
    private String categoria;
    private String descripcion;
    private BigDecimal precio;
    private boolean disponible;

    public Producto(String codigo, String nombre, String categoria, String descripcion,
                    BigDecimal precio, boolean disponible) {
        super(codigo);
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precio = precio;
        this.disponible = disponible;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return getCodigo() + " - " + nombre + " (" + Dinero.formato(precio) + ")";
    }
}
