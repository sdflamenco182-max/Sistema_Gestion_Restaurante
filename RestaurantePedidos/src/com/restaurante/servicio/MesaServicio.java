package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.EstadoMesa;
import com.restaurante.modelo.Mesa;
import com.restaurante.persistencia.MesaRepositorio;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.util.Validador;

import java.util.ArrayList;
import java.util.List;

// Reglas de negocio de las mesas: registrar, modificar, cambiar estado, eliminar y consultar
public class MesaServicio {

    private final MesaRepositorio mesas = new MesaRepositorio();
    private final PedidoRepositorio pedidos = new PedidoRepositorio();

    // Guarda una mesa nueva; no deja repetir el código ni el número de mesa
    public Mesa registrar(String codigo, String numero, String capacidad, EstadoMesa estado) {
        Sesion.exigirAutenticado();
        String cod = Validador.codigo("Código", codigo);
        int num = Validador.enteroPositivo("Número de mesa", numero);
        int cap = Validador.enteroPositivo("Capacidad", capacidad);
        if (estado == null) {
            throw new ValidacionException("Seleccione el estado de la mesa.");
        }
        if (mesas.existe(cod)) {
            throw new ValidacionException("Ya existe una mesa con el código " + cod + ".");
        }
        if (mesas.buscarPorNumero(num) != null) {
            throw new ValidacionException("Ya existe una mesa con el número " + num + ".");
        }
        Mesa mesa = new Mesa(cod, num, cap, estado);
        mesas.insertar(mesa);
        return mesa;
    }

    // Cambia número y capacidad de la mesa; el número no puede ser el de otra mesa
    public void modificar(String codigo, String numero, String capacidad) {
        Sesion.exigirAutenticado();
        Mesa mesa = obtener(codigo);
        int num = Validador.enteroPositivo("Número de mesa", numero);
        int cap = Validador.enteroPositivo("Capacidad", capacidad);
        Mesa conMismoNumero = mesas.buscarPorNumero(num);
        if (conMismoNumero != null && !conMismoNumero.getCodigo().equals(mesa.getCodigo())) {
            throw new ValidacionException("Ya existe otra mesa con el número " + num + ".");
        }
        mesa.setNumero(num);
        mesa.setCapacidad(cap);
        mesas.actualizar(mesa);
    }

    // Cambia el estado de la mesa; no la deja disponible si tiene un pedido activo
    public void cambiarEstado(String codigo, EstadoMesa estado) {
        Sesion.exigirAutenticado();
        Mesa mesa = obtener(codigo);
        if (estado == null) {
            throw new ValidacionException("Seleccione el estado de la mesa.");
        }
        if (estado == EstadoMesa.DISPONIBLE && pedidos.existenActivosDeMesa(mesa.getCodigo())) {
            throw new ValidacionException("La mesa tiene un pedido activo; complete o cancele el pedido para liberarla.");
        }
        mesa.setEstado(estado);
        mesas.actualizar(mesa);
    }

    // No deja eliminar la mesa si tiene pedidos activos
    public void eliminar(String codigo) {
        Sesion.exigirAutenticado();
        Mesa mesa = obtener(codigo);
        if (pedidos.existenActivosDeMesa(mesa.getCodigo())) {
            throw new ValidacionException("No se puede eliminar la mesa: tiene pedidos activos asociados.");
        }
        mesas.eliminar(mesa.getCodigo());
    }

    // Busca una mesa por código; si no existe lanza ValidacionException
    public Mesa obtener(String codigo) {
        Mesa mesa = null;
        if (codigo != null) {
            mesa = mesas.buscarPorCodigo(codigo.trim());
        }
        if (mesa == null) {
            throw new ValidacionException("La mesa " + codigo + " no existe.");
        }
        return mesa;
    }

    // Devuelve todas las mesas
    public List<Mesa> listar() {
        Sesion.exigirAutenticado();
        return mesas.listar();
    }

    // Busca por código o número; si el texto viene vacío devuelve todas
    public List<Mesa> buscar(String texto) {
        Sesion.exigirAutenticado();
        if (texto == null || texto.isBlank()) {
            return mesas.listar();
        }
        return mesas.buscar(texto.trim());
    }

    // Devuelve solo las mesas disponibles
    public List<Mesa> listarDisponibles() {
        List<Mesa> disponibles = new ArrayList<>();
        for (Mesa mesa : listar()) {
            if (mesa.estaDisponible()) {
                disponibles.add(mesa);
            }
        }
        return disponibles;
    }
}
