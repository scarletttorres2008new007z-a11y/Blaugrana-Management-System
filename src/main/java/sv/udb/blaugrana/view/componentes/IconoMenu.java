package sv.udb.blaugrana.view.componentes;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Familia unica de iconos vectoriales (dibujados con Java2D, sin depender
 * de ninguna libreria de iconos ni de fuentes de emojis) para el menu
 * lateral. Implementa {@link Icon} en vez de ser un JComponent para poder
 * asignarse directamente a un JButton; toma el color del componente que lo
 * pinta (getForeground del boton), asi que el mismo icono se ve blanco,
 * resaltado o dorado segun el estado del boton, sin crear instancias
 * distintas por estado.
 */
public class IconoMenu implements Icon {

    public enum Tipo {
        DASHBOARD, JUGADORES, PERSONAL, CONTRATOS, PARTIDOS, RENDIMIENTO,
        BONIFICACIONES, PAGOS, INGRESOS, EGRESOS, PRESUPUESTO, REPORTES, USUARIOS
    }

    private final Tipo tipo;
    private final int tamano;

    public IconoMenu(Tipo tipo, int tamano) {
        this.tipo = tipo;
        this.tamano = tamano;
    }

    @Override
    public int getIconWidth() {
        return tamano;
    }

    @Override
    public int getIconHeight() {
        return tamano;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setStroke(new BasicStroke(Math.max(1.4f, tamano * 0.09f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        float w = tamano;
        float h = tamano;
        switch (tipo) {
            case DASHBOARD -> {
                float s = w * 0.38f;
                g2.draw(new RoundRectangle2D.Float(w * 0.08f, h * 0.08f, s, s, 3, 3));
                g2.draw(new RoundRectangle2D.Float(w * 0.54f, h * 0.08f, s, s, 3, 3));
                g2.draw(new RoundRectangle2D.Float(w * 0.08f, h * 0.54f, s, s, 3, 3));
                g2.draw(new RoundRectangle2D.Float(w * 0.54f, h * 0.54f, s, s, 3, 3));
            }
            case JUGADORES, PERSONAL -> {
                g2.draw(new Ellipse2D.Float(w * 0.32f, h * 0.12f, w * 0.36f, w * 0.36f));
                Path2D hombros = new Path2D.Float();
                hombros.moveTo(w * 0.15f, h * 0.92f);
                hombros.curveTo(w * 0.15f, h * 0.58f, w * 0.85f, h * 0.58f, w * 0.85f, h * 0.92f);
                g2.draw(hombros);
            }
            case CONTRATOS, REPORTES -> {
                g2.draw(new RoundRectangle2D.Float(w * 0.2f, h * 0.08f, w * 0.6f, h * 0.84f, 4, 4));
                g2.draw(new Line2D.Float(w * 0.32f, h * 0.32f, w * 0.68f, h * 0.32f));
                g2.draw(new Line2D.Float(w * 0.32f, h * 0.5f, w * 0.68f, h * 0.5f));
                g2.draw(new Line2D.Float(w * 0.32f, h * 0.68f, w * 0.56f, h * 0.68f));
            }
            case PARTIDOS -> {
                g2.draw(new Ellipse2D.Float(w * 0.1f, h * 0.1f, w * 0.8f, h * 0.8f));
                g2.draw(new Ellipse2D.Float(w * 0.38f, h * 0.38f, w * 0.24f, h * 0.24f));
            }
            case RENDIMIENTO -> {
                g2.draw(new Line2D.Float(w * 0.2f, h * 0.85f, w * 0.2f, h * 0.55f));
                g2.draw(new Line2D.Float(w * 0.5f, h * 0.85f, w * 0.5f, h * 0.35f));
                g2.draw(new Line2D.Float(w * 0.8f, h * 0.85f, w * 0.8f, h * 0.15f));
            }
            case BONIFICACIONES -> {
                g2.draw(new Ellipse2D.Float(w * 0.15f, h * 0.15f, w * 0.7f, h * 0.7f));
                g2.setFont(g2.getFont().deriveFont(tamano * 0.42f));
                var fm = g2.getFontMetrics();
                String simbolo = "$";
                g2.drawString(simbolo, (w - fm.stringWidth(simbolo)) / 2f, h * 0.68f);
            }
            case PAGOS -> {
                g2.draw(new RoundRectangle2D.Float(w * 0.08f, h * 0.22f, w * 0.84f, h * 0.56f, 5, 5));
                g2.draw(new Line2D.Float(w * 0.08f, h * 0.4f, w * 0.92f, h * 0.4f));
            }
            case INGRESOS -> dibujarFlecha(g2, w, h, true);
            case EGRESOS -> dibujarFlecha(g2, w, h, false);
            case PRESUPUESTO -> {
                g2.draw(new Ellipse2D.Float(w * 0.1f, h * 0.1f, w * 0.8f, h * 0.8f));
                Path2D radio = new Path2D.Float();
                radio.moveTo(w * 0.5f, h * 0.5f);
                radio.lineTo(w * 0.5f, h * 0.1f);
                radio.moveTo(w * 0.5f, h * 0.5f);
                radio.lineTo(w * 0.82f, h * 0.68f);
                g2.draw(radio);
            }
            case USUARIOS -> {
                g2.draw(new Ellipse2D.Float(w * 0.28f, h * 0.1f, w * 0.3f, w * 0.3f));
                Path2D hombros = new Path2D.Float();
                hombros.moveTo(w * 0.12f, h * 0.72f);
                hombros.curveTo(w * 0.12f, h * 0.46f, w * 0.68f, h * 0.46f, w * 0.68f, h * 0.72f);
                g2.draw(hombros);
                g2.draw(new Ellipse2D.Float(w * 0.62f, h * 0.55f, w * 0.28f, w * 0.28f));
            }
        }
        g2.dispose();
    }

    private void dibujarFlecha(Graphics2D g2, float w, float h, boolean subiendo) {
        float y1 = subiendo ? h * 0.8f : h * 0.2f;
        float y2 = subiendo ? h * 0.2f : h * 0.8f;
        g2.draw(new Line2D.Float(w * 0.2f, y1, w * 0.8f, y2));
        Path2D punta = new Path2D.Float();
        float dir = subiendo ? 1f : -1f;
        punta.moveTo(w * 0.8f, y2);
        punta.lineTo(w * 0.8f - w * 0.22f, y2 + dir * h * 0.02f);
        punta.moveTo(w * 0.8f, y2);
        punta.lineTo(w * 0.8f - w * 0.02f, y2 + dir * h * 0.24f);
        g2.draw(punta);
    }
}
