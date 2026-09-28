package sv.udb.blaugrana.view.componentes;

import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.RecursosExternos;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Escudo de un equipo. Para el club propio, busca un archivo real en
 * recursos/escudo.png (que el usuario coloca bajo su propia licencia de
 * uso); si no existe, o para el rival, dibuja un escudo generico con las
 * iniciales del equipo -nunca un logo real inventado o descargado-.
 */
public class EscudoEquipo extends JComponent {

    private final int tamano;
    private final String iniciales;
    private final Color color;
    private final BufferedImage escudoReal;

    public EscudoEquipo(String nombreEquipo, boolean esClubPropio, int tamano) {
        this.tamano = tamano;
        this.iniciales = iniciales(nombreEquipo);
        this.color = esClubPropio ? ColoresBlaugrana.GRANATE : ColoresBlaugrana.AZUL_MEDIO;
        this.escudoReal = esClubPropio ? cargarEscudoReal() : null;
        setPreferredSize(new Dimension(tamano, tamano));
        setOpaque(false);
    }

    private BufferedImage cargarEscudoReal() {
        var archivo = RecursosExternos.escudoClub();
        if (!archivo.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(archivo);
        } catch (IOException e) {
            return null;
        }
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] palabras = nombre.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty() && resultado.length() < 3) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }
        return resultado.length() > 0 ? resultado.toString() : "?";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (escudoReal != null) {
            g2.drawImage(escudoReal, 0, 0, tamano, tamano, null);
            g2.dispose();
            return;
        }

        GeneralPath forma = formaEscudo();
        g2.setColor(color);
        g2.fill(forma);
        g2.setColor(ColoresBlaugrana.DORADO);
        g2.setStroke(new java.awt.BasicStroke(Math.max(1.5f, tamano * 0.02f)));
        g2.draw(forma);

        g2.setColor(ColoresBlaugrana.BLANCO);
        int tamanoFuente = (int) (tamano * 0.34);
        g2.setFont(getFont().deriveFont(Font.BOLD, tamanoFuente));
        var metricas = g2.getFontMetrics();
        int x = (tamano - metricas.stringWidth(iniciales)) / 2;
        int y = (tamano + metricas.getAscent() - metricas.getDescent()) / 2;
        g2.drawString(iniciales, x, y);

        g2.dispose();
    }

    private GeneralPath formaEscudo() {
        float w = tamano;
        float h = tamano;
        GeneralPath forma = new GeneralPath();
        forma.moveTo(w * 0.5f, 0);
        forma.lineTo(w * 0.95f, h * 0.18f);
        forma.lineTo(w * 0.95f, h * 0.55f);
        forma.curveTo(w * 0.95f, h * 0.85f, w * 0.7f, h * 0.98f, w * 0.5f, h);
        forma.curveTo(w * 0.3f, h * 0.98f, w * 0.05f, h * 0.85f, w * 0.05f, h * 0.55f);
        forma.lineTo(w * 0.05f, h * 0.18f);
        forma.closePath();
        return forma;
    }
}
