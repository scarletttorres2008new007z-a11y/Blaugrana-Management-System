package sv.udb.blaugrana.util;

import javax.swing.JTable;
import java.awt.Component;
import java.awt.print.PrinterException;
import java.text.MessageFormat;

/**
 * Envia una tabla a impresion usando el mecanismo nativo de Swing
 * (JTable.print), que abre el dialogo de impresion del sistema operativo.
 * En Windows, elegir la impresora "Microsoft Print to PDF" produce un PDF
 * sin necesidad de ninguna libreria adicional.
 */
public final class Impresion {

    private Impresion() {
    }

    public static void imprimirTabla(Component padre, JTable tabla, String titulo) {
        try {
            boolean completado = tabla.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat(titulo), new MessageFormat("Pagina {0} de {1}"));
            if (!completado) {
                return;
            }
        } catch (PrinterException e) {
            Mensajes.error(padre, "No se pudo imprimir:\n" + e.getMessage());
        }
    }
}
