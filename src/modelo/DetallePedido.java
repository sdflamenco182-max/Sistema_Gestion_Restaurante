package modelo;

public class DetallePedido {
    private Producto producto;
    private int cantidad;
    private double precioHistorico;
    //Guarda el precio del producto al agregarse
    
    // Obtener precio
    public DetallePedido(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioHistorico = producto.getPrecioUnitario();
    }
    
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {this.cantidad = cantidad; }
    public double getPrecioHistorico() { return  precioHistorico; }
    
    // Calcular subtotal = precio unitario histórico × cantidad
    public double calcularSubtotal() {
        return this.precioHistorico * this.cantidad;
    }
    
    @Override
    public String toString() {
        return producto.getNombre() + " | Cant: " + cantidad + " | Precio Unit: $" + precioHistorico + " | Subtotal: $" + calcularSubtotal();
    }
}