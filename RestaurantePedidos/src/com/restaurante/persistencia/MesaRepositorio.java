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

public class MesaRepositorio extends RepositorioBase<Mesa> {

    public MesaRepositorio() {
        super(Colecciones.MESAS);
    }

    @Override
    protected Document aDocumento(Mesa m) {
        return new Document("codigo", m.getCodigo())
                .append("numero", m.getNumero())
                .append("capacidad", m.getCapacidad())
                .append("estado", m.getEstado().name());
    }

    @Override
    protected Mesa deDocumento(Document d) {
        return new Mesa(d.getString("codigo"), d.getInteger("numero"), d.getInteger("capacidad"),
                EstadoMesa.valueOf(d.getString("estado")));
    }

    @Override
    protected Bson ordenPorDefecto() {
        return Sorts.ascending("numero");
    }

    public Mesa buscarPorNumero(int numero) {
        List<Mesa> resultado = consultar(Filters.eq("numero", numero));
        if (resultado.isEmpty()) {
            return null;
        }
        return resultado.get(0);
    }

    public List<Mesa> buscar(String texto) {
        List<Bson> condiciones = new ArrayList<>();
        condiciones.add(Filters.regex("codigo", Pattern.quote(texto), "i"));
        if (esNumero(texto)) {
            condiciones.add(Filters.eq("numero", Integer.parseInt(texto.trim())));
        }
        return consultar(Filters.or(condiciones));
    }

    private boolean esNumero(String texto) {
        try {
            Integer.parseInt(texto.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
