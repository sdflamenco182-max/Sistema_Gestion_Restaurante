package com.restaurante;

import com.restaurante.persistencia.MongoConexion;
import com.restaurante.ui.LoginFrame;

import javax.swing.SwingUtilities;

// Clase principal: arranca el programa y abre la ventana de login
public class App {

    // Punto de entrada: cierra Mongo al terminar y muestra el login
    public static void main(String[] args) {
        // Al cerrar el programa se cierra la conexión a la base
        Runtime.getRuntime().addShutdownHook(new Thread(() -> MongoConexion.cerrar()));
        // Abre el login en el hilo de Swing
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
