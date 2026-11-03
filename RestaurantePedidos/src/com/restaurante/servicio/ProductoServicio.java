package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Producto;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.persistencia.ProductoRepositorio;
import com.restaurante.util.Validador;

import java.math.BigDecimal;
import java.util.List;

public class ProductoServicio {

    private final ProductoRepositorio productos = new ProductoRepositorio();
    private final PedidoRepositorio pedidos = new PedidoRepositorio();

    public Producto registrar(String codigo, String nombre, String categoria, String descripcion,
                              String precio, boolean disponible) {
        Sesion.exigirAutenticado();
        String cod = Validador.codigo("Código", codigo);
        String nom = Validador.requerido("Nombre", nombre);
        String cat = Validador.requerido("Categoría", categoria);
        BigDecimal monto = Validador.montoNoNegativo("Precio unitario", precio);
        if (productos.existe(cod)) {
            throw new ValidacionException("Ya existe un producto con el código " + cod + ".");
        }
        Producto producto = new Producto(cod, nom, cat, limpiarDescripcion(descripcion), monto, disponible);
        productos.insertar(producto);
        return producto;
    }

    public void modificar(String codigo, String nombre, String categoria, String descripcion,
                          String precio, boolean disponible) {
        Sesion.exigirAutenticado();
        Producto producto = obtener(codigo);
        producto.setNombre(Validador.requerido("Nombre", nombre));
        producto.setCategoria(Validador.requerido("Categoría", categoria));
        producto.setDescripcion(limpiarDescripcion(descripcion));
        producto.setPrecio(Validador.montoNoNegativo("Precio unitario", precio));
        producto.setDisponible(disponible);
        productos.actualizar(producto);
    }

    public void cambiarDisponibilidad(String codigo, boolean disponible) {
        Sesion.exigirAutenticado();
        Producto producto = obtener(codigo);
        producto.setDisponible(disponible);
        productos.actualizar(producto);
    }

    public void eliminar(String codigo) {
        Sesion.exigirAutenticado();
        Producto producto = obtener(codigo);
        if (pedidos.existenConProducto(producto.getCodigo())) {
            throw new ValidacionException("No se puede eliminar el producto porque forma parte de pedidos registrados. "
                    + "Márquelo como no disponible para que no se use en pedidos nuevos.");
        }
        productos.eliminar(producto.getCodigo());
    }

    public Producto obtener(String codigo) {
        Producto producto = null;
        if (codigo != null) {
            producto = productos.buscarPorCodigo(codigo.trim());
        }
        if (producto == null) {
            throw new ValidacionException("El producto " + codigo + " no existe.");
        }
        return producto;
    }

    public List<Producto> listar() {
        Sesion.exigirAutenticado();
        return productos.listar();
    }

    public List<Producto> listarDisponibles() {
        Sesion.exigirAutenticado();
        return productos.listarDisponibles();
    }

    public List<Producto> buscar(String texto) {
        Sesion.exigirAutenticado();
        if (texto == null || texto.isBlank()) {
            return productos.listar();
        }
        return productos.buscar(texto.trim());
    }

    private String limpiarDescripcion(String descripcion) {
        if (descripcion == null) {
            return "";
        }
        return descripcion.trim();
    }
}
