package com.restaurante.excepcion;

// Error cuando un dato o una operación no cumple una regla del sistema
public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
