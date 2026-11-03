package com.restaurante.servicio;

import com.restaurante.modelo.EstadoPedido;
import com.restaurante.modelo.Pedido;
import com.restaurante.persistencia.PedidoRepositorio;
import com.restaurante.util.Dinero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReporteServicio {

    public record ResumenEstado(EstadoPedido estado, int cantidad, BigDecimal importe) {
    }

    private final PedidoRepositorio pedidos = new PedidoRepositorio();

    public List<ResumenEstado> resumenPorEstado() {
        Sesion.exigirAutenticado();
        List<Pedido> todos = pedidos.listar();
        List<ResumenEstado> resumen = new ArrayList<>();
        for (EstadoPedido estado : EstadoPedido.values()) {
            resumen.add(resumirEstado(estado, todos));
        }
        return resumen;
    }

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
