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

public class MainFrame extends JFrame {

    private final AuthServicio autenticacion = new AuthServicio();
    private final JTabbedPane pestanas = new JTabbedPane();

    public MainFrame() {
        super("Restaurante - Sistema de pedidos");
        Sesion.exigirAutenticado();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        crearPestanas();
        setJMenuBar(crearMenu());

        setLayout(new BorderLayout());
        add(pestanas, BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);

        conectarEventos();

        setMinimumSize(new Dimension(980, 640));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        refrescar(pestanas.getSelectedComponent());
    }

    private void crearPestanas() {
        pestanas.addTab("Consultas y reportes", new PanelConsultas());
    }

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

    private JLabel crearBarraEstado() {
        JLabel estado = new JLabel("Usuario: " + Sesion.usuario().getNombreCompleto()
                + " (" + Sesion.usuario().getNombreUsuario() + ")");
        estado.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return estado;
    }

    private void conectarEventos() {
        pestanas.addChangeListener(e -> refrescar(pestanas.getSelectedComponent()));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
    }

    private void refrescar(Component panel) {
        if (panel instanceof Refrescable) {
            Refrescable refrescable = (Refrescable) panel;
            UiUtil.ejecutar(this, () -> refrescable.refrescar());
        }
    }

    private void cerrarSesion() {
        if (UiUtil.confirmar(this, "¿Desea cerrar la sesión?")) {
            autenticacion.cerrarSesion();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void salir() {
        if (UiUtil.confirmar(this, "¿Desea salir de la aplicación?")) {
            autenticacion.cerrarSesion();
            dispose();
            System.exit(0);
        }
    }
}
