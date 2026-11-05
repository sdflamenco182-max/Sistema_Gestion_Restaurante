package com.restaurante.ui;

import com.restaurante.modelo.Cliente;
import com.restaurante.modelo.DetallePedido;
import com.restaurante.modelo.Mesa;
import com.restaurante.modelo.Pedido;
import com.restaurante.servicio.ClienteServicio;
import com.restaurante.servicio.MesaServicio;
import com.restaurante.util.Dinero;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// Guarda los nombres de clientes y mesas para mostrar nombres en vez de códigos
final class CatalogoNombres {

    // Formato en que se muestran las fechas
    static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Servicios y mapas código -> nombre
    private final ClienteServicio clientes = new ClienteServicio();
    private final MesaServicio mesas = new MesaServicio();
    private final Map<String, String> nombresCliente = new HashMap<>();
    private final Map<String, String> nombresMesa = new HashMap<>();

    // Vuelve a leer clientes y mesas y rellena los mapas
    void recargar() {
        nombresCliente.clear();
        for (Cliente c : clientes.listar()) {
            nombresCliente.put(c.getCodigo(), c.getNombre());
        }
        nombresMesa.clear();
        for (Mesa m : mesas.listar()) {
            nombresMesa.put(m.getCodigo(), "Mesa " + m.getNumero());
        }
    }

    // Devuelve el nombre del cliente a partir de su código
    String cliente(String codigo) {
        if (codigo == null) {
            return "(sin cliente)";
        }
        return nombresCliente.getOrDefault(codigo, codigo);
    }

    // Devuelve el texto "Mesa N" a partir del código de la mesa
    String mesa(String codigo) {
        if (codigo == null) {
            return "(sin mesa)";
        }
        return nombresMesa.getOrDefault(codigo, codigo);
    }

    // Arma el texto completo de un pedido para mostrarlo en el detalle
    String describir(Pedido pedido) {
        // datos generales
        StringBuilder sb = new StringBuilder();
        sb.append("Pedido: ").append(pedido.getCodigo()).append('\n')
                .append("Fecha: ").append(pedido.getFecha().format(FORMATO_FECHA)).append('\n')
                .append("Cliente: ").append(cliente(pedido.getCodigoCliente())).append('\n')
                .append("Mesa: ").append(mesa(pedido.getCodigoMesa())).append('\n')
                .append("Estado: ").append(pedido.getEstado()).append("\n\n")
                .append("Productos:\n");
        if (pedido.getDetalles().isEmpty()) {
            sb.append("  (sin productos)\n");
        }
        // una línea por producto
        for (DetallePedido d : pedido.getDetalles()) {
            sb.append(String.format("  %-26s %3d x %8s = %9s%n",
                    d.getNombreProducto(), d.getCantidad(),
                    Dinero.formato(d.getPrecioUnitario()),
                    Dinero.formato(d.subtotal())));
        }
        // total y aviso si no coincide con el detalle
        sb.append("\nTotal: ").append(Dinero.formato(pedido.calcularTotal()));
        if (!pedido.totalConsistente()) {
            sb.append("\n[ADVERTENCIA] El total almacenado (")
                    .append(Dinero.formato(pedido.getTotal()))
                    .append(") no coincide con la suma de los detalles.");
        }
        return sb.toString();
    }
}
