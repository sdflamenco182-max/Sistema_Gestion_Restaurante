package com.restaurante.servicio;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.EstadoMesa;
import com.restaurante.modelo.Mesa;
import com.restaurante.persistencia.MesaRepositorio;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.util.Validador;

import java.util.ArrayList;
import java.util.List;

public class MesaServicio {

    private final MesaRepositorio mesas = new MesaRepositorio();
    private final PedidoRepositorio pedidos = new PedidoRepositorio();

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

    public void eliminar(String codigo) {
        Sesion.exigirAutenticado();
        Mesa mesa = obtener(codigo);
        if (pedidos.existenActivosDeMesa(mesa.getCodigo())) {
            throw new ValidacionException("No se puede eliminar la mesa: tiene pedidos activos asociados.");
        }
        mesas.eliminar(mesa.getCodigo());
    }

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

    public List<Mesa> listar() {
        Sesion.exigirAutenticado();
        return mesas.listar();
    }

    public List<Mesa> buscar(String texto) {
        Sesion.exigirAutenticado();
        if (texto == null || texto.isBlank()) {
            return mesas.listar();
        }
        return mesas.buscar(texto.trim());
    }

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
