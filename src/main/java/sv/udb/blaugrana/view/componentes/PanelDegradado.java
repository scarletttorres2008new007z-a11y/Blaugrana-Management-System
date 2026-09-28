package sv.udb.blaugrana.view.componentes;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

/** Panel con fondo en degradado lineal, para encabezados y sidebar. */
public class PanelDegradado extends JPanel {

    private final Color inicio;
    private final Color fin;
    private final boolean horizontal;

    public PanelDegradado(LayoutManager layout, Color inicio, Color fin, boolean horizontal) {
        super(layout);
        this.inicio = inicio;
        this.fin = fin;
        this.horizontal = horizontal;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        GradientPaint degradado = horizontal
                ? new GradientPaint(0, 0, inicio, getWidth(), 0, fin)
                : new GradientPaint(0, 0, inicio, 0, getHeight(), fin);
        g2.setPaint(degradado);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
