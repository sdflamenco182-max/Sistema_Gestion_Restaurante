package com.restaurante.servicio;

import com.restaurante.modelo.Cliente;
import com.restaurante.modelo.EstadoMesa;
import com.restaurante.modelo.EstadoPedido;
import com.restaurante.modelo.Mesa;
import com.restaurante.modelo.Pedido;
import com.restaurante.modelo.Producto;
import com.restaurante.modelo.Usuario;
import com.restaurante.persistencia.ClienteRepositorio;
import com.restaurante.persistencia.MesaRepositorio;
import com.restaurante.persistencia.MongoConexion;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.persistencia.ProductoRepositorio;
import com.restaurante.persistencia.UsuarioRepositorio;
import com.restaurante.util.PasswordHasher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class InicializadorBD {

    public static final String USUARIO_PRUEBA = "admin";
    public static final String CLAVE_PRUEBA = "Admin123";

    private InicializadorBD() {
    }

    public static boolean ejecutar() {
        MongoConexion.obtener().verificar();
        MongoConexion.obtener().crearIndices();

        UsuarioRepositorio usuarios = new UsuarioRepositorio();
        if (usuarios.contar() > 0) {
            return false;
        }
        crearUsuario(usuarios);
        crearClientes();
        crearMesas();
        List<Producto> productos = crearProductos();
        crearPedidos(productos);
        return true;
    }

    private static void crearUsuario(UsuarioRepositorio usuarios) {
        usuarios.insertar(new Usuario(USUARIO_PRUEBA, "Administrador del sistema",
                PasswordHasher.hash(CLAVE_PRUEBA.toCharArray()), true));
    }

    private static void crearClientes() {
        ClienteRepositorio clientes = new ClienteRepositorio();
        clientes.insertar(new Cliente("C001", "Ana Martínez", "7000-1111", "ana.martinez@correo.com", true));
        clientes.insertar(new Cliente("C002", "Carlos Hernández", "7000-2222", "carlos.h@correo.com", true));
        clientes.insertar(new Cliente("C003", "María López", "7000-3333", "maria.lopez@correo.com", true));
        clientes.insertar(new Cliente("C004", "José Ramírez", "7000-4444", "", true));
        clientes.insertar(new Cliente("C005", "Lucía Flores", "7000-5555", "lucia.flores@correo.com", true));
        clientes.insertar(new Cliente("C006", "Pedro Castillo", "7000-6666", "pedro.c@correo.com", true));
    }

    private static void crearMesas() {
        MesaRepositorio mesas = new MesaRepositorio();
        mesas.insertar(new Mesa("M01", 1, 2, EstadoMesa.DISPONIBLE));
        mesas.insertar(new Mesa("M02", 2, 4, EstadoMesa.OCUPADA));
        mesas.insertar(new Mesa("M03", 3, 4, EstadoMesa.OCUPADA));
        mesas.insertar(new Mesa("M04", 4, 6, EstadoMesa.DISPONIBLE));
        mesas.insertar(new Mesa("M05", 5, 6, EstadoMesa.DISPONIBLE));
        mesas.insertar(new Mesa("M06", 6, 8, EstadoMesa.DISPONIBLE));
    }

    private static List<Producto> crearProductos() {
        List<Producto> lista = new ArrayList<>();
        lista.add(new Producto("P001", "Pupusa revuelta", "Comida", "Chicharrón y frijol", new BigDecimal("1.25"), true));
        lista.add(new Producto("P002", "Pupusa de queso", "Comida", "Con loroco opcional", new BigDecimal("1.25"), true));
        lista.add(new Producto("P003", "Pollo asado con ensalada", "Comida", "Cuarto de pollo", new BigDecimal("7.50"), true));
        lista.add(new Producto("P004", "Hamburguesa clásica", "Comida", "Con papas fritas", new BigDecimal("6.00"), true));
        lista.add(new Producto("P005", "Refresco natural", "Bebidas", "Vaso de 12 oz", new BigDecimal("1.75"), true));
        lista.add(new Producto("P006", "Café americano", "Bebidas", "Taza de 8 oz", new BigDecimal("1.50"), true));
        lista.add(new Producto("P007", "Cerveza nacional", "Bebidas", "Botella", new BigDecimal("2.50"), true));
        lista.add(new Producto("P008", "Pastel de tres leches", "Postres", "Porción", new BigDecimal("3.25"), false));

        ProductoRepositorio repositorio = new ProductoRepositorio();
        for (Producto p : lista) {
            repositorio.insertar(p);
        }
        return lista;
    }

    private static void crearPedidos(List<Producto> productos) {
        Producto p001 = productos.get(0);
        Producto p002 = productos.get(1);
        Producto p003 = productos.get(2);
        Producto p004 = productos.get(3);
        Producto p005 = productos.get(4);
        Producto p006 = productos.get(5);
        Producto p007 = productos.get(6);

        PedidoRepositorio pedidos = new PedidoRepositorio();
        LocalDateTime ahora = LocalDateTime.now().withNano(0);

        Pedido ped1 = new Pedido(pedidos.siguienteCodigo(), ahora.minusDays(3), "C001", "M01");
        ped1.agregarProducto(p001, 3);
        ped1.agregarProducto(p005, 2);
        ped1.cambiarEstado(EstadoPedido.EN_PREPARACION);
        ped1.cambiarEstado(EstadoPedido.COMPLETADO);

        Pedido ped2 = new Pedido(pedidos.siguienteCodigo(), ahora.minusHours(2), "C002", "M02");
        ped2.agregarProducto(p003, 2);
        ped2.agregarProducto(p007, 2);
        ped2.cambiarEstado(EstadoPedido.EN_PREPARACION);

        Pedido ped3 = new Pedido(pedidos.siguienteCodigo(), ahora.minusHours(1), null, "M03");
        ped3.agregarProducto(p004, 1);
        ped3.agregarProducto(p006, 1);

        Pedido ped4 = new Pedido(pedidos.siguienteCodigo(), ahora.minusDays(1), "C003", null);
        ped4.agregarProducto(p002, 4);
        ped4.agregarProducto(p005, 1);
        ped4.cambiarEstado(EstadoPedido.EN_PREPARACION);
        ped4.cambiarEstado(EstadoPedido.COMPLETADO);

        Pedido ped5 = new Pedido(pedidos.siguienteCodigo(), ahora.minusDays(2), "C004", "M04");
        ped5.agregarProducto(p004, 2);
        ped5.cambiarEstado(EstadoPedido.CANCELADO);

        Pedido ped6 = new Pedido(pedidos.siguienteCodigo(), ahora.minusMinutes(20), null, null);
        ped6.agregarProducto(p006, 2);

        Pedido[] todos = {ped1, ped2, ped3, ped4, ped5, ped6};
        for (Pedido p : todos) {
            pedidos.insertar(p);
        }
    }
}
