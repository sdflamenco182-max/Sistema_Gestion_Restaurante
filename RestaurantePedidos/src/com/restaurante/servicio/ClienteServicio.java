package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Cliente;
import com.restaurante.persistencia.ClienteRepositorio;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.util.Validador;

import java.util.ArrayList;
import java.util.List;

public class ClienteServicio {

    private final ClienteRepositorio clientes = new ClienteRepositorio();
    private final PedidoRepositorio pedidos = new PedidoRepositorio();

    public Cliente registrar(String codigo, String nombre, String telefono, String correo) {
        Sesion.exigirAutenticado();
        String cod = Validador.codigo("Código", codigo);
        String nom = Validador.requerido("Nombre", nombre);
        String tel = Validador.telefonoOpcional(telefono);
        String cor = Validador.correoOpcional(correo);
        if (clientes.existe(cod)) {
            throw new ValidacionException("Ya existe un cliente con el código " + cod + ".");
        }
        Cliente cliente = new Cliente(cod, nom, tel, cor, true);
        clientes.insertar(cliente);
        return cliente;
    }

    public void modificar(String codigo, String nombre, String telefono, String correo, boolean activo) {
        Sesion.exigirAutenticado();
        Cliente cliente = obtener(codigo);
        cliente.setNombre(Validador.requerido("Nombre", nombre));
        cliente.setTelefono(Validador.telefonoOpcional(telefono));
        cliente.setCorreo(Validador.correoOpcional(correo));
        cliente.setActivo(activo);
        if (!clientes.actualizar(cliente)) {
            throw new ValidacionException("El cliente " + codigo + " ya no existe.");
        }
    }

    public ResultadoEliminacion eliminar(String codigo) {
        Sesion.exigirAutenticado();
        Cliente cliente = obtener(codigo);
        if (pedidos.existenDeCliente(cliente.getCodigo())) {
            cliente.setActivo(false);
            clientes.actualizar(cliente);
            return ResultadoEliminacion.INACTIVADO;
        }
        clientes.eliminar(cliente.getCodigo());
        return ResultadoEliminacion.ELIMINADO;
    }

    public Cliente obtener(String codigo) {
        Cliente cliente = null;
        if (codigo != null) {
            cliente = clientes.buscarPorCodigo(codigo.trim());
        }
        if (cliente == null) {
            throw new ValidacionException("El cliente " + codigo + " no existe.");
        }
        return cliente;
    }

    public List<Cliente> listar() {
        Sesion.exigirAutenticado();
        return clientes.listar();
    }

    public List<Cliente> buscar(String texto) {
        Sesion.exigirAutenticado();
        if (texto == null || texto.isBlank()) {
            return clientes.listar();
        }
        return clientes.buscar(texto.trim());
    }

    public List<Cliente> listarActivos() {
        List<Cliente> activos = new ArrayList<>();
        for (Cliente cliente : listar()) {
            if (cliente.isActivo()) {
                activos.add(cliente);
            }
        }
        return activos;
    }
}
