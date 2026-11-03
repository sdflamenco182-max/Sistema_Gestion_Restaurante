package com.restaurante;

import com.restaurante.persistencia.MongoConexion;
import com.restaurante.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> MongoConexion.cerrar()));
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
