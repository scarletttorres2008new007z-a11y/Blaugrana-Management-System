package sv.udb.blaugrana.view.componentes;

import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Tipografia;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Set;

/**
 * Etiqueta tipo "pill" con color segun el estado (ACTIVO, VIGENTE,
 * PENDIENTE, etc.), usada en tablas y tarjetas en lugar de texto plano.
 */
public class Insignia extends JComponent {

    private static final Set<String> POSITIVOS = Set.of("ACTIVO", "VIGENTE", "PAGADO", "GANADO", "FINALIZADO");
    private static final Set<String> NEGATIVOS = Set.of("INACTIVO", "RESCINDIDO", "PERDIDO");
    private static final Set<String> ALERTA = Set.of("PENDIENTE", "PROGRAMADO", "EMPATADO");

    private final String texto;
    private final Color colorFondo;

    public Insignia(String texto) {
        this.texto = texto == null ? "" : texto;
        this.colorFondo = colorPara(this.texto.toUpperCase());
        setFont(Tipografia.ETIQUETA);
        setOpaque(false);
    }

    private Color colorPara(String valor) {
        if (POSITIVOS.contains(valor)) {
            return ColoresBlaugrana.VERDE_ACTIVO;
        }
        if (NEGATIVOS.contains(valor)) {
            return ColoresBlaugrana.ROJO_ALERTA;
        }
        if (ALERTA.contains(valor)) {
            return ColoresBlaugrana.AMBAR_ALERTA;
        }
        return ColoresBlaugrana.AZUL_MEDIO;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metricas = getFontMetrics(getFont());
        int ancho = metricas.stringWidth(texto) + 20;
        int alto = metricas.getHeight() + 6;
        return new Dimension(ancho, alto);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(colorFondo);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Medidas.RADIO_INSIGNIA, Medidas.RADIO_INSIGNIA);

        g2.setColor(ColoresBlaugrana.BLANCO);
        g2.setFont(getFont());
        FontMetrics metricas = g2.getFontMetrics();
        int x = (getWidth() - metricas.stringWidth(texto)) / 2;
        int y = (getHeight() + metricas.getAscent() - metricas.getDescent()) / 2;
        g2.drawString(texto, x, y);
        g2.dispose();
    }
}
