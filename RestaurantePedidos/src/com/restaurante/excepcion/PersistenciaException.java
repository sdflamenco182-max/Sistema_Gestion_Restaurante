package com.restaurante.excepcion;

// Error al leer o guardar datos en la base de datos
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
