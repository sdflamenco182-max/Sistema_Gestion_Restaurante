package com.restaurante.ui;

import com.restaurante.modelo.Cliente;
import com.restaurante.servicio.ClienteServicio;
import com.restaurante.servicio.ResultadoEliminacion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

// Pantalla de clientes: registrar, modificar, eliminar y buscar
class PanelClientes extends JPanel implements Refrescable {

    // Servicio de clientes
    private final ClienteServicio servicio = new ClienteServicio();
    // Campos del formulario y buscador
    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtTelefono = new JTextField(14);
    private final JTextField txtCorreo = new JTextField(24);
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);
    private final JTextField txtBuscar = new JTextField(18);
    // Botones
    private final JButton btnRegistrar = new JButton("Registrar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnTodos = new JButton("Mostrar todos");
    // Tabla y la lista de clientes que muestra
    private final DefaultTableModel modelo =UiUtil.modelo("Código", "Nombre", "Teléfono", "Correo", "Activo");
    private final JTable tabla = new JTable(modelo);
    private List<Cliente> mostrados = new ArrayList<>();

    // Arma la pantalla: formulario con botones arriba, búsqueda y tabla abajo
    PanelClientes() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // formulario y botones
        JPanel norte = UiUtil.formularioConBotones(crearFormulario(), crearBotones());
        add(norte, BorderLayout.NORTH);
        // búsqueda y tabla
        add(UiUtil.busquedaConTabla(crearBusqueda(), tabla), BorderLayout.CENTER);

        // eventos
        conectarEventos();
    }

    // Arma el formulario con los datos del cliente
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(UiUtil.titulo("Datos del cliente"));
        UiUtil.campo(formulario, 0, "Código:", txtCodigo);
        UiUtil.campo(formulario, 1, "Nombre:", txtNombre);
        UiUtil.campo(formulario, 2, "Teléfono:", txtTelefono);
        UiUtil.campo(formulario, 3, "Correo:", txtCorreo);
        UiUtil.campo(formulario, 4, "", chkActivo);
        return formulario;
    }

    // Pone los botones en una fila
    private JPanel crearBotones() {
        return UiUtil.panelFlujo(btnRegistrar, btnModificar, btnEliminar, btnLimpiar);
    }

    // Arma la fila de búsqueda por código o nombre
    private JPanel crearBusqueda() {
        return UiUtil.panelFlujo(new JLabel("Buscar por código o nombre:"), txtBuscar, btnBuscar, btnTodos);
    }

    // Conecta cada botón con su método y la tabla con la selección de fila
    private void conectarEventos() {
        btnRegistrar.addActionListener(e -> registrar());
        btnModificar.addActionListener(e -> modificar());
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

    // Botón Registrar: manda lo escrito al servicio y recarga la tabla
    private void registrar() {
        try {
            servicio.registrar(txtCodigo.getText(), txtNombre.getText(), txtTelefono.getText(), txtCorreo.getText());
            UiUtil.info(this, "Cliente registrado correctamente.");
            limpiar();
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Modificar: guarda los cambios del cliente con el servicio
    private void modificar() {
        try {
            servicio.modificar(txtCodigo.getText(), txtNombre.getText(), txtTelefono.getText(),
                    txtCorreo.getText(), chkActivo.isSelected());
            UiUtil.info(this, "Cliente modificado correctamente.");
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Eliminar: pide confirmar y llama al servicio (si tiene pedidos solo se inactiva)
    private void eliminar() {
        try {
            String codigo = txtCodigo.getText();
            if (codigo.isBlank()) {
                UiUtil.advertencia(this, "Seleccione un cliente de la tabla.");
                return;
            }
            if (!confirmarEliminacion(codigo)) {
                return;
            }
            ResultadoEliminacion resultado = servicio.eliminar(codigo);
            avisarResultadoEliminacion(resultado);
            limpiar();
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Pregunta si de verdad quiere eliminar el cliente
    private boolean confirmarEliminacion(String codigo) {
        return UiUtil.confirmar(this, "¿Eliminar el cliente " + codigo + "?\n"
                + "Si tiene pedidos registrados se inactivará en lugar de eliminarse.");
    }

    // Avisa si el cliente se eliminó o solo se inactivó
    private void avisarResultadoEliminacion(ResultadoEliminacion resultado) {
        if (resultado == ResultadoEliminacion.ELIMINADO) {
            UiUtil.info(this, "Cliente eliminado correctamente.");
        } else {
            UiUtil.info(this, "El cliente tiene pedidos asociados, por lo que fue inactivado y se conserva su historial.");
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

    // Botón Mostrar todos: borra el buscador y carga todos los clientes
    private void mostrarTodos() {
        try {
            txtBuscar.setText("");
            cargar("");
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Busca en el servicio y llena la tabla con los clientes encontrados
    private void cargar(String texto) {
        mostrados = servicio.buscar(texto);
        modelo.setRowCount(0);
        for (Cliente c : mostrados) {
            String activo = c.isActivo() ? "Sí" : "No";
            modelo.addRow(new Object[]{c.getCodigo(), c.getNombre(), c.getTelefono(), c.getCorreo(), activo});
        }
    }

    // Al elegir una fila, pasa sus datos al formulario
    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        Cliente c = mostrados.get(tabla.convertRowIndexToModel(fila));
        txtCodigo.setText(c.getCodigo());
        txtCodigo.setEditable(false);
        txtNombre.setText(c.getNombre());
        txtTelefono.setText(c.getTelefono());
        txtCorreo.setText(c.getCorreo());
        chkActivo.setSelected(c.isActivo());
    }

    // Botón Limpiar: deja el formulario vacío y sin fila elegida
    private void limpiar() {
        tabla.clearSelection();
        txtCodigo.setText("");
        txtCodigo.setEditable(true);
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        chkActivo.setSelected(true);
    }
}
