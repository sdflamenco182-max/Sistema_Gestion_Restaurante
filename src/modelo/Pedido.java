package modelo;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int idPedido;
    private List<DetallePedido> detalles;
    
    public Pedido(int idPedido) {
        this.idPedido = idPedido;
        this.detalles = new ArrayList<>();
    }
    
    // Agregar un producto al pedido
    public void agregarDetalle(Producto producto, int cantidad) {
        for (DetallePedido d : detalles) {
            if (d.getProducto().getCodigo().equalsIgnoreCase(producto.getCodigo())) {
                d.setCantidad(d.getCantidad() + cantidad);
                // Actualiza cantidad si ya existe
                return;
            }
        }
        detalles.add(new DetallePedido(producto, cantidad));
    }
    
    // Modificar cantidad de un producto en el pedido
    public void modificarDetalle(String codigoProducto, int nuevaCantidad) {
        for (DetallePedido d : detalles) {
            if (d.getProducto().getCodigo().equalsIgnoreCase(codigoProducto)) {
                d.setCantidad(nuevaCantidad);
                break;
            }
        }
    }
    
    // Eliminar producto del pedido
    public void eliminarDetalle(String codigoProducto) {
        detalles.removeIf(d -> d.getProducto().getCodigo().equalsIgnoreCase(codigoProducto));
    }
    
    // Calcular total: sumar los subtotales de todos los detalles
    // Actualización automática
    public double calcularTotal() {
        double total = 0;
        for (DetallePedido d : detalles) {
            total += d.calcularSubtotal();
        }
        return total;
    }
    
    // Mostrar el resultado: presentar el detalle y el total actualizado
    public void mostrarResumenPedido() {
        System.out.println(" PEDIDO #" + idPedido + " ");
        for (DetallePedido d : detalles) {
            System.out.println("- " + d);
        }
        System.out.println("-----------------------------------");
        System.out.println("TOTAL A PAGAR: $" + calcularTotal());
    }
}