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

public class PedidoServicio {

    private final PedidoRepositorio pedidos = new PedidoRepositorio();
    private final ClienteRepositorio clientes = new ClienteRepositorio();
    private final MesaRepositorio mesas = new MesaRepositorio();
    private final ProductoRepositorio productos = new ProductoRepositorio();

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

    public Pedido modificarCantidad(String codigoPedido, String codigoProducto, String cantidad) {
        Sesion.exigirAutenticado();
        int cant = Validador.enteroPositivo("Cantidad", cantidad);
        Pedido pedido = obtener(codigoPedido);
        pedido.modificarCantidad(codigoProducto, cant);
        guardar(pedido);
        return pedido;
    }

    public Pedido quitarProducto(String codigoPedido, String codigoProducto) {
        Sesion.exigirAutenticado();
        Pedido pedido = obtener(codigoPedido);
        pedido.quitarProducto(codigoProducto);
        guardar(pedido);
        return pedido;
    }

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

    public List<Pedido> listarActivos() {
        Sesion.exigirAutenticado();
        return pedidos.listarActivos();
    }

    public List<Pedido> listarTodos() {
        Sesion.exigirAutenticado();
        return pedidos.listar();
    }

    public List<Pedido> consultarPorEstado(EstadoPedido estado) {
        Sesion.exigirAutenticado();
        if (estado == null) {
            throw new ValidacionException("Seleccione un estado.");
        }
        return pedidos.porEstado(estado);
    }

    public List<Pedido> consultarPorMesa(String codigoMesa) {
        Sesion.exigirAutenticado();
        if (codigoMesa == null || codigoMesa.isBlank()) {
            throw new ValidacionException("Seleccione una mesa.");
        }
        return pedidos.porMesa(codigoMesa.trim());
    }

    public List<Pedido> consultarPorFecha(String desde, String hasta) {
        Sesion.exigirAutenticado();
        LocalDate inicio = Validador.fecha("Fecha desde", desde);
        LocalDate fin = Validador.fecha("Fecha hasta", hasta);
        if (fin.isBefore(inicio)) {
            throw new ValidacionException("La fecha \"hasta\" no puede ser anterior a la fecha \"desde\".");
        }
        return pedidos.porRangoFecha(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());
    }

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

    private void ocuparMesaOEliminarPedido(Mesa mesa, Pedido pedido) {
        try {
            mesa.setEstado(EstadoMesa.OCUPADA);
            mesas.actualizar(mesa);
        } catch (RuntimeException e) {
            pedidos.eliminar(pedido.getCodigo());
            throw e;
        }
    }

    private void liberarMesa(String codigoMesa) {
        Mesa mesa = mesas.buscarPorCodigo(codigoMesa);
        if (mesa != null) {
            mesa.setEstado(EstadoMesa.DISPONIBLE);
            mesas.actualizar(mesa);
        }
    }

    private void guardar(Pedido pedido) {
        if (!pedidos.actualizar(pedido)) {
            throw new ValidacionException("El pedido " + pedido.getCodigo() + " ya no existe.");
        }
    }

    private static String vacioANulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
