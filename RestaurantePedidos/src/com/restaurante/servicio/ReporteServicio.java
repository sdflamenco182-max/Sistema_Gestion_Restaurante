package com.restaurante.servicio;

import com.restaurante.modelo.EstadoPedido;
import com.restaurante.modelo.Pedido;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.util.Dinero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Arma los reportes de pedidos
public class ReporteServicio {

    // Guarda cuántos pedidos hay en un estado y cuánto suman
    public static class ResumenEstado {

        private final EstadoPedido estado;
        private final int cantidad;
        private final BigDecimal importe;

        public ResumenEstado(EstadoPedido estado, int cantidad, BigDecimal importe) {
            this.estado = estado;
            this.cantidad = cantidad;
            this.importe = importe;
        }

        public EstadoPedido getEstado() {
            return estado;
        }

        public int getCantidad() {
            return cantidad;
        }

        public BigDecimal getImporte() {
            return importe;
        }
    }

    private final PedidoRepositorio pedidos = new PedidoRepositorio();

    // Devuelve un resumen (cantidad e importe) por cada estado de pedido
    public List<ResumenEstado> resumenPorEstado() {
        Sesion.exigirAutenticado();
        List<Pedido> todos = pedidos.listar();
        List<ResumenEstado> resumen = new ArrayList<>();
        for (EstadoPedido estado : EstadoPedido.values()) {
            resumen.add(resumirEstado(estado, todos));
        }
        return resumen;
    }

    // Cuenta los pedidos de un estado y suma sus totales
    private ResumenEstado resumirEstado(EstadoPedido estado, List<Pedido> todos) {
        int cantidad = 0;
        BigDecimal importe = Dinero.CERO;
        for (Pedido p : todos) {
            if (p.getEstado() == estado) {
                cantidad++;
                importe = importe.add(p.calcularTotal());
            }
        }
        return new ResumenEstado(estado, cantidad, Dinero.redondear(importe));
    }
}
