package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Cliente;
import com.restaurante.modelo.EstadoMesa;
import com.restaurante.modelo.EstadoPedido;
import com.restaurante.modelo.Mesa;
import com.restaurante.modelo.Pedido;
import com.restaurante.modelo.Producto;
import com.restaurante.persistencia.ClienteRepositorio;
import com.restaurante.persistencia.MesaRepositorio;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.persistencia.ProductoRepositorio;
import com.restaurante.util.Validador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Reglas de negocio de los pedidos: crearlos, editar su detalle, cambiar estado y consultarlos
public class PedidoServicio {

    private final PedidoRepositorio pedidos = new PedidoRepositorio();
    private final ClienteRepositorio clientes = new ClienteRepositorio();
    private final MesaRepositorio mesas = new MesaRepositorio();
    private final ProductoRepositorio productos = new ProductoRepositorio();

    // Crea un pedido nuevo y ocupa la mesa si se eligió una
    public Pedido crear(String codigoCliente, String codigoMesa) {
        Sesion.exigirAutenticado();
        String cliente = vacioANulo(codigoCliente);
        String mesaCodigo = vacioANulo(codigoMesa);

        validarClienteActivo(cliente);
        Mesa mesa = buscarMesaDisponible(mesaCodigo);

        Pedido pedido = new Pedido(pedidos.siguienteCodigo(), LocalDateTime.now().withNano(0), cliente, mesaCodigo);
        pedidos.insertar(pedido);
        if (mesa != null) {
            ocuparMesaOEliminarPedido(mesa, pedido);
        }
        return pedido;
    }

    // Busca un pedido por código; si no existe lanza ValidacionException
    public Pedido obtener(String codigo) {
        Sesion.exigirAutenticado();
        Pedido pedido = null;
        if (codigo != null) {
            pedido = pedidos.buscarPorCodigo(codigo.trim());
        }
        if (pedido == null) {
            throw new ValidacionException("El pedido " + codigo + " no existe.");
        }
        return pedido;
    }

    // Agrega un producto con su cantidad al pedido y guarda el cambio
    public Pedido agregarProducto(String codigoPedido, String codigoProducto, String cantidad) {
        Sesion.exigirAutenticado();
        int cant = Validador.enteroPositivo("Cantidad", cantidad);
        Pedido pedido = obtener(codigoPedido);
        Producto producto = null;
        if (codigoProducto != null) {
            producto = productos.buscarPorCodigo(codigoProducto);
        }
        if (producto == null) {
            throw new ValidacionException("El producto seleccionado no existe.");
        }
        pedido.agregarProducto(producto, cant);
        guardar(pedido);
        return pedido;
    }

    // Cambia la cantidad de un producto que ya está en el pedido
    public Pedido modificarCantidad(String codigoPedido, String codigoProducto, String cantidad) {
        Sesion.exigirAutenticado();
        int cant = Validador.enteroPositivo("Cantidad", cantidad);
        Pedido pedido = obtener(codigoPedido);
        pedido.modificarCantidad(codigoProducto, cant);
        guardar(pedido);
        return pedido;
    }

    // Quita un producto del pedido
    public Pedido quitarProducto(String codigoPedido, String codigoProducto) {
        Sesion.exigirAutenticado();
        Pedido pedido = obtener(codigoPedido);
        pedido.quitarProducto(codigoProducto);
        guardar(pedido);
        return pedido;
    }

    // Cambia el estado del pedido y libera la mesa si el pedido ya terminó
    public Pedido cambiarEstado(String codigoPedido, EstadoPedido nuevoEstado) {
        Sesion.exigirAutenticado();
        Pedido pedido = obtener(codigoPedido);
        pedido.cambiarEstado(nuevoEstado);
        guardar(pedido);
        if (!pedido.getEstado().esActivo() && pedido.getCodigoMesa() != null) {
            liberarMesa(pedido.getCodigoMesa());
        }
        return pedido;
    }

    // Devuelve los pedidos que siguen activos
    public List<Pedido> listarActivos() {
        Sesion.exigirAutenticado();
        return pedidos.listarActivos();
    }

    // Devuelve todos los pedidos
    public List<Pedido> listarTodos() {
        Sesion.exigirAutenticado();
        return pedidos.listar();
    }

    // Devuelve los pedidos de un estado; exige elegir uno
    public List<Pedido> consultarPorEstado(EstadoPedido estado) {
        Sesion.exigirAutenticado();
        if (estado == null) {
            throw new ValidacionException("Seleccione un estado.");
        }
        return pedidos.porEstado(estado);
    }

    // Devuelve los pedidos de una mesa; exige elegir una
    public List<Pedido> consultarPorMesa(String codigoMesa) {
        Sesion.exigirAutenticado();
        if (codigoMesa == null || codigoMesa.isBlank()) {
            throw new ValidacionException("Seleccione una mesa.");
        }
        return pedidos.porMesa(codigoMesa.trim());
    }

    // Devuelve los pedidos entre dos fechas (la final incluida); "hasta" no puede ser menor
    public List<Pedido> consultarPorFecha(String desde, String hasta) {
        Sesion.exigirAutenticado();
        LocalDate inicio = Validador.fecha("Fecha desde", desde);
        LocalDate fin = Validador.fecha("Fecha hasta", hasta);
        if (fin.isBefore(inicio)) {
            throw new ValidacionException("La fecha \"hasta\" no puede ser anterior a la fecha \"desde\".");
        }
        return pedidos.porRangoFecha(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());
    }

    // Si hay cliente, revisa que exista y esté activo
    private void validarClienteActivo(String codigoCliente) {
        if (codigoCliente == null) {
            return;
        }
        Cliente cliente = clientes.buscarPorCodigo(codigoCliente);
        if (cliente == null) {
            throw new ValidacionException("El cliente " + codigoCliente + " no existe.");
        }
        if (!cliente.isActivo()) {
            throw new ValidacionException("El cliente " + codigoCliente + " está inactivo.");
        }
    }

    // Si hay mesa, revisa que exista y esté disponible; si no hay devuelve null
    private Mesa buscarMesaDisponible(String codigoMesa) {
        if (codigoMesa == null) {
            return null;
        }
        Mesa mesa = mesas.buscarPorCodigo(codigoMesa);
        if (mesa == null) {
            throw new ValidacionException("La mesa " + codigoMesa + " no existe.");
        }
        if (!mesa.estaDisponible()) {
            throw new ValidacionException("La mesa " + mesa.getNumero() + " no está disponible.");
        }
        return mesa;
    }

    // Pone la mesa como ocupada; si falla borra el pedido para no dejarlo a medias
    private void ocuparMesaOEliminarPedido(Mesa mesa, Pedido pedido) {
        try {
            mesa.setEstado(EstadoMesa.OCUPADA);
            mesas.actualizar(mesa);
        } catch (RuntimeException e) {
            pedidos.eliminar(pedido.getCodigo());
            throw e;
        }
    }

    // Deja la mesa como disponible otra vez
    private void liberarMesa(String codigoMesa) {
        Mesa mesa = mesas.buscarPorCodigo(codigoMesa);
        if (mesa != null) {
            mesa.setEstado(EstadoMesa.DISPONIBLE);
            mesas.actualizar(mesa);
        }
    }

    // Guarda los cambios del pedido; si ya no existe lanza ValidacionException
    private void guardar(Pedido pedido) {
        if (!pedidos.actualizar(pedido)) {
            throw new ValidacionException("El pedido " + pedido.getCodigo() + " ya no existe.");
        }
    }

    // Si el texto viene vacío devuelve null, si no lo devuelve sin espacios
    private static String vacioANulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
