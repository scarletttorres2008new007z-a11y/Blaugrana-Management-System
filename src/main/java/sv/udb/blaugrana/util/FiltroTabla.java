package sv.udb.blaugrana.util;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.text.Normalizer;

/**
 * Conecta un campo de texto de busqueda con un JTable: a medida que el
 * usuario escribe, filtra las filas cuyo contenido (en cualquier columna)
 * coincide, sin distinguir mayusculas/minusculas ni acentos.
 */
public final class FiltroTabla {

    private FiltroTabla() {
    }

    public static void activarBusqueda(javax.swing.JTextField campoBusqueda, JTable tabla, DefaultTableModel modelo) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }

            private void filtrar() {
                String texto = normalizar(campoBusqueda.getText());
                if (texto.isBlank()) {
                    sorter.setRowFilter(null);
                    return;
                }
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        for (int i = 0; i < entry.getValueCount(); i++) {
                            Object valor = entry.getValue(i);
                            if (valor != null && normalizar(valor.toString()).contains(texto)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }
        });
    }

    private static String normalizar(String texto) {
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toLowerCase();
    }
}
