package sv.udb.blaugrana;

import sv.udb.blaugrana.view.FrmLogin;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // Se conserva el look and feel por defecto de Swing.
        }

        SwingUtilities.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}
