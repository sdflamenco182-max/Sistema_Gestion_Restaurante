package com.restaurante.ui;

import com.restaurante.excepcion.ValidacionException;
import com.restaurante.modelo.Cliente;
import com.restaurante.modelo.DetallePedido;
import com.restaurante.modelo.EstadoPedido;
import com.restaurante.modelo.Mesa;
import com.restaurante.modelo.Pedido;
import com.restaurante.modelo.Producto;
import com.restaurante.servicio.ClienteServicio;
import com.restaurante.servicio.MesaServicio;
import com.restaurante.servicio.PedidoServicio;
import com.restaurante.servicio.ProductoServicio;
import com.restaurante.util.Dinero;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

// Pantalla de pedidos: lista de pedidos arriba y su detalle abajo
class PanelPedidos extends JPanel implements Refrescable {

    // Opción de un combo: guarda el código y el texto que se ve
    private static class Opcion {

        private final String codigo;
        private final String texto;

        // Guarda el código y el texto de la opción
        Opcion(String codigo, String texto) {
            this.codigo = codigo;
            this.texto = texto;
        }

        // Devuelve el código de la opción
        String getCodigo() {
            return codigo;
        }

        // Devuelve el texto de la opción
        String getTexto() {
            return texto;
        }

        // Es lo que muestra el combo
        @Override
        public String toString() {
            return texto;
        }
    }

    // Servicios y catálogo de nombres
    private final PedidoServicio pedidos = new PedidoServicio();
    private final ClienteServicio clientes = new ClienteServicio();
    private final MesaServicio mesas = new MesaServicio();
    private final ProductoServicio productos = new ProductoServicio();
    private final CatalogoNombres nombres = new CatalogoNombres();

    // Parte de arriba: botones, filtro y tabla de pedidos
    private final JButton btnNuevo = new JButton("Nuevo pedido");
    private final JButton btnActualizar = new JButton("Actualizar lista");
    private final JCheckBox chkSoloActivos = new JCheckBox("Solo pedidos activos", true);
    private final DefaultTableModel modeloPedidos =
            UiUtil.modelo("Código", "Fecha", "Cliente", "Mesa", "Estado", "Total");
    private final JTable tablaPedidos = new JTable(modeloPedidos);

    // Parte de abajo: detalle, productos, cambio de estado y total
    private final JLabel lblEncabezado =new JLabel("Seleccione un pedido o cree uno nuevo.");
    private final DefaultTableModel modeloDetalle =
            UiUtil.modelo("Código", "Producto", "Precio aplicado", "Cantidad", "Subtotal");
    private final JTable tablaDetalle = new JTable(modeloDetalle);
    private final JComboBox<Producto> cmbProducto = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField("1", 5);
    private final JButton btnAgregar = new JButton("Agregar producto");
    private final JButton btnCantidad = new JButton("Modificar cantidad");
    private final JButton btnQuitar = new JButton("Quitar producto");
    private final JComboBox<EstadoPedido> cmbEstado = new JComboBox<>();
    private final JButton btnEstado = new JButton("Cambiar estado");
    private final JLabel lblTotal = new JLabel("Total: " + Dinero.formato(Dinero.CERO));
    private final JLabel lblAlerta = new JLabel(" ");

    // Pedido que se está viendo y bandera para no reaccionar mientras se llena la tabla
    private Pedido actual;
    private boolean cargando;

    // Arma la pantalla dividida en dos: lista de pedidos y detalle
    PanelPedidos() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // división de arriba y abajo
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearPanelSuperior(), crearPanelInferior());
        division.setResizeWeight(0.4);
        add(division, BorderLayout.CENTER);

        // eventos y estado inicial sin pedido
        conectarEventos();
        mostrar(null);
    }

    // Arma la parte de arriba: botones y tabla de pedidos
    private JPanel crearPanelSuperior() {
        JPanel barra = UiUtil.panelFlujo(btnNuevo, chkSoloActivos, btnActualizar);
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel superior = new JPanel(new BorderLayout());
        superior.setBorder(UiUtil.titulo("Pedidos"));
        superior.add(barra, BorderLayout.NORTH);
        superior.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
        return superior;
    }

    // Arma la parte de abajo: detalle del pedido seleccionado
    private JPanel crearPanelInferior() {
        lblEncabezado.setFont(lblEncabezado.getFont().deriveFont(Font.BOLD));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 15f));
        lblAlerta.setForeground(new Color(0xB00020));
        tablaDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setBorder(UiUtil.titulo("Detalle del pedido"));
        inferior.add(crearZonaNorteDetalle(), BorderLayout.NORTH);
        inferior.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);
        inferior.add(crearZonaSurDetalle(), BorderLayout.SOUTH);
        return inferior;
    }

    // Arma el encabezado del detalle y la fila para agregar productos
    private JPanel crearZonaNorteDetalle() {
        JPanel filaProductos = UiUtil.panelFlujo(new JLabel("Producto:"), cmbProducto,
                new JLabel("Cantidad:"), txtCantidad, btnAgregar, btnCantidad, btnQuitar);

        JPanel norte = new JPanel(new GridLayout(0, 1));
        norte.add(lblEncabezado);
        norte.add(filaProductos);
        return norte;
    }

    // Arma la zona de abajo con el total y el cambio de estado
    private JPanel crearZonaSurDetalle() {
        JPanel filaTotal = UiUtil.panelFlujo(lblTotal, lblAlerta);
        JPanel filaEstado = UiUtil.panelFlujo(new JLabel("Nuevo estado:"), cmbEstado, btnEstado);

        JPanel sur = new JPanel(new GridLayout(0, 1));
        sur.add(filaTotal);
        sur.add(filaEstado);
        return sur;
    }

    // Conecta cada botón con su método y la tabla con la selección de pedido
    private void conectarEventos() {
        btnNuevo.addActionListener(e -> nuevoPedido());
        btnActualizar.addActionListener(e -> actualizarLista());
        chkSoloActivos.addActionListener(e -> cambiarFiltroActivos());
        btnAgregar.addActionListener(e -> agregarProducto());
        btnCantidad.addActionListener(e -> modificarCantidad());
        btnQuitar.addActionListener(e -> quitarProducto());
        btnEstado.addActionListener(e -> cambiarEstado());
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !cargando) {
                seleccionarPedidoDeLaTabla();
            }
        });
    }

    // Botón Actualizar lista: vuelve a cargar los pedidos
    private void actualizarLista() {
        try {
            refrescar();
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Check Solo activos: recarga la tabla según el filtro
    private void cambiarFiltroActivos() {
        try {
            cargarPedidos(codigoActual());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Al elegir un pedido de la tabla, muestra su detalle
    private void seleccionarPedidoDeLaTabla() {
        try {
            mostrarPedidoDeFila(tablaPedidos.getSelectedRow());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Recarga nombres, productos disponibles y la lista de pedidos
    @Override
    public void refrescar() {
        nombres.recargar();
        cmbProducto.setModel(new DefaultComboBoxModel<>(
                productos.listarDisponibles().toArray(new Producto[0])));
        cargarPedidos(codigoActual());
    }

    // Devuelve el código del pedido que se está viendo (o null)
    private String codigoActual() {
        if (actual == null) {
            return null;
        }
        return actual.getCodigo();
    }

    // Busca el pedido de la fila elegida y lo muestra
    private void mostrarPedidoDeFila(int fila) {
        if (fila < 0) {
            mostrar(null);
        } else {
            String codigo = (String) modeloPedidos.getValueAt(fila, 0);
            mostrar(pedidos.obtener(codigo));
        }
    }

    // Llena la tabla de pedidos y deja elegido el que se indica
    private void cargarPedidos(String codigoAMostrar) {
        List<Pedido> lista = listarSegunFiltro();
        cargando = true;
        try {
            llenarTablaPedidos(lista, codigoAMostrar);
        } finally {
            cargando = false;
        }
        mostrarPedidoPorCodigo(codigoAMostrar);
    }

    // Pide al servicio solo los activos o todos, según el check
    private List<Pedido> listarSegunFiltro() {
        if (chkSoloActivos.isSelected()) {
            return pedidos.listarActivos();
        }
        return pedidos.listarTodos();
    }

    // Muestra el pedido con ese código, o la pantalla vacía si es null
    private void mostrarPedidoPorCodigo(String codigo) {
        if (codigo == null) {
            mostrar(null);
        } else {
            mostrar(pedidos.obtener(codigo));
        }
    }

    // Agrega una fila por pedido y marca la del pedido indicado
    private void llenarTablaPedidos(List<Pedido> lista, String codigoAMostrar) {
        modeloPedidos.setRowCount(0);
        int filaSeleccionada = -1;
        for (Pedido p : lista) {
            if (p.getCodigo().equals(codigoAMostrar)) {
                filaSeleccionada = modeloPedidos.getRowCount();
            }
            modeloPedidos.addRow(new Object[]{p.getCodigo(), p.getFecha().format(CatalogoNombres.FORMATO_FECHA),
                    nombres.cliente(p.getCodigoCliente()), nombres.mesa(p.getCodigoMesa()),
                    p.getEstado(), Dinero.formato(p.calcularTotal())});
        }
        if (filaSeleccionada >= 0) {
            tablaPedidos.setRowSelectionInterval(filaSeleccionada, filaSeleccionada);
        }
    }

    // Botón Nuevo pedido: pide cliente y mesa y crea el pedido con el servicio
    private void nuevoPedido() {
        try {
            JComboBox<Opcion> cmbCliente = new JComboBox<>(crearOpcionesCliente());
            JComboBox<Opcion> cmbMesa = new JComboBox<>(crearOpcionesMesa());
            if (!pedirClienteYMesa(cmbCliente, cmbMesa)) {
                return;
            }
            crearPedidoConOpciones(cmbCliente, cmbMesa);
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Muestra un cuadro para elegir cliente y mesa, devuelve true si aceptó
    private boolean pedirClienteYMesa(JComboBox<Opcion> cmbCliente, JComboBox<Opcion> cmbMesa) {
        JPanel formulario = new JPanel(new GridBagLayout());
        UiUtil.campo(formulario, 0, "Cliente (opcional):", cmbCliente);
        UiUtil.campo(formulario, 1, "Mesa (opcional):", cmbMesa);
        int opcion = JOptionPane.showConfirmDialog(this, formulario, "Nuevo pedido",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return opcion == JOptionPane.OK_OPTION;
    }

    // Crea el pedido con lo elegido y lo deja seleccionado
    private void crearPedidoConOpciones(JComboBox<Opcion> cmbCliente, JComboBox<Opcion> cmbMesa) {
        Opcion clienteElegido = (Opcion) cmbCliente.getSelectedItem();
        Opcion mesaElegida = (Opcion) cmbMesa.getSelectedItem();
        Pedido nuevo = pedidos.crear(clienteElegido.getCodigo(), mesaElegida.getCodigo());
        nombres.recargar();
        cargarPedidos(nuevo.getCodigo());
    }

    // Arma las opciones de clientes activos, con una opción "sin cliente"
    private Opcion[] crearOpcionesCliente() {
        List<Opcion> opciones = new ArrayList<>();
        opciones.add(new Opcion(null, "(Sin cliente)"));
        for (Cliente c : clientes.listarActivos()) {
            opciones.add(new Opcion(c.getCodigo(), c.getCodigo() + " - " + c.getNombre()));
        }
        return opciones.toArray(new Opcion[0]);
    }

    // Arma las opciones de mesas disponibles, con una opción "sin mesa"
    private Opcion[] crearOpcionesMesa() {
        List<Opcion> opciones = new ArrayList<>();
        opciones.add(new Opcion(null, "(Sin mesa)"));
        for (Mesa m : mesas.listarDisponibles()) {
            opciones.add(new Opcion(m.getCodigo(), "Mesa " + m.getNumero() + " (cap. " + m.getCapacidad() + ")"));
        }
        return opciones.toArray(new Opcion[0]);
    }

    // Muestra un pedido en la parte de abajo (o la deja vacía si es null)
    private void mostrar(Pedido pedido) {
        actual = pedido;
        modeloDetalle.setRowCount(0);
        lblAlerta.setText(" ");
        boolean hayPedido = pedido != null;
        boolean editable = hayPedido && pedido.getEstado().permiteModificarDetalle();
        habilitarEdicionDetalle(editable);

        DefaultComboBoxModel<EstadoPedido> destinos = new DefaultComboBoxModel<>();
        if (!hayPedido) {
            mostrarSinPedido();
        } else {
            mostrarDatosDelPedido(pedido, editable, destinos);
        }
        habilitarCambioDeEstado(destinos);
    }

    // Deja el encabezado y el total como al inicio
    private void mostrarSinPedido() {
        lblEncabezado.setText("Seleccione un pedido o cree uno nuevo.");
        lblTotal.setText("Total: " + Dinero.formato(Dinero.CERO));
    }

    // Pasa a la pantalla los datos del pedido: encabezado, productos, total y estados
    private void mostrarDatosDelPedido(Pedido pedido, boolean editable,
                                       DefaultComboBoxModel<EstadoPedido> destinos) {
        mostrarEncabezado(pedido, editable);
        mostrarDetalles(pedido);
        mostrarTotal(pedido);
        llenarDestinos(destinos, pedido);
    }

    // Activa el combo y el botón de estado solo si hay estados a donde pasar
    private void habilitarCambioDeEstado(DefaultComboBoxModel<EstadoPedido> destinos) {
        cmbEstado.setModel(destinos);
        cmbEstado.setEnabled(destinos.getSize() > 0);
        btnEstado.setEnabled(destinos.getSize() > 0);
    }

    // Activa o desactiva los controles para editar los productos del pedido
    private void habilitarEdicionDetalle(boolean editable) {
        cmbProducto.setEnabled(editable);
        txtCantidad.setEnabled(editable);
        btnAgregar.setEnabled(editable);
        btnCantidad.setEnabled(editable);
        btnQuitar.setEnabled(editable);
    }

    // Escribe la línea con código, cliente, mesa y estado del pedido
    private void mostrarEncabezado(Pedido pedido, boolean editable) {
        String texto = "Pedido " + pedido.getCodigo() + "  |  Cliente: " + nombres.cliente(pedido.getCodigoCliente())
                + "  |  " + nombres.mesa(pedido.getCodigoMesa()) + "  |  Estado: " + pedido.getEstado();
        if (!editable) {
            texto = texto + "  (solo lectura)";
        }
        lblEncabezado.setText(texto);
    }

    // Llena la tabla de detalle con los productos del pedido
    private void mostrarDetalles(Pedido pedido) {
        for (DetallePedido d : pedido.getDetalles()) {
            modeloDetalle.addRow(new Object[]{d.getCodigoProducto(), d.getNombreProducto(),
                    Dinero.formato(d.getPrecioUnitario()), d.getCantidad(), Dinero.formato(d.subtotal())});
        }
    }

    // Muestra el total y una alerta si no coincide con el detalle
    private void mostrarTotal(Pedido pedido) {
        lblTotal.setText("Total: " + Dinero.formato(pedido.calcularTotal()));
        if (!pedido.totalConsistente()) {
            lblAlerta.setText("Inconsistencia: el total almacenado (" + Dinero.formato(pedido.getTotal())
                    + ") no coincide con la suma del detalle.");
        }
    }

    // Agrega al combo los estados a los que puede pasar el pedido
    private void llenarDestinos(DefaultComboBoxModel<EstadoPedido> destinos, Pedido pedido) {
        for (EstadoPedido estado : EstadoPedido.values()) {
            if (pedido.getEstado().puedePasarA(estado)) {
                destinos.addElement(estado);
            }
        }
    }

    // Devuelve el pedido actual o avisa que hay que elegir uno
    private Pedido exigirPedido() {
        if (actual == null) {
            throw new ValidacionException("Seleccione un pedido de la lista.");
        }
        return actual;
    }

    // Devuelve el código del producto elegido en el detalle o avisa que falta elegir
    private String codigoDetalleSeleccionado() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila < 0) {
            throw new ValidacionException("Seleccione un producto del detalle del pedido.");
        }
        return (String) modeloDetalle.getValueAt(fila, 0);
    }

    // Botón Agregar producto: manda producto y cantidad al servicio y recarga
    private void agregarProducto() {
        try {
            Pedido pedido = exigirPedido();
            Producto producto = (Producto) cmbProducto.getSelectedItem();
            if (producto == null) {
                throw new ValidacionException("No hay productos disponibles para agregar.");
            }
            pedidos.agregarProducto(pedido.getCodigo(), producto.getCodigo(), txtCantidad.getText());
            txtCantidad.setText("1");
            cargarPedidos(pedido.getCodigo());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Modificar cantidad: cambia la cantidad del producto elegido con el servicio
    private void modificarCantidad() {
        try {
            Pedido pedido = exigirPedido();
            pedidos.modificarCantidad(pedido.getCodigo(), codigoDetalleSeleccionado(), txtCantidad.getText());
            cargarPedidos(pedido.getCodigo());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Quitar producto: pide confirmar y lo quita del pedido con el servicio
    private void quitarProducto() {
        try {
            Pedido pedido = exigirPedido();
            String codigo = codigoDetalleSeleccionado();
            if (!UiUtil.confirmar(this, "¿Quitar el producto " + codigo + " del pedido?")) {
                return;
            }
            pedidos.quitarProducto(pedido.getCodigo(), codigo);
            cargarPedidos(pedido.getCodigo());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Cambiar estado: pasa el pedido al estado elegido (confirma si es cancelar)
    private void cambiarEstado() {
        try {
            Pedido pedido = exigirPedido();
            EstadoPedido destino = (EstadoPedido) cmbEstado.getSelectedItem();
            if (destino == null) {
                throw new ValidacionException("Este pedido ya no admite cambios de estado.");
            }
            if (destino == EstadoPedido.CANCELADO && !confirmarCancelacion(pedido)) {
                return;
            }
            aplicarCambioDeEstado(pedido, destino);
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Cambia el estado con el servicio, avisa y recarga la lista
    private void aplicarCambioDeEstado(Pedido pedido, EstadoPedido destino) {
        pedidos.cambiarEstado(pedido.getCodigo(), destino);
        UiUtil.info(this, "El pedido " + pedido.getCodigo() + " ahora está \"" + destino + "\".");
        nombres.recargar();
        cargarPedidos(pedido.getCodigo());
    }

    // Pregunta si de verdad quiere cancelar el pedido
    private boolean confirmarCancelacion(Pedido pedido) {
        return UiUtil.confirmar(this,
                "¿Cancelar el pedido " + pedido.getCodigo() + "? Esta acción no se puede deshacer.");
    }
}
