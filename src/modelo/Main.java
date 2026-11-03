package modelo;

import modelo.Pedido;
import modelo.Producto;

public class Main {
    public static void main(String[] args) {
        // Producto inicial de $5.00
        Producto p1 = new Producto("P01", "Pizza Personal", "Comida", "Pizza de queso", 5.00);
        
        Pedido pedido = new Pedido(1);
        pedido.agregarDetalle(p1, 2);
        //2 pizzas a $5.00 = $10.00
        
        // Mostrar resultado inicial (5.5)
        pedido.mostrarResumenPedido();
        
        // Cambiamos el precio oficial del producto en el catálogo a $6.00
        p1.setPrecioUnitario(6.00);
        
        // El pedido anterior NO debe alterarse porque guarda el precio histórico ($5.00)
        System.out.println("\n--- Después de cambiar el precio del producto en el catálogo ---");
        pedido.mostrarResumenPedido();
    }
    
}
