package com.restaurante.modelo;

import com.restaurante.excepcion.ValidacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoTest {

    private Producto pupusa;
    private Producto refresco;
    private Pedido pedido;

    @BeforeEach
    void preparar() {
        pupusa = new Producto("P001", "Pupusa", "Comida", "", new BigDecimal("1.25"), true);
        refresco = new Producto("P005", "Refresco", "Bebidas", "", new BigDecimal("1.75"), true);
        pedido = new Pedido("PED-0001", LocalDateTime.now(), null, null);
    }

    @Test
    void totalEsSumaDeSubtotalesYSeRecalculaTrasCadaCambio() {
        pedido.agregarProducto(pupusa, 3);
        pedido.agregarProducto(refresco, 2);
        assertEquals(new BigDecimal("7.25"), pedido.getTotal());

        pedido.modificarCantidad("P001", 4);
        assertEquals(new BigDecimal("8.50"), pedido.getTotal());

        pedido.quitarProducto("P005");
        assertEquals(new BigDecimal("5.00"), pedido.getTotal());
        assertTrue(pedido.totalConsistente());
    }

    @Test
    void cambioDePrecioDelProductoNoAlteraElDetalleYaRegistrado() {
        pedido.agregarProducto(pupusa, 2);
        pupusa.setPrecio(new BigDecimal("9.99"));
        assertEquals(new BigDecimal("2.50"), pedido.calcularTotal());
    }

    @Test
    void agregarElMismoProductoSumaCantidades() {
        pedido.agregarProducto(pupusa, 1);
        pedido.agregarProducto(pupusa, 2);
        assertEquals(1, pedido.getDetalles().size());
        assertEquals(3, pedido.getDetalles().get(0).getCantidad());
    }

    @Test
    void rechazaCantidadesInvalidasYProductosNoDisponibles() {
        assertThrows(ValidacionException.class, () -> pedido.agregarProducto(pupusa, 0));
        assertThrows(ValidacionException.class, () -> pedido.agregarProducto(pupusa, -1));
        assertThrows(ValidacionException.class, () -> pedido.agregarProducto(null, 1));
        pupusa.setDisponible(false);
        assertThrows(ValidacionException.class, () -> pedido.agregarProducto(pupusa, 1));
    }

    @Test
    void pedidoSinProductosNoPuedeCerrarseNiPrepararse() {
        assertThrows(ValidacionException.class, () -> pedido.cambiarEstado(EstadoPedido.EN_PREPARACION));
        pedido.cambiarEstado(EstadoPedido.CANCELADO); // cancelar sí se permite
        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
    }

    @Test
    void pedidoCerradoNoAdmiteCambiosEnElDetalle() {
        pedido.agregarProducto(pupusa, 1);
        pedido.cambiarEstado(EstadoPedido.EN_PREPARACION);
        assertThrows(ValidacionException.class, () -> pedido.agregarProducto(refresco, 1));
        assertThrows(ValidacionException.class, () -> pedido.modificarCantidad("P001", 2));
        assertThrows(ValidacionException.class, () -> pedido.quitarProducto("P001"));
        pedido.cambiarEstado(EstadoPedido.COMPLETADO);
        assertThrows(ValidacionException.class, () -> pedido.cambiarEstado(EstadoPedido.ABIERTO));
    }

    @Test
    void detectaInconsistenciaEntreTotalAlmacenadoYDetalle() {
        Pedido leido = Pedido.restaurar("PED-0002", LocalDateTime.now(), null, null, EstadoPedido.ABIERTO,
                java.util.List.of(new DetallePedido("P001", "Pupusa", new BigDecimal("1.25"), 2)),
                new BigDecimal("99.00"));
        assertTrue(!leido.totalConsistente());
        assertEquals(new BigDecimal("2.50"), leido.calcularTotal());
    }
}
