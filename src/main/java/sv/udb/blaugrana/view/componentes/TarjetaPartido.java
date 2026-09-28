package sv.udb.blaugrana.view.componentes;

import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Tipografia;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * Tarjeta de partido al estilo de un "match card" deportivo: escudos,
 * marcador o "VS", competicion y fecha. Se usa tanto en grande (proximo
 * partido, en el Dashboard) como en formato compacto (lista de resultados).
 */
public class TarjetaPartido extends JPanel {

    public TarjetaPartido(Partido partido, boolean destacado) {
        setOpaque(false);
        setLayout(new BorderLayout(0, destacado ? 10 : 4));

        int tamanoEscudo = destacado ? 64 : 36;
        boolean finalizado = Partido.ESTADO_FINALIZADO.equals(partido.getEstado());

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);

        JPanel bloqueLocal = bloqueEquipo("FC Barcelona", true, tamanoEscudo, destacado);
        JPanel bloqueVisitante = bloqueEquipo(partido.getRival(), false, tamanoEscudo, destacado);

        JLabel centro = new JLabel(finalizado ? partido.getMarcador() : "VS", SwingConstants.CENTER);
        centro.setFont(destacado ? Tipografia.DISPLAY : Tipografia.SUBTITULO);
        centro.setForeground(finalizado ? ColoresBlaugrana.AZUL_OSCURO : ColoresBlaugrana.GRIS_TEXTO_SUAVE);
        centro.setBorder(new EmptyBorder(0, 12, 0, 12));

        fila.add(bloqueLocal, BorderLayout.WEST);
        fila.add(centro, BorderLayout.CENTER);
        fila.add(bloqueVisitante, BorderLayout.EAST);

        JLabel pie = new JLabel(pie(partido), SwingConstants.CENTER);
        pie.setFont(Tipografia.NOTA);
        pie.setForeground(ColoresBlaugrana.GRIS_TEXTO_SUAVE);
        pie.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(fila, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    private String pie(Partido partido) {
        String resultado = Partido.ESTADO_FINALIZADO.equals(partido.getEstado())
                ? " · " + partido.getResultado()
                : "";
        return partido.getCompeticion() + " · " + partido.getFecha() + resultado;
    }

    private JPanel bloqueEquipo(String nombre, boolean esClubPropio, int tamanoEscudo, boolean destacado) {
        JPanel bloque = new JPanel();
        bloque.setOpaque(false);
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));

        EscudoEquipo escudo = new EscudoEquipo(nombre, esClubPropio, tamanoEscudo);
        escudo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(destacado ? Tipografia.CUERPO_NEGRITA : Tipografia.NOTA);
        lblNombre.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setHorizontalAlignment(SwingConstants.CENTER);

        bloque.add(escudo);
        bloque.add(lblNombre);
        return bloque;
    }
}
