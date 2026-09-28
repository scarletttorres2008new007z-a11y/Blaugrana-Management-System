package sv.udb.blaugrana.util;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.SQLException;

public final class Mensajes {

    private Mensajes() {
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Blaugrana Management", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Blaugrana Management", JOptionPane.ERROR_MESSAGE);
    }

    public static void error(Component padre, String contexto, SQLException causa) {
        error(padre, contexto + ":\n" + ErroresBD.mensajeAmigable(causa));
    }

    public static boolean confirmar(Component padre, String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return opcion == JOptionPane.YES_OPTION;
    }
}
