package com.restaurante.persistencia;

import com.mongodb.client.model.Filters;
import com.restaurante.modelo.Producto;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.util.List;
import java.util.regex.Pattern;

// Guarda y consulta productos en la colección "productos"
public class ProductoRepositorio extends RepositorioBase<Producto> {

    public ProductoRepositorio() {
        super(Colecciones.PRODUCTOS);
    }

    // Producto a documento (el precio se guarda como Decimal128)
    @Override
    protected Document aDocumento(Producto p) {
        return new Document("codigo", p.getCodigo())
                .append("nombre", p.getNombre())
                .append("categoria", p.getCategoria())
                .append("descripcion", p.getDescripcion())
                .append("precio", new Decimal128(p.getPrecio()))
                .append("disponible", p.isDisponible());
    }

    // Documento a producto
    @Override
    protected Producto deDocumento(Document d) {
        return new Producto(d.getString("codigo"), d.getString("nombre"), d.getString("categoria"),
                d.getString("descripcion"), d.get("precio", Decimal128.class).bigDecimalValue(),
                d.getBoolean("disponible", true));
    }

    // Trae solo los productos marcados como disponibles
    public List<Producto> listarDisponibles() {
        return consultar(Filters.eq("disponible", true));
    }

    // Busca por código, nombre o categoría, sin importar mayúsculas
    public List<Producto> buscar(String texto) {
        String patron = Pattern.quote(texto);
        return consultar(Filters.or(Filters.regex("codigo", patron, "i"),
                Filters.regex("nombre", patron, "i"), Filters.regex("categoria", patron, "i")));
    }
}
