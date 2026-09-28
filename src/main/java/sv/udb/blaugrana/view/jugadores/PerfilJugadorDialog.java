package sv.udb.blaugrana.view.jugadores;

import sv.udb.blaugrana.model.Contrato;
import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.service.BonificacionService;
import sv.udb.blaugrana.service.ContratoService;
import sv.udb.blaugrana.service.RendimientoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FormatoMoneda;
import sv.udb.blaugrana.util.Mensajes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Ficha resumen de un jugador: contrato vigente, rendimiento acumulado y
 * bonificaciones totales, en una sola pantalla de consulta.
 */
public class PerfilJugadorDialog extends JDialog {

    private final ContratoService contratoService = new ContratoService();
    private final RendimientoService rendimientoService = new RendimientoService();
    private final BonificacionService bonificacionService = new BonificacionService();

    public PerfilJugadorDialog(Frame propietario, Jugador jugador) {
        super(propietario, "Perfil del jugador", true);
        setSize(420, 560);
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
        cuerpo.setBorder(new EmptyBorder(15, 20, 15, 20));
        cuerpo.setBackground(ColoresBlaugrana.BLANCO);

        cuerpo.add(construirSeccionContrato(jugador));
        cuerpo.add(Box.createVerticalStrut(15));
        cuerpo.add(construirSeccionRendimiento(jugador));
        cuerpo.add(Box.createVerticalStrut(15));
        cuerpo.add(construirSeccionBonificaciones(jugador));

        raiz.add(cuerpo, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.add(btnCerrar);
        raiz.add(panelBoton, BorderLayout.SOUTH);

        setContentPane(raiz);
    }

    private JPanel construirEncabezado(Jugador jugador) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColoresBlaugrana.AZUL_OSCURO);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblNumero = new JLabel("#" + jugador.getNumeroCamiseta());
        lblNumero.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblNumero.setForeground(ColoresBlaugrana.DORADO);
        lblNumero.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNombre = new JLabel(jugador.getNombreCompleto());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblNombre.setForeground(ColoresBlaugrana.BLANCO);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPosicion = new JLabel(jugador.getPosicion());
        lblPosicion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPosicion.setForeground(ColoresBlaugrana.GRIS_CLARO);
        lblPosicion.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblNumero);
        panel.add(lblNombre);
        panel.add(lblPosicion);
        return panel;
    }

    private JPanel construirSeccionContrato(Jugador jugador) {
        JPanel seccion = seccionBase("CONTRATO");
        try {
            List<Contrato> contratos = contratoService.listarPorJugador(jugador.getIdJugador());
            Optional<Contrato> vigente = contratos.stream().filter(c -> "VIGENTE".equals(c.getEstado())).findFirst();
            if (vigente.isPresent()) {
                Contrato contrato = vigente.get();
                BigDecimal salarioMensual = contrato.getSalarioBase().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
                seccion.add(filaDato("Salario mensual:", FormatoMoneda.formatear(salarioMensual)));
                seccion.add(filaDato("Salario anual:", FormatoMoneda.formatear(contrato.getSalarioBase())));
                seccion.add(filaDato("Vigencia:", contrato.getFechaInicio() + " a " + contrato.getFechaFin()));
            } else {
                seccion.add(filaDato("Estado:", "Sin contrato vigente"));
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el contrato", e);
        }
        return seccion;
    }

    private JPanel construirSeccionRendimiento(Jugador jugador) {
        JPanel seccion = seccionBase("RENDIMIENTO ACUMULADO");
        try {
            int partidos = rendimientoService.partidosJugados(jugador.getIdJugador());
            int minutos = rendimientoService.minutosAcumulados(jugador.getIdJugador());
            int goles = rendimientoService.golesAcumulados(jugador.getIdJugador());
            int asistencias = rendimientoService.asistenciasAcumuladas(jugador.getIdJugador());

            seccion.add(filaDato("Partidos jugados:", String.valueOf(partidos)));
            seccion.add(filaDato("Minutos jugados:", String.valueOf(minutos)));
            seccion.add(filaDato("Goles:", String.valueOf(goles)));
            seccion.add(filaDato("Asistencias:", String.valueOf(asistencias)));
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el rendimiento", e);
        }
        return seccion;
    }

    private JPanel construirSeccionBonificaciones(Jugador jugador) {
        JPanel seccion = seccionBase("BONIFICACIONES ACUMULADAS");
        try {
            BigDecimal total = bonificacionService.totalPorJugador(jugador.getIdJugador());
            JLabel lblTotal = new JLabel(FormatoMoneda.formatear(total));
            lblTotal.setFont(new Font("SansSerif", Font.BOLD, 22));
            lblTotal.setForeground(ColoresBlaugrana.GRANATE);
            lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
            seccion.add(lblTotal);
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar las bonificaciones", e);
        }
        return seccion;
    }

    private JPanel seccionBase(String titulo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, ColoresBlaugrana.GRIS_CLARO),
                new EmptyBorder(0, 0, 0, 0)));
        panel.setBackground(ColoresBlaugrana.BLANCO);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(lblTitulo);
        return panel;
    }

    private JPanel filaDato(String etiqueta, String valor) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        fila.setBackground(ColoresBlaugrana.BLANCO);

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblEtiqueta.setForeground(ColoresBlaugrana.GRIS_TEXTO);

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblValor.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        lblValor.setHorizontalAlignment(SwingConstants.RIGHT);

        fila.add(lblEtiqueta, BorderLayout.WEST);
        fila.add(lblValor, BorderLayout.EAST);
        return fila;
    }
}
