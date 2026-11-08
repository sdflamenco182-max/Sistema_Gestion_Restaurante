package com.restaurante.ui;

import com.restaurante.servicio.AuthServicio;
import com.restaurante.servicio.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Ventana principal con una pestaña por cada módulo del sistema
public class MainFrame extends JFrame {

    // Servicio de sesión y las pestañas
    private final AuthServicio autenticacion = new AuthServicio();
    private final JTabbedPane pestanas = new JTabbedPane();

    // Arma la ventana principal (exige haber iniciado sesión)
    public MainFrame() {
        super("Restaurante - Sistema de pedidos");
        Sesion.exigirAutenticado();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // pestañas, menú y distribución
        crearPestanas();
        setJMenuBar(crearMenu());
        armarDistribucion();
        // eventos, tamaño y carga de la primera pestaña
        conectarEventos();
        configurarTamanoYPosicion();
        refrescar(pestanas.getSelectedComponent());
    }

    // Pone las pestañas al centro y la barra de estado abajo
    private void armarDistribucion() {
        setLayout(new BorderLayout());
        add(pestanas, BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);
    }

    // Define el tamaño de la ventana y la centra en pantalla
    private void configurarTamanoYPosicion() {
        setMinimumSize(new Dimension(980, 640));
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    // Agrega una pestaña por cada panel
    private void crearPestanas() {
        pestanas.addTab("Pedidos", new PanelPedidos());
        pestanas.addTab("Clientes", new PanelClientes());
        pestanas.addTab("Mesas", new PanelMesas());
        pestanas.addTab("Productos", new PanelProductos());
        pestanas.addTab("Consultas y reportes", new PanelConsultas());
    }

    // Arma el menú Sesión con las opciones Cerrar sesión y Salir
    private JMenuBar crearMenu() {
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        JMenuItem salir = new JMenuItem("Salir");
        cerrar.addActionListener(e -> cerrarSesion());
        salir.addActionListener(e -> salir());

        JMenu menuSesion = new JMenu("Sesión");
        menuSesion.add(cerrar);
        menuSesion.add(salir);

        JMenuBar barra = new JMenuBar();
        barra.add(menuSesion);
        return barra;
    }

    // Crea la barra de abajo que muestra el usuario conectado
    private JLabel crearBarraEstado() {
        JLabel estado = new JLabel("Usuario: " + Sesion.usuario().getNombreCompleto()
                + " (" + Sesion.usuario().getNombreUsuario() + ")");
        estado.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return estado;
    }

    // Recarga el panel al cambiar de pestaña y pide confirmar al cerrar la ventana
    private void conectarEventos() {
        pestanas.addChangeListener(e -> refrescar(pestanas.getSelectedComponent()));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
    }

    // Si el panel es Refrescable, le pide que recargue sus datos
    private void refrescar(Component panel) {
        if (panel instanceof Refrescable) {
            try {
                ((Refrescable) panel).refrescar();
            } catch (RuntimeException ex) {
                UiUtil.mostrarError(this, ex);
            }
        }
    }

    // Menú Cerrar sesión: confirma, cierra la sesión y vuelve al login
    private void cerrarSesion() {
        if (UiUtil.confirmar(this, "¿Desea cerrar la sesión?")) {
            autenticacion.cerrarSesion();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // Menú Salir: confirma, cierra la sesión y termina el programa
    private void salir() {
        if (UiUtil.confirmar(this, "¿Desea salir de la aplicación?")) {
            autenticacion.cerrarSesion();
            dispose();
            System.exit(0);
        }
    }
}
