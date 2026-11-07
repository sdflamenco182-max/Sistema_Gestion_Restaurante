package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Cliente;
import com.restaurante.persistencia.ClienteRepositorio;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.util.Validador;

import java.util.ArrayList;
import java.util.List;

// Reglas de negocio de los clientes: registrar, modificar, eliminar y consultar
public class ClienteServicio {

    private final ClienteRepositorio clientes = new ClienteRepositorio();
    private final PedidoRepositorio pedidos = new PedidoRepositorio();

    // Valida los datos y guarda un cliente nuevo; no deja repetir el código
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

    // Cambia los datos de un cliente existente después de validarlos
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

    // Si el cliente tiene pedidos lo deja inactivo; si no, lo borra
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

    // Busca un cliente por código; si no existe lanza ValidacionException
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

    // Devuelve todos los clientes
    public List<Cliente> listar() {
        Sesion.exigirAutenticado();
        return clientes.listar();
    }

    // Busca por código o nombre; si el texto viene vacío devuelve todos
    public List<Cliente> buscar(String texto) {
        Sesion.exigirAutenticado();
        if (texto == null || texto.isBlank()) {
            return clientes.listar();
        }
        return clientes.buscar(texto.trim());
    }

    // Devuelve solo los clientes activos
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
