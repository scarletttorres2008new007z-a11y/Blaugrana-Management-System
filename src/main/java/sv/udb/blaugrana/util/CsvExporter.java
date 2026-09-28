package sv.udb.blaugrana.util;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Exporta datos tabulares a un archivo CSV (se abre directamente en Excel
 * o cualquier hoja de calculo), sin depender de ninguna libreria externa.
 */
public final class CsvExporter {

    private CsvExporter() {
    }

    public static void exportarTabla(Component padre, String nombreSugerido, JTable tabla) {
        TableModel modelo = tabla.getModel();
        int columnas = modelo.getColumnCount();
        String[] encabezados = new String[columnas];
        for (int c = 0; c < columnas; c++) {
            encabezados[c] = modelo.getColumnName(c);
        }

        List<Object[]> filas = new ArrayList<>();
        for (int f = 0; f < modelo.getRowCount(); f++) {
            Object[] fila = new Object[columnas];
            for (int c = 0; c < columnas; c++) {
                fila[c] = modelo.getValueAt(f, c);
            }
            filas.add(fila);
        }

        exportar(padre, nombreSugerido, encabezados, filas);
    }

    public static void exportar(Component padre, String nombreSugerido, String[] columnas, List<Object[]> filas) {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File(nombreSugerido));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        try (PrintWriter escritor = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            escritor.println(String.join(",", escapar(columnas)));
            for (Object[] fila : filas) {
                String[] valores = new String[fila.length];
                for (int i = 0; i < fila.length; i++) {
                    valores[i] = fila[i] == null ? "" : fila[i].toString();
                }
                escritor.println(String.join(",", escapar(valores)));
            }
            Mensajes.info(padre, "Archivo exportado correctamente:\n" + archivo.getAbsolutePath());
        } catch (IOException e) {
            Mensajes.error(padre, "No se pudo exportar el archivo:\n" + e.getMessage());
        }
    }

    private static String[] escapar(String[] valores) {
        String[] resultado = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            resultado[i] = escapar(valores[i]);
        }
        return resultado;
    }

    private static String escapar(String valor) {
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
