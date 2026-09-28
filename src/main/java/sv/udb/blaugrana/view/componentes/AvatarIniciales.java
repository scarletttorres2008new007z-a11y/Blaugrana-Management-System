package sv.udb.blaugrana.view.componentes;

import sv.udb.blaugrana.util.ColoresBlaugrana;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/** Circulo con las iniciales de una persona, usado como avatar generico. */
public class AvatarIniciales extends JComponent {

    private final String iniciales;
    private final Color colorFondo;
    private final int tamano;

    public AvatarIniciales(String nombreCompleto, Color colorFondo, int tamano) {
        this.iniciales = iniciales(nombreCompleto);
        this.colorFondo = colorFondo;
        this.tamano = tamano;
        setPreferredSize(new Dimension(tamano, tamano));
        setOpaque(false);
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] palabras = nombre.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty() && resultado.length() < 2) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }
        return resultado.length() > 0 ? resultado.toString() : "?";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(colorFondo);
        g2.fill(new Ellipse2D.Float(0, 0, tamano, tamano));

        g2.setColor(ColoresBlaugrana.BLANCO);
        g2.setFont(getFont().deriveFont(Font.BOLD, tamano * 0.4f));
        var metricas = g2.getFontMetrics();
        int x = (tamano - metricas.stringWidth(iniciales)) / 2;
        int y = (tamano + metricas.getAscent() - metricas.getDescent()) / 2;
        g2.drawString(iniciales, x, y);
        g2.dispose();
    }
}
