# Sistema de pedidos de un restaurante

Este es el proyecto final de Programación Orientada a Objetos (Universidad de El Salvador en Línea, grupo de tutoría 3, grupo de proyecto 7). Elegimos la clave 7, el sistema de pedidos de un restaurante.

La aplicación sirve para llevar el control de lo que pasa en un restaurante pequeño: quién hizo un pedido, en qué mesa está, qué pidió y cuánto debe pagar. Está hecha en Java con Swing para las ventanas y guarda todo en MongoDB.

Integrantes: [nombres y usuarios de GitHub]

## Qué necesita para usarla

- Java 21 o más nuevo.
- NetBeans, porque el proyecto es de tipo "Java with Ant" y ya trae Ant incluido.
- MongoDB instalado y corriendo en su computadora (por defecto en `localhost:27017`).
- `mongosh`, la consola de MongoDB, para cargar los datos de prueba. También sirve abrir el script con MongoDB Compass.

Las librerías que usa el programa ya vienen dentro de la carpeta `lib`, así que no hay que descargar nada más.

## Cómo ponerla a funcionar

1. Abra NetBeans y elija `File > Open Project`. Busque la carpeta `RestaurantePedidos`.
2. Cargue los datos de prueba. Desde la carpeta principal del repositorio ejecute:

   ```
   mongosh "mongodb://localhost:27017" BaseDatos/init.js
   ```

   El script crea las colecciones, un usuario para entrar y algunos datos de ejemplo: 6 clientes, 6 mesas, 8 productos y 6 pedidos. Si lo corre otra vez no pasa nada, porque si ya hay datos no los vuelve a cargar.
3. Si su MongoDB no está en `localhost:27017`, copie el archivo `mongodb.properties.example` con el nombre `mongodb.properties` y cambie la dirección ahí.
4. En NetBeans haga clic derecho sobre el proyecto, elija `Clean and Build` y después `Run`.

## Cómo se usa

Al abrirla aparece el login. Para probar use:

- Usuario: `admin`
- Contraseña: `Admin123`

Sin entrar no se puede ver ninguna otra pantalla. Dentro hay una pestaña para cada parte del sistema:

- **Pedidos.** Se crea un pedido, y si quiere se le asigna un cliente y una mesa. Se le agregan productos con su cantidad y el total se va calculando solo. Cuando está listo se manda a preparación, y al final se completa. También se puede cancelar.
- **Clientes.** Registrar, buscar, modificar y eliminar clientes.
- **Mesas.** Lo mismo para las mesas, con su número, su capacidad y si están libres u ocupadas.
- **Productos.** El menú del restaurante, con categoría, precio y si está disponible o no.
- **Consultas y reportes.** Sirve para buscar un pedido por su código, ver los de una mesa, los de un estado o los de un rango de fechas. También muestra el listado de productos y un resumen con cuántos pedidos hay en cada estado y cuánto suman.

Para salir, el menú `Sesión` tiene la opción de cerrar sesión.

[Si en su rama todavía solo está la pestaña de Consultas, deje este aviso y borre el cuando estén todas: "Por ahora esta versión solo trae Consultas y reportes. Las demás pestañas las vamos agregando entre todos."]

## Reglas que tiene el sistema

Algunas cosas que quisimos que el programa cuidara:

- Un pedido solo se puede modificar mientras está abierto. Una vez que pasa a preparación ya no se tocan sus productos.
- Un pedido sin productos no se puede mandar a preparación ni completar.
- Cuando se crea un pedido con una mesa, la mesa queda ocupada. Se libera sola cuando el pedido se completa o se cancela.
- Cada producto del pedido guarda el precio que tenía en ese momento. Si después se cambia el precio en el menú, los pedidos viejos no se alteran.
- No se puede eliminar una mesa que tiene pedidos activos. Un cliente que ya tiene pedidos no se borra, se deja inactivo para no perder el historial. Un producto que ya se usó en un pedido tampoco se borra, solo se marca como no disponible.
- Los datos se revisan antes de guardarse: campos vacíos, cantidades que no sean números enteros mayores que cero, precios negativos, correos mal escritos y códigos repetidos.
- Las contraseñas no se guardan tal cual. En la base solo queda un hash.

## Cómo está organizado el código

Todo el código está en `RestaurantePedidos/src/com/restaurante`, dividido en carpetas:

- `modelo`: las clases que representan las cosas del restaurante (cliente, mesa, producto, pedido y su detalle).
- `persistencia`: todo lo que habla con MongoDB.
- `servicio`: las reglas del sistema, por ejemplo qué se puede hacer con un pedido según su estado.
- `ui`: las ventanas.
- `util` y `excepcion`: validaciones, manejo de dinero, contraseñas y los errores propios del programa.

La idea es que las ventanas nunca hablan directo con la base de datos. Le piden las cosas a un servicio, y el servicio usa la persistencia.

## Pruebas

Hay pruebas automáticas para las validaciones, el manejo de contraseñas y las reglas del pedido. Se corren con:

```
ant probar
```

## Base de datos

Se llama `restaurante_db` y tiene estas colecciones: `usuarios`, `clientes`, `mesas`, `productos`, `pedidos` y `contadores`. Los productos de cada pedido se guardan dentro del mismo pedido, junto con el precio y la cantidad.
