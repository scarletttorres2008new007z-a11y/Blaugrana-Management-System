package sv.udb.blaugrana.view.jugadores;

import sv.udb.blaugrana.model.Contrato;
import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.service.BonificacionService;
import sv.udb.blaugrana.service.ContratoService;
import sv.udb.blaugrana.service.RendimientoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FormatoMoneda;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.view.componentes.AvatarJugador;
import sv.udb.blaugrana.view.componentes.EncabezadoSeccion;
import sv.udb.blaugrana.view.componentes.Insignia;
import sv.udb.blaugrana.view.componentes.PanelDegradado;
import sv.udb.blaugrana.view.componentes.TarjetaEstadistica;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Ficha detallada de un jugador: foto/silueta, contrato vigente, rendimiento
 * acumulado (como tarjetas de estadistica) y bonificaciones totales, en una
 * sola pantalla de consulta.
 */
public class PerfilJugadorDialog extends JDialog {

    private final ContratoService contratoService = new ContratoService();
    private final RendimientoService rendimientoService = new RendimientoService();
    private final BonificacionService bonificacionService = new BonificacionService();

    public PerfilJugadorDialog(Frame propietario, Jugador jugador) {
        super(propietario, "Perfil del jugador", true);
        setSize(460, 700);
        setLocationRelativeTo(propietario);
        setResizable(false);
        construirInterfaz(jugador);
    }

    private void construirInterfaz(Jugador jugador) {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(ColoresBlaugrana.BLANCO);

        raiz.add(construirEncabezado(jugador), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBorder(new EmptyBorder(Medidas.PADDING_SECCION, Medidas.PADDING_SECCION,
                Medidas.PADDING_SECCION, Medidas.PADDING_SECCION));
        cuerpo.setBackground(ColoresBlaugrana.BLANCO);

        cuerpo.add(seccionTitulo("Contrato"));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(construirSeccionContrato(jugador));
        cuerpo.add(Box.createVerticalStrut(Medidas.PADDING_SECCION));

        cuerpo.add(seccionTitulo("Rendimiento acumulado"));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(construirSeccionRendimiento(jugador));
        cuerpo.add(Box.createVerticalStrut(Medidas.PADDING_SECCION));

        cuerpo.add(seccionTitulo("Bonificaciones acumuladas"));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(construirSeccionBonificaciones(jugador));

        JScrollPane scroll = new JScrollPane(cuerpo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        raiz.add(scroll, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.add(btnCerrar);
        raiz.add(panelBoton, BorderLayout.SOUTH);

        setContentPane(raiz);
    }

    private JPanel construirEncabezado(Jugador jugador) {
        PanelDegradado panel = new PanelDegradado(new BorderLayout(),
                ColoresBlaugrana.AZUL_OSCURO, ColoresBlaugrana.AZUL_MEDIO, true);
        panel.setBorder(new EmptyBorder(Medidas.PADDING_SECCION, Medidas.PADDING_SECCION,
                Medidas.PADDING_SECCION, Medidas.PADDING_SECCION));

        JPanel filaSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        filaSuperior.setOpaque(false);
        filaSuperior.add(new AvatarJugador(jugador.getNumeroCamiseta(), 84, 84));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblDorsal = new JLabel("Nº " + jugador.getNumeroCamiseta());
        lblDorsal.setFont(Tipografia.ETIQUETA);
        lblDorsal.setForeground(ColoresBlaugrana.DORADO_SUAVE);
        lblDorsal.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNombre = new JLabel(jugador.getNombreCompleto());
        lblNombre.setFont(Tipografia.DISPLAY);
        lblNombre.setForeground(ColoresBlaugrana.BLANCO);
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPosicion = new JLabel(String.valueOf(jugador.getPosicion()));
        lblPosicion.setFont(Tipografia.CUERPO);
        lblPosicion.setForeground(ColoresBlaugrana.DORADO_SUAVE);
        lblPosicion.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filaInsignia = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        filaInsignia.setOpaque(false);
        filaInsignia.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaInsignia.add(new Insignia(jugador.getEstado()));

        textos.add(lblDorsal);
        textos.add(lblNombre);
        textos.add(lblPosicion);
        textos.add(filaInsignia);
        filaSuperior.add(textos);

        panel.add(filaSuperior, BorderLayout.CENTER);
        return panel;
    }

    private JPanel seccionTitulo(String titulo) {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setAlignmentX(Component.LEFT_ALIGNMENT);
        envoltorio.add(new EncabezadoSeccion(titulo), BorderLayout.CENTER);
        return envoltorio;
    }

    private JPanel construirSeccionContrato(Jugador jugador) {
        JPanel tarjeta = tarjetaBase();
        try {
            List<Contrato> contratos = contratoService.listarPorJugador(jugador.getIdJugador());
            Optional<Contrato> vigente = contratos.stream().filter(c -> "VIGENTE".equals(c.getEstado())).findFirst();
            if (vigente.isPresent()) {
                Contrato contrato = vigente.get();
                BigDecimal salarioMensual = contrato.getSalarioBase().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
                tarjeta.add(filaDato("Salario mensual:", FormatoMoneda.formatear(salarioMensual)));
                tarjeta.add(filaDato("Salario anual:", FormatoMoneda.formatear(contrato.getSalarioBase())));
                tarjeta.add(filaDato("Vigencia:", contrato.getFechaInicio() + " a " + contrato.getFechaFin()));
            } else {
                tarjeta.add(filaDato("Estado:", "Sin contrato vigente"));
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el contrato", e);
        }
        return tarjeta;
    }

    private JPanel construirSeccionRendimiento(Jugador jugador) {
        JPanel grilla = new JPanel(new GridLayout(2, 2, Medidas.ESPACIO_ENTRE_TARJETAS, Medidas.ESPACIO_ENTRE_TARJETAS));
        grilla.setOpaque(false);
        grilla.setAlignmentX(Component.LEFT_ALIGNMENT);
        grilla.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        try {
            int partidos = rendimientoService.partidosJugados(jugador.getIdJugador());
            int minutos = rendimientoService.minutosAcumulados(jugador.getIdJugador());
            int goles = rendimientoService.golesAcumulados(jugador.getIdJugador());
            int asistencias = rendimientoService.asistenciasAcumuladas(jugador.getIdJugador());

            grilla.add(new TarjetaEstadistica("Partidos jugados", String.valueOf(partidos), ColoresBlaugrana.AZUL_OSCURO));
            grilla.add(new TarjetaEstadistica("Minutos jugados", String.valueOf(minutos), ColoresBlaugrana.AZUL_MEDIO));
            grilla.add(new TarjetaEstadistica("Goles", String.valueOf(goles), ColoresBlaugrana.GRANATE));
            grilla.add(new TarjetaEstadistica("Asistencias", String.valueOf(asistencias), ColoresBlaugrana.DORADO));
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el rendimiento", e);
        }
        return grilla;
    }

    private JPanel construirSeccionBonificaciones(Jugador jugador) {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        try {
            BigDecimal total = bonificacionService.totalPorJugador(jugador.getIdJugador());
            contenedor.add(new TarjetaEstadistica("Total acumulado", FormatoMoneda.formatear(total),
                    ColoresBlaugrana.GRANATE), BorderLayout.CENTER);
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar las bonificaciones", e);
        }
        return contenedor;
    }

    private JPanel tarjetaBase() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1),
                new EmptyBorder(Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA,
                        Medidas.PADDING_TARJETA, Medidas.PADDING_TARJETA)));
        return panel;
    }

    private JPanel filaDato(String etiqueta, String valor) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        fila.setOpaque(false);

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(Tipografia.CUERPO);
        lblEtiqueta.setForeground(ColoresBlaugrana.GRIS_TEXTO);

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(Tipografia.CUERPO_NEGRITA);
        lblValor.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        lblValor.setHorizontalAlignment(SwingConstants.RIGHT);

        fila.add(lblEtiqueta, BorderLayout.WEST);
        fila.add(lblValor, BorderLayout.EAST);
        return fila;
    }
}
