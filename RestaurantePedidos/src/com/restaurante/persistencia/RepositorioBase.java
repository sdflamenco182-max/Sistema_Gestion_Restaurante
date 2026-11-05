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

// Base de los repositorios; T es el tipo de entidad con el que trabaja (Cliente, Mesa, etc.)
abstract class RepositorioBase<T extends EntidadBase> {

    // Colección de MongoDB donde guarda esta entidad
    protected final MongoCollection<Document> coleccion;

    protected RepositorioBase(String nombreColeccion) {
        this.coleccion = MongoConexion.obtener().coleccion(nombreColeccion);
    }

    // Cada subclase dice cómo pasar el objeto a documento de MongoDB
    protected abstract Document aDocumento(T entidad);

    // Cada subclase dice cómo pasar el documento de MongoDB al objeto
    protected abstract T deDocumento(Document documento);

    // Orden de las consultas; por defecto por código (las subclases lo pueden cambiar)
    protected Bson ordenPorDefecto() {
        return Sorts.ascending("codigo");
    }

    // Guarda la entidad; si el código ya existe lanza ValidacionException
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

    // Busca una entidad por su código; devuelve null si no existe
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

    // Dice si ya hay un registro con ese código
    public boolean existe(String codigo) {
        try {
            return coleccion.countDocuments(Filters.eq("codigo", codigo)) > 0;
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    // Trae todos los registros de la colección
    public List<T> listar() {
        return consultar(new Document());
    }

    // Reemplaza el documento con el mismo código; true si lo encontró
    public boolean actualizar(T entidad) {
        try {
            Bson filtro = Filters.eq("codigo", entidad.getCodigo());
            return coleccion.replaceOne(filtro, aDocumento(entidad)).getMatchedCount() > 0;
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    // Borra el registro con ese código; true si se borró algo
    public boolean eliminar(String codigo) {
        try {
            return coleccion.deleteOne(Filters.eq("codigo", codigo)).getDeletedCount() > 0;
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    // Busca con un filtro y devuelve los resultados ya convertidos y ordenados
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

    // Cuenta cuántos documentos cumplen el filtro
    protected long contar(Bson filtro) {
        try {
            return coleccion.countDocuments(filtro);
        } catch (MongoException e) {
            throw crearError(e);
        }
    }

    // Pasa el error de Mongo a PersistenciaException con un mensaje entendible
    protected PersistenciaException crearError(MongoException e) {
        if (e instanceof MongoTimeoutException) {
            return new PersistenciaException("No hay conexión con MongoDB. Verifique que el servicio esté iniciado "
                    + "y que la configuración (mongodb.properties) sea correcta.", e);
        }
        return new PersistenciaException("Error de conexión, lectura o escritura en MongoDB: " + e.getMessage(), e);
    }

    // Convierte el documento a objeto; si los datos están mal lanza PersistenciaException
    private T convertir(Document documento) {
        try {
            return deDocumento(documento);
        } catch (RuntimeException e) {
            throw new PersistenciaException("La información almacenada no pudo interpretarse correctamente "
                    + "(documento " + documento.get("codigo") + ").", e);
        }
    }
}
