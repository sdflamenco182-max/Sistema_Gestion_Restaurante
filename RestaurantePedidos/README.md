# Sistema de Pedidos de un Restaurante (Clave 7)

Proyecto de Programación Orientada a Objetos — Universidad de El Salvador en Línea.
Aplicación de escritorio en **Java 21 + Java Swing + MongoDB**, con login, gestión de clientes,
mesas, productos y pedidos, validaciones y reportes.

## Estado actual del proyecto

Esta versión contiene la **base** (login, conexión a MongoDB, modelo, repositorios y servicios) y el
módulo de **Consultas y reportes**. Las pantallas de **Clientes, Mesas, Productos y Pedidos** se irán
agregando en commits posteriores de cada integrante: cada una es un archivo `Panel*.java` en
`src/com/restaurante/ui/` más una línea en `MainFrame.crearPestanas()`.

## Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 21 (también funciona con versiones superiores) |
| NetBeans | 22 o superior (proyecto «Java with Ant»; incluye Apache Ant) |
| MongoDB Community Server | 7.x o superior, en ejecución en `localhost:27017` |
| Driver `mongodb-driver-sync` | 5.2.1 (ya incluido en la carpeta `lib`) |

Las librerías necesarias están en la carpeta `lib` del proyecto; no hace falta descargar nada.

## Configuración de MongoDB

1. Copie `mongodb.properties.example` como `mongodb.properties` en la **raíz del proyecto**
   (o junto al JAR) y ajuste la URI si su MongoDB no es local o requiere autenticación.
   Sin ese archivo se usa `mongodb://localhost:27017` y la base `restaurante_db`.
   También puede definir la variable de entorno `MONGODB_URI`.
2. Cree los datos iniciales con **una** de estas opciones:
   - Desde la aplicación (no requiere `mongosh`):
     ```
     ant jar
     java -jar dist/RestaurantePedidos.jar --init-bd
     ```
     (en NetBeans: clic derecho sobre el proyecto > *Clean and Build*, y luego ejecutar el JAR de la carpeta `dist`).
   - Con el script de la carpeta `BaseDatos`:
     ```
     mongosh "mongodb://localhost:27017" ../BaseDatos/init.js
     ```
   Ambas crean los índices únicos, el usuario de prueba y datos de ejemplo
   (6 clientes, 6 mesas, 8 productos y 6 pedidos en distintos estados).

## Ejecución

**Desde NetBeans:** `File > Open Project`, seleccione la carpeta `RestaurantePedidos`,
clic derecho > *Run* (clase principal `com.restaurante.App`).

**Desde consola** (requiere Ant instalado, o usar el que trae NetBeans):
```
ant clean jar
java -jar dist/RestaurantePedidos.jar
```

**Pruebas unitarias:** `ant probar` (validaciones, hash de contraseñas y reglas del pedido; no requieren MongoDB).
En NetBeans se ejecuta el objetivo `probar` desde la pestaña *Files* > `build.xml` > clic derecho > *Run Target*.

## Carpetas del proyecto

```
RestaurantePedidos/
├── src/            Código fuente (paquetes com.restaurante.*)
├── test/           Pruebas unitarias (JUnit 5)
├── lib/            Librerías de ejecución (controlador de MongoDB y SLF4J)
├── lib-test/       Librería de pruebas (JUnit 5)
├── nbproject/      Configuración del proyecto NetBeans (Ant)
├── build.xml       Script de construcción Ant
└── manifest.mf     Manifiesto del JAR
```

## Usuario de prueba

| Usuario | Contraseña |
|---|---|
| `admin` | `Admin123` |

La contraseña se almacena únicamente como hash PBKDF2-HMAC-SHA256 con sal aleatoria.

## Estructura

```
com.restaurante
├── App                  Punto de entrada (--init-bd para cargar datos)
├── modelo               Entidades: Cliente, Mesa, Producto, Pedido, DetallePedido, Usuario y estados
├── persistencia         MongoConexion y repositorios (RepositorioBase<T> genérico)
├── servicio             Reglas de negocio y casos de uso (AuthServicio, PedidoServicio, ...)
├── ui                   Interfaces Swing (LoginFrame, MainFrame y un panel por módulo)
├── util                 Validador, PasswordHasher, Dinero
└── excepcion            ValidacionException y PersistenciaException
```

La interfaz **no** accede a MongoDB: llama a los servicios, estos aplican las reglas y usan los repositorios.

## Colecciones de MongoDB

| Colección | Campos principales |
|---|---|
| `usuarios` | nombreUsuario (único), nombreCompleto, passwordHash, activo |
| `clientes` | codigo (único), nombre, telefono, correo, activo |
| `mesas` | codigo (único), numero (único), capacidad, estado (DISPONIBLE/OCUPADA) |
| `productos` | codigo (único), nombre, categoria, descripcion, precio (Decimal128), disponible |
| `pedidos` | codigo (único), fecha, codigoCliente?, codigoMesa?, estado, detalles[] (codigoProducto, nombreProducto, precioUnitario, cantidad), total (Decimal128) |
| `contadores` | _id "pedidos", valor (secuencia atómica para PED-0001, PED-0002...) |

## Reglas principales

- Estados del pedido: `ABIERTO → EN_PREPARACION → COMPLETADO`; `ABIERTO/EN_PREPARACION → CANCELADO`.
- Los productos solo se agregan, modifican o quitan mientras el pedido está **abierto**.
- Un pedido sin productos no puede enviarse a preparación ni completarse.
- Cada detalle guarda el **precio aplicado**; cambiar el precio del producto no altera pedidos anteriores.
- El total se recalcula tras cada cambio del detalle; si el total almacenado no coincide con la suma, se muestra una advertencia.
- Una mesa se ocupa al crear un pedido con ella y se libera al completar o cancelar el pedido.
- No se elimina una mesa con pedidos activos, ni un producto que forma parte de pedidos (se marca no disponible).
- Un cliente con pedidos históricos se **inactiva** en lugar de eliminarse.
- Importes con `BigDecimal` y 2 decimales (`Decimal128` en MongoDB).
