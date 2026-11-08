package com.restaurante.ui;

import com.restaurante.modelo.Producto;
import com.restaurante.servicio.ProductoServicio;
import com.restaurante.util.Dinero;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

// Pantalla de productos: registrar, modificar, disponibilidad, eliminar y buscar
class PanelProductos extends JPanel implements Refrescable {

    // Categorías que salen en el combo
    private static final String[] CATEGORIAS = {"Comida", "Bebidas", "Postres", "Entradas", "Otros"};

    // Servicio de productos
    private final ProductoServicio servicio = new ProductoServicio();
    // Campos del formulario y buscador
    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNombre = new JTextField(24);
    private final JComboBox<String> cmbCategoria = new JComboBox<>(CATEGORIAS);
    private final JTextField txtDescripcion = new JTextField(24);
    private final JTextField txtPrecio = new JTextField(8);
    private final JCheckBox chkDisponible = new JCheckBox("Disponible para nuevos pedidos", true);
    private final JTextField txtBuscar = new JTextField(16);
    // Botones
    private final JButton btnRegistrar = new JButton("Registrar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnDisponibilidad = new JButton("Cambiar disponibilidad");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnTodos = new JButton("Mostrar todos");
    // Tabla y la lista de productos que muestra
    private final DefaultTableModel modelo =
            UiUtil.modelo("Código", "Nombre", "Categoría", "Descripción", "Precio", "Disponible");
    private final JTable tabla = new JTable(modelo);
    private List<Producto> mostrados = new ArrayList<>();

    // Arma la pantalla: formulario con botones arriba, búsqueda y tabla abajo
    PanelProductos() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // la categoría se puede escribir además de elegir
        cmbCategoria.setEditable(true);

        // formulario y botones
        JPanel norte = UiUtil.formularioConBotones(crearFormulario(), crearBotones());
        add(norte, BorderLayout.NORTH);
        // búsqueda y tabla
        add(UiUtil.busquedaConTabla(crearBusqueda(), tabla), BorderLayout.CENTER);

        // eventos
        conectarEventos();
    }

    // Arma el formulario con los datos del producto
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(UiUtil.titulo("Datos del producto"));
        UiUtil.campo(formulario, 0, "Código:", txtCodigo);
        UiUtil.campo(formulario, 1, "Nombre:", txtNombre);
        UiUtil.campo(formulario, 2, "Categoría:", cmbCategoria);
        UiUtil.campo(formulario, 3, "Descripción:", txtDescripcion);
        UiUtil.campo(formulario, 4, "Precio unitario:", txtPrecio);
        UiUtil.campo(formulario, 5, "", chkDisponible);
        return formulario;
    }

    // Pone los botones en una fila
    private JPanel crearBotones() {
        return UiUtil.panelFlujo(btnRegistrar, btnModificar, btnDisponibilidad, btnEliminar, btnLimpiar);
    }

    // Arma la fila de búsqueda por código, nombre o categoría
    private JPanel crearBusqueda() {
        return UiUtil.panelFlujo(new JLabel("Buscar por código, nombre o categoría:"), txtBuscar, btnBuscar, btnTodos);
    }

    // Conecta cada botón con su método y la tabla con la selección de fila
    private void conectarEventos() {
        btnRegistrar.addActionListener(e -> registrar());
        btnModificar.addActionListener(e -> modificar());
        btnDisponibilidad.addActionListener(e -> cambiarDisponibilidad());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnBuscar.addActionListener(e -> buscar());
        btnTodos.addActionListener(e -> mostrarTodos());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
    }

    // Recarga la tabla con lo que haya escrito en el buscador
    @Override
    public void refrescar() {
        cargar(txtBuscar.getText());
    }

    // Devuelve la categoría escrita o elegida en el combo (vacía si no hay)
    private String categoria() {
        Object seleccion = cmbCategoria.getEditor().getItem();
        if (seleccion == null) {
            return "";
        }
        return seleccion.toString();
    }

    // Botón Registrar: manda lo escrito al servicio y recarga la tabla
    private void registrar() {
        try {
            servicio.registrar(txtCodigo.getText(), txtNombre.getText(), categoria(), txtDescripcion.getText(),
                    txtPrecio.getText(), chkDisponible.isSelected());
            UiUtil.info(this, "Producto registrado correctamente.");
            limpiar();
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Modificar: guarda los cambios del producto con el servicio
    private void modificar() {
        try {
            servicio.modificar(txtCodigo.getText(), txtNombre.getText(), categoria(), txtDescripcion.getText(),
                    txtPrecio.getText(), chkDisponible.isSelected());
            UiUtil.info(this, "Producto modificado correctamente. Los pedidos anteriores conservan su precio.");
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Cambiar disponibilidad: aplica lo marcado en el check al producto
    private void cambiarDisponibilidad() {
        try {
            servicio.cambiarDisponibilidad(txtCodigo.getText(), chkDisponible.isSelected());
            UiUtil.info(this, "Disponibilidad actualizada.");
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Eliminar: pide confirmar y elimina el producto con el servicio
    private void eliminar() {
        try {
            String codigo = txtCodigo.getText();
            if (codigo.isBlank()) {
                UiUtil.advertencia(this, "Seleccione un producto de la tabla.");
                return;
            }
            if (!UiUtil.confirmar(this, "¿Eliminar el producto " + codigo + "?")) {
                return;
            }
            servicio.eliminar(codigo);
            UiUtil.info(this, "Producto eliminado correctamente.");
            limpiar();
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Buscar: filtra la tabla con el texto escrito
    private void buscar() {
        try {
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Mostrar todos: borra el buscador y carga todos los productos
    private void mostrarTodos() {
        try {
            txtBuscar.setText("");
            cargar("");
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Busca en el servicio y llena la tabla con los productos encontrados
    private void cargar(String texto) {
        mostrados = servicio.buscar(texto);
        modelo.setRowCount(0);
        for (Producto p : mostrados) {
            String disponible = p.isDisponible() ? "Sí" : "No";
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(), p.getDescripcion(),
                    Dinero.formato(p.getPrecio()), disponible});
        }
    }

    // Al elegir una fila, pasa sus datos al formulario
    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        Producto p = mostrados.get(tabla.convertRowIndexToModel(fila));
        txtCodigo.setText(p.getCodigo());
        txtCodigo.setEditable(false);
        txtNombre.setText(p.getNombre());
        cmbCategoria.setSelectedItem(p.getCategoria());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(p.getPrecio().toPlainString());
        chkDisponible.setSelected(p.isDisponible());
    }

    // Botón Limpiar: deja el formulario vacío y sin fila elegida
    private void limpiar() {
        tabla.clearSelection();
        txtCodigo.setText("");
        txtCodigo.setEditable(true);
        txtNombre.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtDescripcion.setText("");
        txtPrecio.setText("");
        chkDisponible.setSelected(true);
    }
}
