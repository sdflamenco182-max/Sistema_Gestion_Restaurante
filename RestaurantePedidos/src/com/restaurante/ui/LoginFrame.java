package com.restaurante.ui;

import com.restaurante.excepcion.PersistenciaException;
import com.restaurante.excepcion.ValidacionException;
import com.restaurante.servicio.AuthServicio;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;

// Ventana de inicio de sesión: pide usuario y contraseña
public class LoginFrame extends JFrame {

    // Servicio que valida el usuario
    private final AuthServicio autenticacion = new AuthServicio();
    // Campos, mensaje de error y botón de la pantalla
    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtClave = new JPasswordField(18);
    private final JLabel lblMensaje = new JLabel(" ", SwingConstants.CENTER);
    private final JButton btnIngresar = new JButton("Iniciar sesión");

    // Arma la ventana: título, formulario y zona del botón
    public LoginFrame() {
        super("Restaurante - Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // partes de la ventana
        setLayout(new BorderLayout());
        add(crearTitulo(), BorderLayout.NORTH);
        add(crearFormulario(), BorderLayout.CENTER);
        add(crearZonaInferior(), BorderLayout.SOUTH);

        // eventos
        conectarEventos();

        // tamaño y posición
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    // Crea el título que va arriba
    private JLabel crearTitulo() {
        JLabel titulo = new JLabel("Sistema de Pedidos del Restaurante", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setBorder(BorderFactory.createEmptyBorder(14, 10, 4, 10));
        return titulo;
    }

    // Arma el formulario con usuario y contraseña
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new GridBagLayout());
        UiUtil.campo(formulario, 0, "Usuario o correo:", txtUsuario);
        UiUtil.campo(formulario, 1, "Contraseña:", txtClave);
        return formulario;
    }

    // Arma la parte de abajo: mensaje de error y botón de ingresar
    private JPanel crearZonaInferior() {
        lblMensaje.setForeground(new Color(0xB00020));
        JPanel inferior = new JPanel(new BorderLayout(0, 8));
        inferior.setBorder(BorderFactory.createEmptyBorder(4, 20, 16, 20));
        inferior.add(lblMensaje, BorderLayout.NORTH);
        inferior.add(btnIngresar, BorderLayout.CENTER);
        return inferior;
    }

    // Conecta el botón y deja Enter como botón por defecto
    private void conectarEventos() {
        btnIngresar.addActionListener(e -> iniciarSesion());
        getRootPane().setDefaultButton(btnIngresar);
    }

    // Botón Iniciar sesión: valida con AuthServicio y abre la ventana principal
    private void iniciarSesion() {
        lblMensaje.setText(" ");
        try {
            autenticacion.iniciarSesion(txtUsuario.getText(), txtClave.getPassword());
            new MainFrame().setVisible(true);
            dispose();
        } catch (ValidacionException e) {
            lblMensaje.setText(e.getMessage());
            txtClave.setText("");
        } catch (PersistenciaException e) {
            lblMensaje.setText("No hay conexión con la base de datos.");
            UiUtil.error(this, e.getMessage());
        }
    }
}
