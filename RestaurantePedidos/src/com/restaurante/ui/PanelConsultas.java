package com.restaurante.ui;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.EstadoPedido;
import com.restaurante.modelo.Mesa;
import com.restaurante.modelo.Pedido;
import com.restaurante.modelo.Producto;
import com.restaurante.servicio.MesaServicio;
import com.restaurante.servicio.PedidoServicio;
import com.restaurante.servicio.ProductoServicio;
import com.restaurante.servicio.ReporteServicio;
import com.restaurante.servicio.ReporteServicio.ResumenEstado;
import com.restaurante.util.Dinero;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class PanelConsultas extends JPanel implements Refrescable {

    private static final String POR_CODIGO = "Pedido por código";
    private static final String POR_MESA = "Pedidos por mesa";
    private static final String POR_ESTADO = "Pedidos por estado";
    private static final String POR_FECHA = "Pedidos por fecha";
    private static final String PRODUCTOS = "Listado de productos y disponibilidad";
    private static final String RESUMEN = "Resumen de pedidos";

    private final PedidoServicio pedidos = new PedidoServicio();
    private final MesaServicio mesas = new MesaServicio();
    private final ProductoServicio productos = new ProductoServicio();
    private final ReporteServicio reportes = new ReporteServicio();
    private final CatalogoNombres nombres = new CatalogoNombres();

    private final JComboBox<String> cmbTipo =
            new JComboBox<>(new String[]{POR_CODIGO, POR_MESA, POR_ESTADO, POR_FECHA, PRODUCTOS, RESUMEN});
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel criterios = new JPanel(tarjetas);
    private final JTextField txtCodigo = new JTextField("PED-", 10);
    private final JComboBox<Mesa> cmbMesa = new JComboBox<>();
    private final JComboBox<EstadoPedido> cmbEstado = new JComboBox<>(EstadoPedido.values());
    private final JTextField txtDesde = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField txtHasta = new JTextField(LocalDate.now().toString(), 10);
    private final JButton btnConsultar = new JButton("Consultar");
    private final JButton btnDetalle = new JButton("Ver detalle del pedido");
    private final JTable tabla = new JTable();
    private final JLabel lblResultado = new JLabel(" ");
    private List<Pedido> pedidosMostrados = new ArrayList<>();

    PanelConsultas() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        crearTarjetasDeCriterios();

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lblResultado.setFont(lblResultado.getFont().deriveFont(Font.BOLD));
        btnDetalle.setEnabled(false);

        add(crearPanelConsulta(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblResultado, BorderLayout.SOUTH);

        conectarEventos();
        tarjetas.show(criterios, POR_CODIGO);
    }

    private void crearTarjetasDeCriterios() {
        criterios.add(fila(new JLabel("Código del pedido:"), txtCodigo), POR_CODIGO);
        criterios.add(fila(new JLabel("Mesa:"), cmbMesa), POR_MESA);
        criterios.add(fila(new JLabel("Estado:"), cmbEstado), POR_ESTADO);
        criterios.add(fila(new JLabel("Desde (AAAA-MM-DD):"), txtDesde, new JLabel("Hasta:"), txtHasta), POR_FECHA);
        criterios.add(fila(new JLabel("No requiere criterios.")), PRODUCTOS);
        criterios.add(fila(new JLabel("No requiere criterios.")), RESUMEN);
    }

    private JPanel crearPanelConsulta() {
        JPanel norte = UiUtil.panelFlujo(new JLabel("Tipo:"), cmbTipo, criterios, btnConsultar, btnDetalle);
        norte.setBorder(UiUtil.titulo("Consulta"));
        return norte;
    }

    private void conectarEventos() {
        cmbTipo.addActionListener(e -> cambiarTipoDeConsulta());
        btnConsultar.addActionListener(e -> UiUtil.ejecutar(this, () -> consultar()));
        btnDetalle.addActionListener(e -> UiUtil.ejecutar(this, () -> verDetalle()));
    }

    private void cambiarTipoDeConsulta() {
        tarjetas.show(criterios, (String) cmbTipo.getSelectedItem());
        limpiarResultados();
    }

    private static JPanel fila(Component... componentes) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (Component c : componentes) {
            panel.add(c);
        }
        return panel;
    }

    @Override
    public void refrescar() {
        nombres.recargar();
        cmbMesa.setModel(new DefaultComboBoxModel<>(mesas.listar().toArray(new Mesa[0])));
    }

    private void consultar() {
        String tipo = (String) cmbTipo.getSelectedItem();
        if (tipo.equals(POR_CODIGO)) {
            consultarPorCodigo();
        } else if (tipo.equals(POR_MESA)) {
            consultarPorMesa();
        } else if (tipo.equals(POR_ESTADO)) {
            mostrarPedidos(pedidos.consultarPorEstado((EstadoPedido) cmbEstado.getSelectedItem()));
        } else if (tipo.equals(POR_FECHA)) {
            mostrarPedidos(pedidos.consultarPorFecha(txtDesde.getText(), txtHasta.getText()));
        } else if (tipo.equals(PRODUCTOS)) {
            mostrarProductos(productos.listar());
        } else if (tipo.equals(RESUMEN)) {
            mostrarResumen(reportes.resumenPorEstado());
        } else {
            throw new IllegalStateException("Tipo de consulta desconocido: " + tipo);
        }
    }

    private void consultarPorCodigo() {
        List<Pedido> encontrados = new ArrayList<>();
        encontrados.add(pedidos.obtener(txtCodigo.getText()));
        mostrarPedidos(encontrados);
    }

    private void consultarPorMesa() {
        Mesa mesa = (Mesa) cmbMesa.getSelectedItem();
        if (mesa == null) {
            throw new ValidacionException("No hay mesas registradas.");
        }
        mostrarPedidos(pedidos.consultarPorMesa(mesa.getCodigo()));
    }

    private void mostrarPedidos(List<Pedido> lista) {
        pedidosMostrados = new ArrayList<>(lista);
        DefaultTableModel modelo = UiUtil.modelo("Código", "Fecha", "Cliente", "Mesa", "Estado", "Productos", "Total");
        for (Pedido p : lista) {
            modelo.addRow(new Object[]{p.getCodigo(), p.getFecha().format(CatalogoNombres.FORMATO_FECHA),
                    nombres.cliente(p.getCodigoCliente()), nombres.mesa(p.getCodigoMesa()), p.getEstado(),
                    p.getDetalles().size(), Dinero.formato(p.calcularTotal())});
        }
        tabla.setModel(modelo);
        btnDetalle.setEnabled(!lista.isEmpty());
        if (lista.isEmpty()) {
            lblResultado.setText("La consulta no produjo resultados.");
        } else {
            lblResultado.setText(lista.size() + " pedido(s) encontrado(s).");
        }
    }

    private void mostrarProductos(List<Producto> lista) {
        limpiarResultados();
        DefaultTableModel modelo = UiUtil.modelo("Código", "Nombre", "Categoría", "Precio", "Disponible");
        for (Producto p : lista) {
            String disponible = p.isDisponible() ? "Sí" : "No";
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(),
                    Dinero.formato(p.getPrecio()), disponible});
        }
        tabla.setModel(modelo);
        if (lista.isEmpty()) {
            lblResultado.setText("No hay productos registrados.");
        } else {
            lblResultado.setText(lista.size() + " producto(s) en el catálogo.");
        }
    }

    private void mostrarResumen(List<ResumenEstado> resumen) {
        limpiarResultados();
        DefaultTableModel modelo = UiUtil.modelo("Estado", "Cantidad de pedidos", "Importe total");
        int cantidadTotal = 0;
        BigDecimal importeTotal = Dinero.CERO;
        for (ResumenEstado r : resumen) {
            modelo.addRow(new Object[]{r.estado(), r.cantidad(), Dinero.formato(r.importe())});
            cantidadTotal += r.cantidad();
            if (r.estado() != EstadoPedido.CANCELADO) {
                importeTotal = importeTotal.add(r.importe());
            }
        }
        modelo.addRow(new Object[]{"TOTAL (importe sin cancelados)", cantidadTotal, Dinero.formato(importeTotal)});
        tabla.setModel(modelo);
        if (cantidadTotal == 0) {
            lblResultado.setText("No hay pedidos registrados.");
        } else {
            lblResultado.setText("Resumen generado a partir de los pedidos almacenados.");
        }
    }

    private void verDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            throw new ValidacionException("Seleccione un pedido de la tabla.");
        }
        JTextArea texto = new JTextArea(nombres.describir(pedidosMostrados.get(fila)));
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(texto), "Detalle del pedido",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void limpiarResultados() {
        pedidosMostrados = new ArrayList<>();
        btnDetalle.setEnabled(false);
        tabla.setModel(new DefaultTableModel());
        lblResultado.setText(" ");
    }
}
