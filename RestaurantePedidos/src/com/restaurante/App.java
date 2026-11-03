package com.restaurante;

import com.restaurante.persistencia.MongoConexion;
import com.restaurante.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {
        usarAspectoNimbus();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> MongoConexion.cerrar()));
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private static void usarAspectoNimbus() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el aspecto Nimbus.");
        }
    }
}
