package com.restaurante.ui;

import com.restaurante.excepcion.PersistenciaException;
import com.restaurante.excepcion.ValidacionException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

// Métodos de ayuda para las pantallas: mensajes, tablas y formularios
final class UiUtil {

    // Constructor privado para que no se creen objetos de esta clase
    private UiUtil() {
    }

    // Muestra una ventanita de información
    static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    // Muestra un aviso de validación (datos mal escritos)
    static void advertencia(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    // Muestra un mensaje de error
    static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Pregunta Sí/No y devuelve true si el usuario dijo que sí
    static boolean confirmar(Component padre, String mensaje) {
        int respuesta = JOptionPane.showConfirmDialog(padre, mensaje, "Confirmación",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return respuesta == JOptionPane.YES_OPTION;
    }

    // Elige el tipo de mensaje según la excepción que llegó del servicio
    static void mostrarError(Component padre, RuntimeException e) {
        if (e instanceof ValidacionException) {
            advertencia(padre, e.getMessage());
        } else if (e instanceof PersistenciaException) {
            error(padre, e.getMessage());
        } else {
            error(padre, "Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    // Crea un modelo de tabla con esas columnas y sin celdas editables
    static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    // Agrega al formulario una fila con su etiqueta y su componente
    static void campo(JPanel panel, int fila, String etiqueta, JComponent componente) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.gridy = fila;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(componente, c);
    }

    // Devuelve un borde con título para rotular un panel
    static TitledBorder titulo(String texto) {
        return BorderFactory.createTitledBorder(texto);
    }

    // Pone los componentes en una fila alineada a la izquierda
    static JPanel panelFlujo(Component... componentes) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Component c : componentes) {
            panel.add(c);
        }
        return panel;
    }

    // Junta el formulario (centro) con sus botones (abajo)
    static JPanel formularioConBotones(JPanel formulario, JPanel botones) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(formulario, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // Arma la zona de búsqueda arriba y la tabla con scroll abajo
    static JPanel busquedaConTabla(JPanel busqueda, JTable tabla) {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(busqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }
}
