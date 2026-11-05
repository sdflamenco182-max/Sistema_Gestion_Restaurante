package com.restaurante.ui;

import com.restaurante.modelo.EstadoMesa;
import com.restaurante.modelo.Mesa;
import com.restaurante.servicio.MesaServicio;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

// Pantalla de mesas: registrar, modificar, cambiar estado, eliminar y buscar
class PanelMesas extends JPanel implements Refrescable {

    // Servicio de mesas
    private final MesaServicio servicio = new MesaServicio();
    // Campos del formulario y buscador
    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNumero = new JTextField(6);
    private final JTextField txtCapacidad = new JTextField(6);
    private final JComboBox<EstadoMesa> cmbEstado = new JComboBox<>(EstadoMesa.values());
    private final JTextField txtBuscar = new JTextField(14);
    // Botones
    private final JButton btnRegistrar = new JButton("Registrar");
    private final JButton btnModificar = new JButton("Modificar datos");
    private final JButton btnEstado = new JButton("Cambiar estado");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnTodas = new JButton("Mostrar todas");
    // Tabla y la lista de mesas que muestra
    private final DefaultTableModel modelo =UiUtil.modelo("Código", "Número", "Capacidad", "Estado");
    private final JTable tabla = new JTable(modelo);
    private List<Mesa> mostradas = new ArrayList<>();

    // Arma la pantalla: formulario con botones arriba, búsqueda y tabla abajo
    PanelMesas() {
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

    // Arma el formulario con los datos de la mesa
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(UiUtil.titulo("Datos de la mesa"));
        UiUtil.campo(formulario, 0, "Código:", txtCodigo);
        UiUtil.campo(formulario, 1, "Número:", txtNumero);
        UiUtil.campo(formulario, 2, "Capacidad:", txtCapacidad);
        UiUtil.campo(formulario, 3, "Estado:", cmbEstado);
        return formulario;
    }

    // Pone los botones en una fila
    private JPanel crearBotones() {
        return UiUtil.panelFlujo(btnRegistrar, btnModificar, btnEstado, btnEliminar, btnLimpiar);
    }

    // Arma la fila de búsqueda por número o código
    private JPanel crearBusqueda() {
        return UiUtil.panelFlujo(new JLabel("Buscar por número o código:"), txtBuscar, btnBuscar, btnTodas);
    }

    // Conecta cada botón con su método y la tabla con la selección de fila
    private void conectarEventos() {
        btnRegistrar.addActionListener(e -> registrar());
        btnModificar.addActionListener(e -> modificar());
        btnEstado.addActionListener(e -> cambiarEstado());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnBuscar.addActionListener(e -> buscar());
        btnTodas.addActionListener(e -> mostrarTodas());
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
            servicio.registrar(txtCodigo.getText(), txtNumero.getText(), txtCapacidad.getText(),
                    (EstadoMesa) cmbEstado.getSelectedItem());
            UiUtil.info(this, "Mesa registrada correctamente.");
            limpiar();
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Modificar datos: guarda número y capacidad con el servicio
    private void modificar() {
        try {
            servicio.modificar(txtCodigo.getText(), txtNumero.getText(), txtCapacidad.getText());
            UiUtil.info(this, "Mesa modificada correctamente.");
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Cambiar estado: le pone a la mesa el estado elegido en el combo
    private void cambiarEstado() {
        try {
            servicio.cambiarEstado(txtCodigo.getText(), (EstadoMesa) cmbEstado.getSelectedItem());
            UiUtil.info(this, "Estado de la mesa actualizado.");
            cargar(txtBuscar.getText());
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Botón Eliminar: pide confirmar y elimina la mesa con el servicio
    private void eliminar() {
        try {
            String codigo = txtCodigo.getText();
            if (codigo.isBlank()) {
                UiUtil.advertencia(this, "Seleccione una mesa de la tabla.");
                return;
            }
            if (!UiUtil.confirmar(this, "¿Eliminar la mesa " + codigo + "?")) {
                return;
            }
            servicio.eliminar(codigo);
            UiUtil.info(this, "Mesa eliminada correctamente.");
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

    // Botón Mostrar todas: borra el buscador y carga todas las mesas
    private void mostrarTodas() {
        try {
            txtBuscar.setText("");
            cargar("");
        } catch (RuntimeException ex) {
            UiUtil.mostrarError(this, ex);
        }
    }

    // Busca en el servicio y llena la tabla con las mesas encontradas
    private void cargar(String texto) {
        mostradas = servicio.buscar(texto);
        modelo.setRowCount(0);
        for (Mesa m : mostradas) {
            modelo.addRow(new Object[]{m.getCodigo(), m.getNumero(), m.getCapacidad(), m.getEstado()});
        }
    }

    // Al elegir una fila, pasa sus datos al formulario
    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        Mesa m = mostradas.get(tabla.convertRowIndexToModel(fila));
        txtCodigo.setText(m.getCodigo());
        txtCodigo.setEditable(false);
        txtNumero.setText(String.valueOf(m.getNumero()));
        txtCapacidad.setText(String.valueOf(m.getCapacidad()));
        cmbEstado.setSelectedItem(m.getEstado());
    }

    // Botón Limpiar: deja el formulario vacío y sin fila elegida
    private void limpiar() {
        tabla.clearSelection();
        txtCodigo.setText("");
        txtCodigo.setEditable(true);
        txtNumero.setText("");
        txtCapacidad.setText("");
        cmbEstado.setSelectedItem(EstadoMesa.DISPONIBLE);
    }
}
