package com.restaurante.persistencia;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.restaurante.excepcion.PersistenciaException;
import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.EntidadBase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

abstract class RepositorioBase<T extends EntidadBase> {

    protected final MongoCollection<Document> coleccion;

    protected RepositorioBase(String nombreColeccion) {
        this.coleccion = MongoConexion.obtener().coleccion(nombreColeccion);
    }

    protected abstract Document aDocumento(T entidad);

    protected abstract T deDocumento(Document documento);

    protected Bson ordenPorDefecto() {
        return Sorts.ascending("codigo");
    }

    public void insertar(T entidad) {
        try {
            coleccion.insertOne(aDocumento(entidad));
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                throw new ValidacionException("Ya existe un registro con el código " + entidad.getCodigo()
                        + " (o con un valor que debe ser único).");
            }
            throw crearError(e);
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    public T buscarPorCodigo(String codigo) {
        try {
            Document documento = coleccion.find(Filters.eq("codigo", codigo)).first();
            if (documento == null) {
                return null;
            }
            return convertir(documento);
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    public boolean existe(String codigo) {
        try {
            return coleccion.countDocuments(Filters.eq("codigo", codigo)) > 0;
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    public List<T> listar() {
        return consultar(new Document());
    }

    public boolean actualizar(T entidad) {
        try {
            Bson filtro = Filters.eq("codigo", entidad.getCodigo());
            return coleccion.replaceOne(filtro, aDocumento(entidad)).getMatchedCount() > 0;
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    public boolean eliminar(String codigo) {
        try {
            return coleccion.deleteOne(Filters.eq("codigo", codigo)).getDeletedCount() > 0;
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    protected List<T> consultar(Bson filtro) {
        try {
            List<T> resultado = new ArrayList<>();
            for (Document documento : coleccion.find(filtro).sort(ordenPorDefecto())) {
                resultado.add(convertir(documento));
            }
            return resultado;
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    protected long contar(Bson filtro) {
        try {
            return coleccion.countDocuments(filtro);
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    protected PersistenciaException crearError(MongoException e) {
        if (e instanceof MongoTimeoutException) {
            return new PersistenciaException("No hay conexión con MongoDB. Verifique que el servicio esté iniciado "
                    + "y que la configuración (mongodb.properties) sea correcta.", e);
        }
        return new PersistenciaException("Error de conexión, lectura o escritura en MongoDB: " + e.getMessage(), e);
    }

    private T convertir(Document documento) {
        try {
            return deDocumento(documento);
        } catch (RuntimeException e) {
            throw new PersistenciaException("La información almacenada no pudo interpretarse correctamente "
                    + "(documento " + documento.get("codigo") + ").", e);
        }
    }
}
