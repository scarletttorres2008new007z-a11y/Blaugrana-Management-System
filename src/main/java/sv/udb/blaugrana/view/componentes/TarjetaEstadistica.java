package sv.udb.blaugrana.view.componentes;

import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Tipografia;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/**
 * Tarjeta compacta con un valor grande y una etiqueta debajo. Sirve tanto
 * para estadisticas deportivas como para cifras financieras (StatCard /
 * FinancialCard son la misma idea visual con distinto color de acento).
 */
public class TarjetaEstadistica extends JPanel {

    private final JLabel lblValor;

    public TarjetaEstadistica(String etiqueta, String valorInicial, Color colorAcento) {
        this(etiqueta, valorInicial, colorAcento, Tipografia.DATO_GRANDE);
    }

    public TarjetaEstadistica(String etiqueta, String valorInicial, Color colorAcento, Font fuenteValor) {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.BLANCO);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 3, colorAcento),
                new EmptyBorder(Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA,
                        Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA - 3)));

        JLabel lblEtiqueta = new JLabel(etiqueta.toUpperCase());
        lblEtiqueta.setFont(Tipografia.ETIQUETA);
        lblEtiqueta.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);

        lblValor = new JLabel(valorInicial);
        lblValor.setFont(fuenteValor);
        lblValor.setForeground(colorAcento);

        add(lblEtiqueta, BorderLayout.NORTH);
        add(lblValor, BorderLayout.CENTER);
    }

    public void setValor(String valor) {
        lblValor.setText(valor);
    }
}
