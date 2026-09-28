package sv.udb.blaugrana.util;

import sv.udb.blaugrana.session.SesionUsuario;

import javax.swing.AbstractButton;

/**
 * Ayuda a los paneles Swing a respetar el modo de solo lectura de roles
 * como Directivo: los botones que crean, modifican o eliminan datos se
 * deshabilitan, sin afectar los de consulta (Nuevo, Refrescar, etc.).
 */
public final class PermisosUI {

    private PermisosUI() {
    }

    public static void deshabilitarSiSoloLectura(AbstractButton... botones) {
        if (!SesionUsuario.esSoloLectura()) {
            return;
        }
        for (AbstractButton boton : botones) {
            boton.setEnabled(false);
        }
    }
}
