package com.restaurante.persistencia;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.restaurante.excepcion.PersistenciaException;
import com.restaurante.modelo.Usuario;
import org.bson.Document;

public class UsuarioRepositorio {

    private static final String MENSAJE_ERROR_MONGO = "Error de conexión, lectura o escritura en MongoDB: ";

    private final MongoCollection<Document> coleccion =
            MongoConexion.obtener().coleccion(Colecciones.USUARIOS);

    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        MongoConexion.obtener().verificar();
        try {
            Document d = coleccion.find(Filters.eq("nombreUsuario", nombreUsuario)).first();
            if (d == null) {
                return null;
            }
            return new Usuario(d.getString("nombreUsuario"), d.getString("nombreCompleto"),
                    d.getString("passwordHash"), d.getBoolean("activo", true));
        } catch (MongoException e) {
            throw new PersistenciaException(MENSAJE_ERROR_MONGO + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new PersistenciaException("La información del usuario almacenada no pudo interpretarse.", e);
        }
    }

    public void insertar(Usuario usuario) {
        try {
            coleccion.insertOne(new Document("nombreUsuario", usuario.getNombreUsuario())
                    .append("nombreCompleto", usuario.getNombreCompleto())
                    .append("passwordHash", usuario.getPasswordHash())
                    .append("activo", usuario.isActivo()));
        } catch (MongoException e) {
            throw new PersistenciaException(MENSAJE_ERROR_MONGO + e.getMessage(), e);
        }
    }

    public long contar() {
        try {
            return coleccion.countDocuments();
        } catch (MongoException e) {
            throw new PersistenciaException(MENSAJE_ERROR_MONGO + e.getMessage(), e);
        }
    }
}
