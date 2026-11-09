package com.restaurante.persistencia;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.restaurante.modelo.EstadoMesa;
import com.restaurante.modelo.Mesa;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Guarda y consulta mesas en la colección "mesas"
public class MesaRepositorio extends RepositorioBase<Mesa> {

    public MesaRepositorio() {
        super(Colecciones.MESAS);
    }

    // Mesa a documento (el estado se guarda como texto)
    @Override
    protected Document aDocumento(Mesa m) {
        return new Document("codigo", m.getCodigo())
                .append("numero", m.getNumero())
                .append("capacidad", m.getCapacidad())
                .append("estado", m.getEstado().name());
    }

    // Documento a mesa
    @Override
    protected Mesa deDocumento(Document d) {
        return new Mesa(d.getString("codigo"), d.getInteger("numero"), d.getInteger("capacidad"),
                EstadoMesa.valueOf(d.getString("estado")));
    }

    // Las mesas se ordenan por número en vez de por código
    @Override
    protected Bson ordenPorDefecto() {
        return Sorts.ascending("numero");
    }

    // Busca la mesa por su número; devuelve null si no existe
    public Mesa buscarPorNumero(int numero) {
        List<Mesa> resultado = consultar(Filters.eq("numero", numero));
        if (resultado.isEmpty()) {
            return null;
        }
        return resultado.get(0);
    }

    // Busca por código; si el texto es un número también busca por número de mesa
    public List<Mesa> buscar(String texto) {
        List<Bson> condiciones = new ArrayList<>();
        condiciones.add(Filters.regex("codigo", Pattern.quote(texto), "i"));
        if (esNumero(texto)) {
            condiciones.add(Filters.eq("numero", Integer.parseInt(texto.trim())));
        }
        return consultar(Filters.or(condiciones));
    }

    // Revisa si el texto se puede convertir a número entero
    private boolean esNumero(String texto) {
        try {
            Integer.parseInt(texto.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
