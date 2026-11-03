package controlador;

import modelo.Producto;
import java.util.ArrayList;
import java.util.List;

public class GestorProductos {
    private List<Producto> listaProductos;

    public GestorProductos() {
        this.listaProductos = new ArrayList<>();
    }
    
    // Registrar producto
    public boolean registrarProducto(String codigo, String nombre, String categoria, String descripcion, double precio) {
        if (buscarPorCodigo(codigo) != null) {
            return false;
        }
        Producto nuevo = new Producto(codigo, nombre, categoria, descripcion, precio);
        listaProductos.add(nuevo);
        return true;
    }
    
    // Buscar producto por código
    public Producto buscarPorCodigo(String codigo) {
        for (Producto p : listaProductos) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                return p;
            }
        }
        return null;
    }
    
    // Buscar producto por nombre
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> encontrados = new ArrayList<>();
        for (Producto p : listaProductos) {
            if (p.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                encontrados.add(p);
            }
        }
        return encontrados;
    }
    
    // Consultar productos disponibles
    public List<Producto> consultarProductos() {
        return listaProductos;
    }
    
    // Modificar producto
    public boolean modificarProducto(String codigo, String nuevoNombre, String nuevaCategoria, String nuevaDescripcion, double nuevoPrecio) {
        Producto p = buscarPorCodigo(codigo);
        if (p != null) {
            p.setNombre(nuevoNombre);
            p.setCategoria(nuevaCategoria);
            p.setDescripcion(nuevaDescripcion);
            p.setPrecioUnitario(nuevoPrecio);
            return true;
        }
        return false;
    }
    
    // Cambiar disponibilidad
    public boolean cambiarDisponibilidad(String codigo) {
        Producto p = buscarPorCodigo(codigo);
        if (p != null) {
            p.setDisponible(!p.isDisponible());
            return true;
        }
        return false;
    }

    // Eliminar producto con validación de restricciones
    public boolean eliminarProducto(String codigo, boolean tieneRestriccionPedidos) {
        if (tieneRestriccionPedidos) {
            return false; 
        }
        Producto p = buscarPorCodigo(codigo);
        if (p != null) {
            listaProductos.remove(p);
            return true;
        }
        return false;
    }
}