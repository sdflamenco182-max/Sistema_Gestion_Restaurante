package com.restaurante.persistencia;

import com.mongodb.client.model.Filters;
import com.restaurante.modelo.Cliente;
import org.bson.Document;

import java.util.List;
import java.util.regex.Pattern;

// Guarda y consulta clientes en la colección "clientes"
public class ClienteRepositorio extends RepositorioBase<Cliente> {

    public ClienteRepositorio() {
        super(Colecciones.CLIENTES);
    }

    // Cliente a documento
    @Override
    protected Document aDocumento(Cliente c) {
        return new Document("codigo", c.getCodigo())
                .append("nombre", c.getNombre())
                .append("telefono", c.getTelefono())
                .append("correo", c.getCorreo())
                .append("activo", c.isActivo());
    }

    // Documento a cliente (si no trae "activo" lo toma como true)
    @Override
    protected Cliente deDocumento(Document d) {
        return new Cliente(d.getString("codigo"), d.getString("nombre"), d.getString("telefono"),
                d.getString("correo"), d.getBoolean("activo", true));
    }

    // Busca clientes cuyo código o nombre contenga el texto, sin importar mayúsculas
    public List<Cliente> buscar(String texto) {
        String patron = Pattern.quote(texto);
        return consultar(Filters.or(Filters.regex("codigo", patron, "i"), Filters.regex("nombre", patron, "i")));
    }
}
