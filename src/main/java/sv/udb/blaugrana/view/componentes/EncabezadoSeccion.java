package sv.udb.blaugrana.view.componentes;

import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Tipografia;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * Titulo de seccion con una barra de acento a la izquierda, usado para dar
 * jerarquia visual entre bloques de contenido sin encerrar todo en cajas.
 */
public class EncabezadoSeccion extends JPanel {

    public EncabezadoSeccion(String titulo) {
        this(titulo, null);
    }

    public EncabezadoSeccion(String titulo, String subtitulo) {
        setOpaque(false);
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel barra = new JPanel();
        barra.setBackground(ColoresBlaugrana.DORADO);
        barra.setPreferredSize(new java.awt.Dimension(5, 26));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tipografia.TITULO);
        lblTitulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(lblTitulo);

        if (subtitulo != null && !subtitulo.isBlank()) {
            JLabel lblSubtitulo = new JLabel(subtitulo);
            lblSubtitulo.setFont(Tipografia.NOTA);
            lblSubtitulo.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
            lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            textos.add(lblSubtitulo);
        }

        add(barra, BorderLayout.WEST);
        add(textos, BorderLayout.CENTER);
    }
}
