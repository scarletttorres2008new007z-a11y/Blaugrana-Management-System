package sv.udb.blaugrana.view.componentes;

import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.RecursosExternos;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Espacio de foto de un jugador. Si existe un archivo de imagen en
 * recursos/jugadores/&lt;numeroCamiseta&gt;.(jpg|png) lo muestra recortado
 * de forma consistente; si no existe, muestra un marcador generico de
 * "sin foto" (nunca una imagen inventada o generada).
 */
public class AvatarJugador extends JComponent {

    private final int ancho;
    private final int alto;
    private final BufferedImage foto;

    public AvatarJugador(int numeroCamiseta, int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
        this.foto = cargarFoto(numeroCamiseta);
        setPreferredSize(new Dimension(ancho, alto));
        setOpaque(false);
    }

    private BufferedImage cargarFoto(int numeroCamiseta) {
        File archivo = RecursosExternos.buscarFotoJugador(numeroCamiseta);
        if (archivo == null) {
            return null;
        }
        try {
            return ImageIO.read(archivo);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D marco = new RoundRectangle2D.Float(0, 0, ancho, alto, 16, 16);
        g2.setClip(marco);

        if (foto != null) {
            dibujarFotoEscalada(g2);
        } else {
            dibujarMarcadorSinFoto(g2);
        }

        g2.dispose();
    }

    private void dibujarFotoEscalada(Graphics2D g2) {
        double escala = Math.max((double) ancho / foto.getWidth(), (double) alto / foto.getHeight());
        int anchoEscalado = (int) Math.ceil(foto.getWidth() * escala);
        int altoEscalado = (int) Math.ceil(foto.getHeight() * escala);
        int x = (ancho - anchoEscalado) / 2;
        int y = (alto - altoEscalado) / 2;
        g2.drawImage(foto, x, y, anchoEscalado, altoEscalado, null);
    }

    private void dibujarMarcadorSinFoto(Graphics2D g2) {
        g2.setColor(ColoresBlaugrana.GRIS_CLARO);
        g2.fillRect(0, 0, ancho, alto);

        // Silueta generica: cabeza (circulo) + hombros (elipse recortada)
        Color trazo = ColoresBlaugrana.GRIS_MEDIO;
        g2.setColor(trazo);

        int diametroCabeza = (int) (Math.min(ancho, alto) * 0.32);
        int xCabeza = (ancho - diametroCabeza) / 2;
        int yCabeza = (int) (alto * 0.18);
        g2.fill(new Ellipse2D.Float(xCabeza, yCabeza, diametroCabeza, diametroCabeza));

        int anchoHombros = (int) (ancho * 0.78);
        int altoHombros = (int) (alto * 0.42);
        int xHombros = (ancho - anchoHombros) / 2;
        int yHombros = (int) (alto * 0.58);
        g2.fill(new Ellipse2D.Float(xHombros, yHombros, anchoHombros, altoHombros));

        if (alto > 70) {
            g2.setColor(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
            g2.setFont(getFont().deriveFont(10f));
            String texto = "Sin foto";
            int anchoTexto = g2.getFontMetrics().stringWidth(texto);
            g2.drawString(texto, (ancho - anchoTexto) / 2f, alto - 8f);
        }
    }
}
