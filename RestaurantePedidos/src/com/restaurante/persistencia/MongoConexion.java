package com.restaurante.persistencia;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.restaurante.excepcion.PersistenciaException;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

// Maneja la conexión a MongoDB; hay una sola para todo el programa (singleton)
public final class MongoConexion {

    private static final String URI_POR_DEFECTO = "mongodb://localhost:27017";
    private static final String BD_POR_DEFECTO = "restaurante_db";
    private static MongoConexion instancia;

    private final MongoClient cliente;
    private final MongoDatabase baseDatos;

    // Lee la configuración y abre el cliente de MongoDB (espera máximo 3 segundos al servidor)
    private MongoConexion() {
        Properties config = cargarConfiguracion();
        String uri = obtenerUri(config);
        String nombreBd = config.getProperty("mongodb.database", BD_POR_DEFECTO);
        MongoClientSettings ajustes = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .applyToClusterSettings(b -> b.serverSelectionTimeout(3, TimeUnit.SECONDS))
                .build();
        cliente = MongoClients.create(ajustes);
        baseDatos = cliente.getDatabase(nombreBd);
    }

    // Devuelve la conexión única; la crea la primera vez que se pide
    public static synchronized MongoConexion obtener() {
        if (instancia == null) {
            instancia = new MongoConexion();
        }
        return instancia;
    }

    // Cierra la conexión con MongoDB al terminar el programa
    public static synchronized void cerrar() {
        if (instancia != null) {
            instancia.cliente.close();
            instancia = null;
        }
    }

    // Devuelve la colección con ese nombre de la base de datos
    public MongoCollection<Document> coleccion(String nombre) {
        return baseDatos.getCollection(nombre);
    }

    // Hace un ping a MongoDB para comprobar que responde; si no, lanza PersistenciaException
    public void verificar() {
        try {
            baseDatos.runCommand(new Document("ping", 1));
        } catch (MongoException e) {
            throw new PersistenciaException("No fue posible conectarse a MongoDB. Verifique que el servicio esté "
                    + "iniciado y que la configuración (mongodb.properties) sea correcta.", e);
        }
    }

    // Crea los índices: únicos para códigos y números, y otros para acelerar búsquedas de pedidos
    public void crearIndices() {
        try {
            IndexOptions unico = new IndexOptions().unique(true);
            coleccion(Colecciones.USUARIOS).createIndex(Indexes.ascending("nombreUsuario"), unico);
            coleccion(Colecciones.CLIENTES).createIndex(Indexes.ascending("codigo"), unico);
            coleccion(Colecciones.MESAS).createIndex(Indexes.ascending("codigo"), unico);
            coleccion(Colecciones.MESAS).createIndex(Indexes.ascending("numero"), unico);
            coleccion(Colecciones.PRODUCTOS).createIndex(Indexes.ascending("codigo"), unico);
            coleccion(Colecciones.PEDIDOS).createIndex(Indexes.ascending("codigo"), unico);
            coleccion(Colecciones.PEDIDOS).createIndex(Indexes.ascending("estado"));
            coleccion(Colecciones.PEDIDOS).createIndex(Indexes.ascending("codigoMesa"));
            coleccion(Colecciones.PEDIDOS).createIndex(Indexes.ascending("fecha"));
        } catch (MongoException e) {
            throw new PersistenciaException("No fue posible crear los índices en MongoDB.", e);
        }
    }

    // Saca la dirección de MongoDB: primero la variable MONGODB_URI, luego el archivo de config
    private static String obtenerUri(Properties config) {
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            uri = config.getProperty("mongodb.uri", URI_POR_DEFECTO);
        }
        return uri;
    }

    // Lee mongodb.properties si existe; si no, devuelve la configuración vacía
    private static Properties cargarConfiguracion() {
        Properties propiedades = new Properties();
        Path archivo = Path.of("mongodb.properties");
        if (Files.isRegularFile(archivo)) {
            try (InputStream entrada = Files.newInputStream(archivo)) {
                propiedades.load(entrada);
            } catch (IOException e) {
                throw new PersistenciaException("No se pudo leer el archivo mongodb.properties.", e);
            }
        }
        return propiedades;
    }
}
