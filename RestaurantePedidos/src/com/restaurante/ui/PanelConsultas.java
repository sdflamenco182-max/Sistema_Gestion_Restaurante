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

// Pantalla de consultas y reportes de pedidos, productos y resumen
class PanelConsultas extends JPanel implements Refrescable {

    // Nombres de los tipos de consulta
    private static final String POR_CODIGO = "Pedido por código";
    private static final String POR_MESA = "Pedidos por mesa";
    private static final String POR_ESTADO = "Pedidos por estado";
    private static final String POR_FECHA = "Pedidos por fecha";
    private static final String PRODUCTOS = "Listado de productos y disponibilidad";
    private static final String RESUMEN = "Resumen de pedidos";

    // Servicios y catálogo de nombres
    private final PedidoServicio pedidos = new PedidoServicio();
    private final MesaServicio mesas = new MesaServicio();
    private final ProductoServicio productos = new ProductoServicio();
    private final ReporteServicio reportes = new ReporteServicio();
    private final CatalogoNombres nombres = new CatalogoNombres();

    // Combo de tipo y paneles de criterios que cambian según el tipo
    private final JComboBox<String> cmbTipo =
            new JComboBox<>(new String[]{POR_CODIGO, POR_MESA, POR_ESTADO, POR_FECHA, PRODUCTOS, RESUMEN});
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel criterios = new JPanel(tarjetas);
    // Campos de los criterios (código, mesa, estado y fechas)
    private final JTextField txtCodigo =new JTextField("PED-", 10);
    private final JComboBox<Mesa> cmbMesa = new JComboBox<>();
    private final JComboBox<EstadoPedido> cmbEstado = new JComboBox<>(EstadoPedido.values());
    private final JTextField txtDesde = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField txtHasta = new JTextField(LocalDate.now().toString(), 10);
    // Botones, tabla de resultados y mensaje de abajo
    private final JButton btnConsultar =new JButton("Consultar");
    private final JButton btnDetalle = new JButton("Ver detalle del pedido");
    private final JTable tabla = new JTable();
    private final JLabel lblResultado = new JLabel(" ");
    // Pedidos que se ven ahora en la tabla
    private List<Pedido> pedidosMostrados = new ArrayList<>();

    // Arma la pantalla: consulta arriba, tabla al centro y mensaje abajo
    PanelConsultas() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // criterios de cada tipo de consulta
        crearTarjetasDeCriterios();

        // ajustes de la tabla y los componentes
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lblResultado.setFont(lblResultado.getFont().deriveFont(Font.BOLD));
        btnDetalle.setEnabled(false);

        // partes de la pantalla
        add(crearPanelConsulta(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblResultado, BorderLayout.SOUTH);

        // eventos y criterio inicial
        conectarEventos();
        tarjetas.show(criterios, POR_CODIGO);
    }

    // Crea un panel de criterios por cada tipo de consulta
    private void crearTarjetasDeCriterios() {
        criterios.add(fila(new JLabel("Código del pedido:"), txtCodigo), POR_CODIGO);
        criterios.add(fila(new JLabel("Mesa:"), cmbMesa), POR_MESA);
        criterios.add(fila(new JLabel("Estado:"), cmbEstado), POR_ESTADO);
        criterios.add(fila(new JLabel("Desde (AAAA-MM-DD):"), txtDesde, new JLabel("Hasta:"), txtHasta), POR_FECHA);
        criterios.add(fila(), PRODUCTOS);
        criterios.add(fila(), RESUMEN);
    }

    // Arma la parte de arriba: tipo, criterios y botones
    private JPanel crearPanelConsulta() {
        JPanel norte = UiUtil.panelFlujo(new JLabel("Tipo:"), cmbTipo, criterios, btnConsultar, btnDetalle);
        norte.setBorder(UiUtil.titulo("Consulta"));
        return norte;
    }

    // Conecta el combo de tipo y los botones Consultar y Ver detalle
    private void conectarEventos() {
        cmbTipo.addActionListener(e -> cambiarTipoDeConsulta());
        btnConsultar.addActionListener(e -> consultar());
        btnDetalle.addActionListener(e -> verDetalle());
    }

    // Al cambiar el tipo, muestra sus criterios y limpia la tabla
    private void cambiarTipoDeConsulta() {
        tarjetas.show(criterios, (String) cmbTipo.getSelectedItem());
        limpiarResultados();
    }

    // Pone los componentes en una fila pegada a la izquierda
    private static JPanel fila(Component... componentes) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (Component c : componentes) {
            panel.add(c);
        }
        return panel;
    }

    // Recarga los nombres y la lista de mesas del combo
    @Override
    public void refrescar() {
        nombres.recargar();
        cmbMesa.setModel(new DefaultComboBoxModel<>(mesas.listar().toArray(new Mesa[0])));
    }

    // Botón Consultar: ejecuta la consulta elegida y muestra errores si fallan
    private void consultar() {
        try {
            consultarSegunTipo();
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Llama al método que corresponde al tipo de consulta elegido
    private void consultarSegunTipo() {
        String tipo = (String) cmbTipo.getSelectedItem();
        if (tipo.equals(POR_CODIGO)) {
            consultarPedidoPorCodigo();
        } else if (tipo.equals(POR_MESA)) {
            consultarPedidosPorMesa();
        } else if (tipo.equals(POR_ESTADO)) {
            consultarPedidosPorEstado();
        } else if (tipo.equals(POR_FECHA)) {
            consultarPedidosPorFecha();
        } else if (tipo.equals(PRODUCTOS)) {
            mostrarListadoProductos();
        } else if (tipo.equals(RESUMEN)) {
            mostrarResumenDePedidos();
        } else {
            throw new IllegalStateException("Tipo de consulta desconocido: " + tipo);
        }
    }

    // Busca un pedido por su código con PedidoServicio
    private void consultarPedidoPorCodigo() {
        List<Pedido> encontrados = new ArrayList<>();
        encontrados.add(pedidos.obtener(txtCodigo.getText()));
        mostrarPedidos(encontrados);
    }

    // Busca los pedidos de la mesa elegida con PedidoServicio
    private void consultarPedidosPorMesa() {
        Mesa mesa = (Mesa) cmbMesa.getSelectedItem();
        if (mesa == null) {
            throw new ValidacionException("No hay mesas registradas.");
        }
        mostrarPedidos(pedidos.consultarPorMesa(mesa.getCodigo()));
    }

    // Busca los pedidos que están en el estado elegido
    private void consultarPedidosPorEstado() {
        mostrarPedidos(pedidos.consultarPorEstado((EstadoPedido) cmbEstado.getSelectedItem()));
    }

    // Busca los pedidos entre las fechas escritas
    private void consultarPedidosPorFecha() {
        mostrarPedidos(pedidos.consultarPorFecha(txtDesde.getText(), txtHasta.getText()));
    }

    // Muestra todos los productos con su disponibilidad
    private void mostrarListadoProductos() {
        mostrarProductos(productos.listar());
    }

    // Pide a ReporteServicio el resumen por estado y lo muestra
    private void mostrarResumenDePedidos() {
        mostrarResumen(reportes.resumenPorEstado());
    }

    // Muestra los pedidos en la tabla y avisa cuántos se encontraron
    private void mostrarPedidos(List<Pedido> lista) {
        pedidosMostrados = new ArrayList<>(lista);
        tabla.setModel(crearModeloDePedidos(lista));
        btnDetalle.setEnabled(!lista.isEmpty());
        if (lista.isEmpty()) {
            lblResultado.setText("La consulta no produjo resultados.");
        } else {
            lblResultado.setText(lista.size() + " pedido(s) encontrado(s).");
        }
    }

    // Arma el modelo de tabla con una fila por pedido
    private DefaultTableModel crearModeloDePedidos(List<Pedido> lista) {
        DefaultTableModel modelo = UiUtil.modelo("Código", "Fecha", "Cliente", "Mesa", "Estado", "Productos", "Total");
        for (Pedido p : lista) {
            modelo.addRow(new Object[]{p.getCodigo(), p.getFecha().format(CatalogoNombres.FORMATO_FECHA),
                    nombres.cliente(p.getCodigoCliente()), nombres.mesa(p.getCodigoMesa()), p.getEstado(),
                    p.getDetalles().size(), Dinero.formato(p.calcularTotal())});
        }
        return modelo;
    }

    // Muestra los productos en la tabla y avisa cuántos hay
    private void mostrarProductos(List<Producto> lista) {
        limpiarResultados();
        tabla.setModel(crearModeloDeProductos(lista));
        if (lista.isEmpty()) {
            lblResultado.setText("No hay productos registrados.");
        } else {
            lblResultado.setText(lista.size() + " producto(s) en el catálogo.");
        }
    }

    // Arma el modelo de tabla con una fila por producto
    private DefaultTableModel crearModeloDeProductos(List<Producto> lista) {
        DefaultTableModel modelo = UiUtil.modelo("Código", "Nombre", "Categoría", "Precio", "Disponible");
        for (Producto p : lista) {
            String disponible = p.isDisponible() ? "Sí" : "No";
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(),
                    Dinero.formato(p.getPrecio()), disponible});
        }
        return modelo;
    }

    // Muestra el resumen por estado en la tabla
    private void mostrarResumen(List<ResumenEstado> resumen) {
        limpiarResultados();
        int cantidadTotal = contarPedidos(resumen);
        tabla.setModel(crearModeloDeResumen(resumen, cantidadTotal));
        if (cantidadTotal == 0) {
            lblResultado.setText("No hay pedidos registrados.");
        } else {
            lblResultado.setText("Resumen generado a partir de los pedidos almacenados.");
        }
    }

    // Suma la cantidad de pedidos de todos los estados
    private int contarPedidos(List<ResumenEstado> resumen) {
        int cantidadTotal = 0;
        for (ResumenEstado r : resumen) {
            cantidadTotal += r.getCantidad();
        }
        return cantidadTotal;
    }

    // Suma los importes de todos los estados menos los cancelados
    private BigDecimal sumarImporteSinCancelados(List<ResumenEstado> resumen) {
        BigDecimal importeTotal = Dinero.CERO;
        for (ResumenEstado r : resumen) {
            if (r.getEstado() != EstadoPedido.CANCELADO) {
                importeTotal = importeTotal.add(r.getImporte());
            }
        }
        return importeTotal;
    }

    // Arma el modelo de tabla del resumen con una fila final de totales
    private DefaultTableModel crearModeloDeResumen(List<ResumenEstado> resumen, int cantidadTotal) {
        DefaultTableModel modelo = UiUtil.modelo("Estado", "Cantidad de pedidos", "Importe total");
        for (ResumenEstado r : resumen) {
            modelo.addRow(new Object[]{r.getEstado(), r.getCantidad(), Dinero.formato(r.getImporte())});
        }
        modelo.addRow(new Object[]{"TOTAL (importe sin cancelados)", cantidadTotal,
                Dinero.formato(sumarImporteSinCancelados(resumen))});
        return modelo;
    }

    // Botón Ver detalle: abre el detalle del pedido elegido en la tabla
    private void verDetalle() {
        try {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                throw new ValidacionException("Seleccione un pedido de la tabla.");
            }
            mostrarVentanaDeDetalle(pedidosMostrados.get(fila));
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Muestra el texto del pedido en un cuadro de diálogo
    private void mostrarVentanaDeDetalle(Pedido pedido) {
        JTextArea texto = new JTextArea(nombres.describir(pedido));
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(texto), "Detalle del pedido",
                JOptionPane.PLAIN_MESSAGE);
    }

    // Vacía la tabla, el mensaje y desactiva el botón de detalle
    private void limpiarResultados() {
        pedidosMostrados = new ArrayList<>();
        btnDetalle.setEnabled(false);
        tabla.setModel(new DefaultTableModel());
        lblResultado.setText(" ");
    }
}
