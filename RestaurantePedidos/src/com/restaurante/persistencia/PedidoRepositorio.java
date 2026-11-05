package com.restaurante.persistencia;

import com.mongodb.MongoException;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.restaurante.modelo.DetallePedido;
import com.restaurante.modelo.EstadoPedido;
import com.restaurante.modelo.Pedido;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Guarda y consulta pedidos (con sus detalles) en la colección "pedidos"
public class PedidoRepositorio extends RepositorioBase<Pedido> {

    private static final String SECUENCIA_PEDIDOS = "pedidos";

    public PedidoRepositorio() {
        super(Colecciones.PEDIDOS);
    }

    // Pedido a documento; los detalles van como lista dentro del mismo documento
    @Override
    protected Document aDocumento(Pedido p) {
        List<Document> detalles = new ArrayList<>();
        for (DetallePedido d : p.getDetalles()) {
            detalles.add(new Document("codigoProducto", d.getCodigoProducto())
                    .append("nombreProducto", d.getNombreProducto())
                    .append("precioUnitario", new Decimal128(d.getPrecioUnitario()))
                    .append("cantidad", d.getCantidad()));
        }
        return new Document("codigo", p.getCodigo())
                .append("fecha", aDate(p.getFecha()))
                .append("codigoCliente", p.getCodigoCliente())
                .append("codigoMesa", p.getCodigoMesa())
                .append("estado", p.getEstado().name())
                .append("detalles", detalles)
                .append("total", new Decimal128(p.getTotal()));
    }

    // Documento a pedido, armando otra vez la lista de detalles y la fecha
    @Override
    protected Pedido deDocumento(Document d) {
        List<DetallePedido> detalles = new ArrayList<>();
        for (Document x : d.getList("detalles", Document.class)) {
            detalles.add(new DetallePedido(x.getString("codigoProducto"), x.getString("nombreProducto"),
                    x.get("precioUnitario", Decimal128.class).bigDecimalValue(), x.getInteger("cantidad")));
        }
        LocalDateTime fecha = LocalDateTime.ofInstant(d.getDate("fecha").toInstant(), ZoneId.systemDefault());
        return Pedido.restaurar(d.getString("codigo"), fecha, d.getString("codigoCliente"), d.getString("codigoMesa"),
                EstadoPedido.valueOf(d.getString("estado")), detalles,
                d.get("total", Decimal128.class).bigDecimalValue());
    }

    // Los pedidos salen del más reciente al más antiguo
    @Override
    protected Bson ordenPorDefecto() {
        return Sorts.descending("fecha");
    }

    // Genera el siguiente código (PED-0001, PED-0002...) con un contador guardado en MongoDB
    public String siguienteCodigo() {
        try {
            FindOneAndUpdateOptions opciones = new FindOneAndUpdateOptions()
                    .upsert(true).returnDocument(ReturnDocument.AFTER);
            Document actualizado = MongoConexion.obtener().coleccion(Colecciones.CONTADORES).findOneAndUpdate(
                    Filters.eq("_id", SECUENCIA_PEDIDOS), Updates.inc("valor", 1), opciones);
            return String.format("PED-%04d", actualizado.getInteger("valor"));
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    // Trae los pedidos que todavía están activos (no cerrados ni cancelados)
    public List<Pedido> listarActivos() {
        return consultar(Filters.in("estado", nombresEstadosActivos()));
    }

    // Trae los pedidos que están en ese estado
    public List<Pedido> porEstado(EstadoPedido estado) {
        return consultar(Filters.eq("estado", estado.name()));
    }

    // Trae los pedidos de una mesa
    public List<Pedido> porMesa(String codigoMesa) {
        return consultar(Filters.eq("codigoMesa", codigoMesa));
    }

    // Trae los pedidos desde una fecha (incluida) hasta otra (sin incluir)
    public List<Pedido> porRangoFecha(LocalDateTime desde, LocalDateTime hasta) {
        return consultar(Filters.and(
                Filters.gte("fecha", aDate(desde)),
                Filters.lt("fecha", aDate(hasta))));
    }

    // Dice si la mesa tiene algún pedido activo
    public boolean existenActivosDeMesa(String codigoMesa) {
        Bson filtro = Filters.and(Filters.eq("codigoMesa", codigoMesa),
                Filters.in("estado", nombresEstadosActivos()));
        return contar(filtro) > 0;
    }

    // Dice si el cliente tiene algún pedido
    public boolean existenDeCliente(String codigoCliente) {
        return contar(Filters.eq("codigoCliente", codigoCliente)) > 0;
    }

    // Dice si el producto aparece en el detalle de algún pedido
    public boolean existenConProducto(String codigoProducto) {
        return contar(Filters.eq("detalles.codigoProducto", codigoProducto)) > 0;
    }

    // Pasa LocalDateTime a Date, que es lo que guarda MongoDB
    private static Date aDate(LocalDateTime fecha) {
        return Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Arma la lista con los nombres de los estados que cuentan como activos
    private static List<String> nombresEstadosActivos() {
        List<String> nombres = new ArrayList<>();
        for (EstadoPedido e : EstadoPedido.values()) {
            if (e.esActivo()) {
                nombres.add(e.name());
            }
        }
        return nombres;
    }
}
