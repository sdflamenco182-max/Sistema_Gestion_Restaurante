package com.restaurante.modelo;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.util.Dinero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Pedido de un cliente en una mesa, con sus productos, su estado y su total
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

    // Rearma un pedido guardado, con su estado y total tal como estaban
    public static Pedido restaurar(String codigo, LocalDateTime fecha, String codigoCliente, String codigoMesa,
                                   EstadoPedido estado, List<DetallePedido> detalles, BigDecimal totalAlmacenado) {
        Pedido pedido = new Pedido(codigo, fecha, codigoCliente, codigoMesa);
        pedido.estado = estado;
        pedido.detalles.addAll(detalles);
        pedido.total = totalAlmacenado;
        return pedido;
    }

    // Getters de los datos del pedido (la lista de detalles no se puede modificar desde fuera)
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

    // Agrega un producto; si ya estaba en el pedido, le suma la cantidad
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

    // Cambia la cantidad de un producto del pedido y recalcula el total
    public void modificarCantidad(String codigoProducto, int nuevaCantidad) {
        exigirModificable();
        exigirCantidadValida(nuevaCantidad);
        DetallePedido detalle = buscarDetalleObligatorio(codigoProducto);
        detalle.setCantidad(nuevaCantidad);
        recalcularTotal();
    }

    // Quita un producto del pedido y recalcula el total
    public void quitarProducto(String codigoProducto) {
        exigirModificable();
        DetallePedido detalle = buscarDetalleObligatorio(codigoProducto);
        detalles.remove(detalle);
        recalcularTotal();
    }

    // Cambia el estado si la transición es válida y el pedido tiene productos
    public void cambiarEstado(EstadoPedido destino) {
        exigirTransicionPermitida(destino);
        exigirProductosParaAvanzar(destino);
        estado = destino;
    }

    // Suma los subtotales de todos los productos del pedido
    public BigDecimal calcularTotal() {
        BigDecimal suma = Dinero.CERO;
        for (DetallePedido detalle : detalles) {
            suma = suma.add(detalle.subtotal());
        }
        return Dinero.redondear(suma);
    }

    // Actualiza el total guardado con la suma actual
    public void recalcularTotal() {
        total = calcularTotal();
    }

    // Revisa que el total guardado coincida con la suma de los detalles
    public boolean totalConsistente() {
        return calcularTotal().compareTo(total) == 0;
    }

    // Lanza error si el pedido no puede pasar a ese estado
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

    // Un pedido sin productos no puede pasar a preparación ni completarse
    private void exigirProductosParaAvanzar(EstadoPedido destino) {
        boolean avanza = destino == EstadoPedido.EN_PREPARACION || destino == EstadoPedido.COMPLETADO;
        if (avanza && detalles.isEmpty()) {
            throw new ValidacionException("Un pedido sin productos no puede enviarse a preparación ni completarse. "
                    + "Agregue productos o cancele el pedido.");
        }
    }

    // Busca la línea de un producto; devuelve null si no está
    private DetallePedido buscarDetalle(String codigoProducto) {
        for (DetallePedido detalle : detalles) {
            if (detalle.getCodigoProducto().equals(codigoProducto)) {
                return detalle;
            }
        }
        return null;
    }

    // Igual que la anterior, pero lanza error si el producto no está en el pedido
    private DetallePedido buscarDetalleObligatorio(String codigoProducto) {
        DetallePedido detalle = buscarDetalle(codigoProducto);
        if (detalle == null) {
            throw new ValidacionException("El producto no forma parte de este pedido.");
        }
        return detalle;
    }

    // Lanza error si el producto no existe o no está disponible
    private void exigirProductoDisponible(Producto producto) {
        if (producto == null) {
            throw new ValidacionException("El producto seleccionado no existe.");
        }
        if (!producto.isDisponible()) {
            throw new ValidacionException("El producto \"" + producto.getNombre() + "\" no está disponible.");
        }
    }

    // Un pedido cerrado ya no se puede modificar
    private void exigirModificable() {
        if (!estado.permiteModificarDetalle()) {
            throw new ValidacionException("El pedido está \"" + estado
                    + "\" y ya no admite cambios en sus productos.");
        }
    }

    // Lanza error si la cantidad es cero o negativa
    private static void exigirCantidadValida(int cantidad) {
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad debe ser un entero mayor que cero.");
        }
    }
}
