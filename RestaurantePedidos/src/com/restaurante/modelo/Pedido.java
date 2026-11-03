package com.restaurante.modelo;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido extends EntidadBase {
    private final LocalDateTime fecha;
    private final String codigoCliente;
    private final String codigoMesa;
    private EstadoPedido estado;
    private final List<DetallePedido> detalles = new ArrayList<>();
    private BigDecimal total = Dinero.CERO;

    public Pedido(String codigo, LocalDateTime fecha, String codigoCliente, String codigoMesa) {
        super(codigo);
        this.fecha = fecha;
        this.codigoCliente = codigoCliente;
        this.codigoMesa = codigoMesa;
        this.estado = EstadoPedido.ABIERTO;
    }

    public static Pedido restaurar(String codigo, LocalDateTime fecha, String codigoCliente, String codigoMesa,
                                   EstadoPedido estado, List<DetallePedido> detalles, BigDecimal totalAlmacenado) {
        Pedido pedido = new Pedido(codigo, fecha, codigoCliente, codigoMesa);
        pedido.estado = estado;
        pedido.detalles.addAll(detalles);
        pedido.total = totalAlmacenado;
        return pedido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public String getCodigoMesa() {
        return codigoMesa;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public void agregarProducto(Producto producto, int cantidad) {
        exigirModificable();
        exigirProductoDisponible(producto);
        exigirCantidadValida(cantidad);
        DetallePedido existente = buscarDetalle(producto.getCodigo());
        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
        } else {
            detalles.add(new DetallePedido(producto.getCodigo(), producto.getNombre(), producto.getPrecio(), cantidad));
        }
        recalcularTotal();
    }

    public void modificarCantidad(String codigoProducto, int nuevaCantidad) {
        exigirModificable();
        exigirCantidadValida(nuevaCantidad);
        DetallePedido detalle = buscarDetalleObligatorio(codigoProducto);
        detalle.setCantidad(nuevaCantidad);
        recalcularTotal();
    }

    public void quitarProducto(String codigoProducto) {
        exigirModificable();
        DetallePedido detalle = buscarDetalleObligatorio(codigoProducto);
        detalles.remove(detalle);
        recalcularTotal();
    }

    public void cambiarEstado(EstadoPedido destino) {
        exigirTransicionPermitida(destino);
        exigirProductosParaAvanzar(destino);
        estado = destino;
    }

    public BigDecimal calcularTotal() {
        BigDecimal suma = Dinero.CERO;
        for (DetallePedido detalle : detalles) {
            suma = suma.add(detalle.subtotal());
        }
        return Dinero.redondear(suma);
    }

    public void recalcularTotal() {
        total = calcularTotal();
    }

    public boolean totalConsistente() {
        return calcularTotal().compareTo(total) == 0;
    }

    private void exigirTransicionPermitida(EstadoPedido destino) {
        if (destino == null || !estado.puedePasarA(destino)) {
            String textoDestino = "(sin estado)";
            if (destino != null) {
                textoDestino = destino.toString();
            }
            throw new ValidacionException("No es posible pasar un pedido de \"" + estado + "\" a \""
                    + textoDestino + "\".");
        }
    }

    private void exigirProductosParaAvanzar(EstadoPedido destino) {
        boolean avanza = destino == EstadoPedido.EN_PREPARACION || destino == EstadoPedido.COMPLETADO;
        if (avanza && detalles.isEmpty()) {
            throw new ValidacionException("Un pedido sin productos no puede enviarse a preparación ni completarse. "
                    + "Agregue productos o cancele el pedido.");
        }
    }

    private DetallePedido buscarDetalle(String codigoProducto) {
        for (DetallePedido detalle : detalles) {
            if (detalle.getCodigoProducto().equals(codigoProducto)) {
                return detalle;
            }
        }
        return null;
    }

    private DetallePedido buscarDetalleObligatorio(String codigoProducto) {
        DetallePedido detalle = buscarDetalle(codigoProducto);
        if (detalle == null) {
            throw new ValidacionException("El producto no forma parte de este pedido.");
        }
        return detalle;
    }

    private void exigirProductoDisponible(Producto producto) {
        if (producto == null) {
            throw new ValidacionException("El producto seleccionado no existe.");
        }
        if (!producto.isDisponible()) {
            throw new ValidacionException("El producto \"" + producto.getNombre() + "\" no está disponible.");
        }
    }

    private void exigirModificable() {
        if (!estado.permiteModificarDetalle()) {
            throw new ValidacionException("El pedido está \"" + estado
                    + "\" y ya no admite cambios en sus productos.");
        }
    }

    private static void exigirCantidadValida(int cantidad) {
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad debe ser un entero mayor que cero.");
        }
    }
}
