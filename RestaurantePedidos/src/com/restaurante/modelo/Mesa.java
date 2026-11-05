package com.restaurante.modelo;

// Mesa del restaurante con su número, capacidad y estado
public class Mesa extends EntidadBase {
    private int numero;
    private int capacidad;
    private EstadoMesa estado;

    public Mesa(String codigo, int numero, int capacidad, EstadoMesa estado) {
        super(codigo);
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    // Getters y setters de los datos de la mesa
    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }

    // Dice si la mesa está libre
    public boolean estaDisponible() {
        return estado == EstadoMesa.DISPONIBLE;
    }

    @Override
    public String toString() {
        return "Mesa " + numero + " (" + getCodigo() + ", cap. " + capacidad + ")";
    }
}
