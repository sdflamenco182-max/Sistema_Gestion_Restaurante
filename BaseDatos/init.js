
const bd = db.getSiblingDB("restaurante_db");

["usuarios", "clientes", "mesas", "productos", "pedidos", "contadores"].forEach(n => {
  if (!bd.getCollectionNames().includes(n)) bd.createCollection(n);
});
bd.usuarios.createIndex({ nombreUsuario: 1 }, { unique: true });
bd.clientes.createIndex({ codigo: 1 }, { unique: true });
bd.mesas.createIndex({ codigo: 1 }, { unique: true });
bd.mesas.createIndex({ numero: 1 }, { unique: true });
bd.productos.createIndex({ codigo: 1 }, { unique: true });
bd.pedidos.createIndex({ codigo: 1 }, { unique: true });
bd.pedidos.createIndex({ estado: 1 });
bd.pedidos.createIndex({ codigoMesa: 1 });
bd.pedidos.createIndex({ fecha: 1 });

if (bd.usuarios.countDocuments() > 0) {
  print("La base de datos ya contiene datos; no se realizaron cambios.");
} else {
  bd.usuarios.insertOne({
    nombreUsuario: "admin",
    nombreCompleto: "Administrador del sistema",
    passwordHash: "pbkdf2_sha256$120000$czVppDS6UM2OhqBSCsyHdQ==$TJdmfnW2yT5FuE4WJrJFy/Ns3q/VeaOXawl/i4tEHXw=",
    activo: true
  });

  bd.clientes.insertMany([
    { codigo: "C001", nombre: "Ana Martínez",     telefono: "7000-1111", correo: "ana.martinez@correo.com", activo: true },
    { codigo: "C002", nombre: "Carlos Hernández", telefono: "7000-2222", correo: "carlos.h@correo.com",      activo: true },
    { codigo: "C003", nombre: "María López",      telefono: "7000-3333", correo: "maria.lopez@correo.com",   activo: true },
    { codigo: "C004", nombre: "José Ramírez",     telefono: "7000-4444", correo: "",                         activo: true },
    { codigo: "C005", nombre: "Lucía Flores",     telefono: "7000-5555", correo: "lucia.flores@correo.com",  activo: true },
    { codigo: "C006", nombre: "Pedro Castillo",   telefono: "7000-6666", correo: "pedro.c@correo.com",       activo: true }
  ]);

  bd.mesas.insertMany([
    { codigo: "M01", numero: 1, capacidad: 2, estado: "DISPONIBLE" },
    { codigo: "M02", numero: 2, capacidad: 4, estado: "OCUPADA" },
    { codigo: "M03", numero: 3, capacidad: 4, estado: "OCUPADA" },
    { codigo: "M04", numero: 4, capacidad: 6, estado: "DISPONIBLE" },
    { codigo: "M05", numero: 5, capacidad: 6, estado: "DISPONIBLE" },
    { codigo: "M06", numero: 6, capacidad: 8, estado: "DISPONIBLE" }
  ]);

  bd.productos.insertMany([
    { codigo: "P001", nombre: "Pupusa revuelta",         categoria: "Comida",  descripcion: "Chicharrón y frijol",   precio: NumberDecimal("1.25"), disponible: true },
    { codigo: "P002", nombre: "Pupusa de queso",         categoria: "Comida",  descripcion: "Con loroco opcional",   precio: NumberDecimal("1.25"), disponible: true },
    { codigo: "P003", nombre: "Pollo asado con ensalada", categoria: "Comida", descripcion: "Cuarto de pollo",       precio: NumberDecimal("7.50"), disponible: true },
    { codigo: "P004", nombre: "Hamburguesa clásica",     categoria: "Comida",  descripcion: "Con papas fritas",      precio: NumberDecimal("6.00"), disponible: true },
    { codigo: "P005", nombre: "Refresco natural",        categoria: "Bebidas", descripcion: "Vaso de 12 oz",         precio: NumberDecimal("1.75"), disponible: true },
    { codigo: "P006", nombre: "Café americano",          categoria: "Bebidas", descripcion: "Taza de 8 oz",          precio: NumberDecimal("1.50"), disponible: true },
    { codigo: "P007", nombre: "Cerveza nacional",        categoria: "Bebidas", descripcion: "Botella",               precio: NumberDecimal("2.50"), disponible: true },
    { codigo: "P008", nombre: "Pastel de tres leches",   categoria: "Postres", descripcion: "Porción",               precio: NumberDecimal("3.25"), disponible: false }
  ]);

  const hace = ms => new Date(Date.now() - ms);
  const MIN = 60 * 1000, HORA = 60 * MIN, DIA = 24 * HORA;
  const linea = (codigoProducto, nombreProducto, precio, cantidad) =>
    ({ codigoProducto, nombreProducto, precioUnitario: NumberDecimal(precio), cantidad });

  bd.pedidos.insertMany([
    { codigo: "PED-0001", fecha: hace(3 * DIA), codigoCliente: "C001", codigoMesa: "M01", estado: "COMPLETADO",
      detalles: [linea("P001", "Pupusa revuelta", "1.25", 3), linea("P005", "Refresco natural", "1.75", 2)],
      total: NumberDecimal("7.25") },
    { codigo: "PED-0002", fecha: hace(2 * HORA), codigoCliente: "C002", codigoMesa: "M02", estado: "EN_PREPARACION",
      detalles: [linea("P003", "Pollo asado con ensalada", "7.50", 2), linea("P007", "Cerveza nacional", "2.50", 2)],
      total: NumberDecimal("20.00") },
    { codigo: "PED-0003", fecha: hace(1 * HORA), codigoCliente: null, codigoMesa: "M03", estado: "ABIERTO",
      detalles: [linea("P004", "Hamburguesa clásica", "6.00", 1), linea("P006", "Café americano", "1.50", 1)],
      total: NumberDecimal("7.50") },
    { codigo: "PED-0004", fecha: hace(1 * DIA), codigoCliente: "C003", codigoMesa: null, estado: "COMPLETADO",
      detalles: [linea("P002", "Pupusa de queso", "1.25", 4), linea("P005", "Refresco natural", "1.75", 1)],
      total: NumberDecimal("6.75") },
    { codigo: "PED-0005", fecha: hace(2 * DIA), codigoCliente: "C004", codigoMesa: "M04", estado: "CANCELADO",
      detalles: [linea("P004", "Hamburguesa clásica", "6.00", 2)],
      total: NumberDecimal("12.00") },
    { codigo: "PED-0006", fecha: hace(20 * MIN), codigoCliente: null, codigoMesa: null, estado: "ABIERTO",
      detalles: [linea("P006", "Café americano", "1.50", 2)],
      total: NumberDecimal("3.00") }
  ]);

  bd.contadores.updateOne({ _id: "pedidos" }, { $set: { valor: 6 } }, { upsert: true });

  print("Base de datos inicializada correctamente.");
}
