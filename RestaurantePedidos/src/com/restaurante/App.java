package com.restaurante;

import com.restaurante.excepcion.PersistenciaException;
import com.restaurante.persistencia.MongoConexion;
import com.restaurante.servicio.InicializadorBD;
import com.restaurante.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {

    public static void main(String[] args) {
        if (args.length > 0 && "--init-bd".equals(args[0])) {
            inicializarBaseDatos();
            return;
        }
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

    private static void inicializarBaseDatos() {
        try {
            if (InicializadorBD.ejecutar()) {
                System.out.println("Base de datos inicializada. Usuario de prueba: "
                        + InicializadorBD.USUARIO_PRUEBA + " / " + InicializadorBD.CLAVE_PRUEBA);
            } else {
                System.out.println("La base de datos ya contiene datos; no se realizaron cambios.");
            }
        } catch (PersistenciaException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            MongoConexion.cerrar();
        }
    }
}
