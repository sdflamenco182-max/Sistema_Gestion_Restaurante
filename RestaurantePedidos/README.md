# Sistema de Pedidos de un Restaurante (Clave 7)

Proyecto de Programación Orientada a Objetos — Universidad de El Salvador en Línea.
Aplicación de escritorio en **Java 21 + Java Swing + MongoDB**, con login, gestión de clientes,
mesas, productos y pedidos, validaciones y reportes.

## Contenido de la entrega

```
Proyecto_37/
├── RestaurantePedidos/     Proyecto de NetBeans (código, librerías y este README)
├── BaseDatos/              Script init.js con los datos de prueba e instrucciones (LEEME.txt)
├── Documentacion/          Documento técnico
└── Evidencia_Repositorio/  Enlace de GitHub e historial de commits
```

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
2. Cree los datos iniciales ejecutando el script de la carpeta `BaseDatos` con `mongosh`
   (o abriéndolo en MongoDB Compass):
   ```
   mongosh "mongodb://localhost:27017" ../BaseDatos/init.js
   ```
   (este comando se ejecuta desde la carpeta `RestaurantePedidos`; desde la carpeta principal de la
   entrega sería `BaseDatos/init.js`).
   El script crea los índices únicos, el usuario de prueba y datos de ejemplo
   (6 clientes, 6 mesas, 8 productos y 6 pedidos en distintos estados). Se puede ejecutar
   varias veces: si ya hay datos, no los vuelve a cargar.

## Ejecución

**Desde NetBeans:** `File > Open Project`, seleccione la carpeta `RestaurantePedidos`,
clic derecho > *Run* (clase principal `com.restaurante.App`).

**Desde consola** (requiere Ant instalado, o usar el que trae NetBeans):
```
ant clean jar
java -jar dist/RestaurantePedidos.jar
```

## Carpetas del proyecto

```
RestaurantePedidos/
├── src/            Código fuente (paquetes com.restaurante.*)
├── lib/            Librerías de ejecución (controlador de MongoDB y SLF4J)
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
├── App                  Punto de entrada de la aplicación
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
