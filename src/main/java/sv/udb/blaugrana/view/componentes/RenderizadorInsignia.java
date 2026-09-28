package sv.udb.blaugrana.view.componentes;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.FlowLayout;

/**
 * Renderer de celda de tabla que dibuja una {@link Insignia} en vez de texto
 * plano, para columnas de estado (ACTIVO/INACTIVO, VIGENTE/RESCINDIDO,
 * PAGADO/PENDIENTE, etc.), reutilizando el mismo lenguaje visual de las
 * tarjetas en todas las tablas de la aplicacion.
 */
public class RenderizadorInsignia extends JPanel implements TableCellRenderer {

    public RenderizadorInsignia() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        removeAll();
        add(new Insignia(value == null ? "" : value.toString()));
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return this;
    }
}
