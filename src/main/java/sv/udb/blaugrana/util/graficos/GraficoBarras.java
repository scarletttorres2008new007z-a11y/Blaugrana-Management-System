package sv.udb.blaugrana.util.graficos;

import sv.udb.blaugrana.util.ColoresBlaugrana;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Grafico de barras horizontales dibujado con Java2D puro, sin depender de
 * ninguna libreria externa. Pensado para listas cortas de categorias
 * (posiciones, categorias financieras, jugadores destacados, etc.).
 */
public class GraficoBarras extends JPanel {

    private List<String> etiquetas = List.of();
    private List<Double> valores = List.of();
    private String titulo = "";
    private Color colorBarra = ColoresBlaugrana.GRANATE;

    public GraficoBarras() {
        setBackground(ColoresBlaugrana.BLANCO);
    }

    public void setColorBarra(Color color) {
        this.colorBarra = color;
    }

    public void setDatos(String titulo, List<String> etiquetas, List<Double> valores) {
        this.titulo = titulo;
        this.etiquetas = etiquetas;
        this.valores = valores;
        int alturaSugerida = Math.max(120, etiquetas.size() * 32 + 40);
        setPreferredSize(new Dimension(getPreferredSize().width, alturaSugerida));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        int margenSuperior = 12;
        if (!titulo.isEmpty()) {
            g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
            g2.setColor(ColoresBlaugrana.AZUL_OSCURO);
            g2.drawString(titulo, 8, margenSuperior + 10);
            margenSuperior += 22;
        }

        if (etiquetas.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("Sin datos para mostrar.", 12, alto / 2);
            return;
        }

        int margenIzquierdo = 130;
        int margenDerecho = 60;
        int margenInferior = 8;

        double maximo = valores.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (maximo <= 0) {
            maximo = 1;
        }

        int filas = etiquetas.size();
        int altoFila = Math.max(20, (alto - margenSuperior - margenInferior) / filas);
        int altoBarra = Math.max(10, altoFila - 10);
        int anchoDisponible = Math.max(20, ancho - margenIzquierdo - margenDerecho);

        g2.setFont(getFont().deriveFont(12f));
        FontMetrics metricas = g2.getFontMetrics();

        for (int i = 0; i < filas; i++) {
            int y = margenSuperior + i * altoFila + (altoFila - altoBarra) / 2;
            double valor = valores.get(i);
            int anchoBarra = (int) Math.round((valor / maximo) * anchoDisponible);

            String etiqueta = etiquetas.get(i);
            int maxAnchoTexto = margenIzquierdo - 10;
            while (metricas.stringWidth(etiqueta) > maxAnchoTexto && etiqueta.length() > 3) {
                etiqueta = etiqueta.substring(0, etiqueta.length() - 2);
            }
            g2.setColor(ColoresBlaugrana.GRIS_TEXTO);
            g2.drawString(etiqueta, 4, y + altoBarra - 2);

            g2.setColor(colorBarra);
            g2.fillRoundRect(margenIzquierdo, y, Math.max(anchoBarra, 2), altoBarra, 6, 6);

            g2.setColor(ColoresBlaugrana.GRIS_TEXTO);
            g2.drawString(formatearValor(valor), margenIzquierdo + anchoBarra + 6, y + altoBarra - 2);
        }
    }

    private String formatearValor(double valor) {
        if (valor == Math.floor(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format("%.2f", valor);
    }
}
