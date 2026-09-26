package sv.udb.blaugrana.view;

import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.service.ContratoService;
import sv.udb.blaugrana.service.FinanzasService;
import sv.udb.blaugrana.service.JugadorService;
import sv.udb.blaugrana.service.PartidoService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FormatoMoneda;
import sv.udb.blaugrana.util.Mensajes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class DashboardPanel extends JPanel implements Refrescable {

    private final JugadorService jugadorService = new JugadorService();
    private final ContratoService contratoService = new ContratoService();
    private final PartidoService partidoService = new PartidoService();
    private final FinanzasService finanzasService = new FinanzasService();

    private final JLabel lblPlantilla = valorTarjeta("0");
    private final JLabel lblContratos = valorTarjeta("0");
    private final JLabel lblPartidos = valorTarjeta("0");
    private final JLabel lblProximoPartido = new JLabel("Sin partidos programados");
    private final JLabel lblIngresos = new JLabel("$0.00");
    private final JLabel lblEgresos = new JLabel("$0.00");
    private final JLabel lblBalance = new JLabel("$0.00");

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(ColoresBlaugrana.GRIS_CLARO);

        JLabel titulo = new JLabel("FC BARCELONA - Panel general del club");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(titulo, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new GridLayout(2, 1, 15, 15));
        contenido.setOpaque(false);

        JPanel filaTarjetas = new JPanel(new GridLayout(1, 3, 15, 15));
        filaTarjetas.setOpaque(false);
        filaTarjetas.add(tarjeta("PLANTILLA", lblPlantilla));
        filaTarjetas.add(tarjeta("CONTRATOS VIGENTES", lblContratos));
        filaTarjetas.add(tarjeta("PARTIDOS FINALIZADOS", lblPartidos));

        JPanel filaInferior = new JPanel(new GridLayout(1, 2, 15, 15));
        filaInferior.setOpaque(false);
        filaInferior.add(construirPanelProximoPartido());
        filaInferior.add(construirPanelFinanzas());

        contenido.add(filaTarjetas);
        contenido.add(filaInferior);

        add(contenido, BorderLayout.CENTER);
    }

    private JPanel tarjeta(String titulo, JLabel valor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.DORADO, 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitulo.setForeground(ColoresBlaugrana.GRIS_TEXTO);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);
        return panel;
    }

    private JLabel valorTarjeta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.BOLD, 32));
        label.setForeground(ColoresBlaugrana.GRANATE);
        return label;
    }

    private JPanel construirPanelProximoPartido() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.AZUL_OSCURO, 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titulo = new JLabel("PROXIMO PARTIDO");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);

        lblProximoPartido.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblProximoPartido.setVerticalAlignment(SwingConstants.CENTER);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(lblProximoPartido, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPanelFinanzas() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 8));
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.AZUL_OSCURO, 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titulo = new JLabel("SITUACION FINANCIERA");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(titulo, BorderLayout.NORTH);

        JPanel filas = new JPanel(new GridLayout(3, 2, 5, 6));
        filas.setOpaque(false);
        filas.add(new JLabel("Ingresos:"));
        lblIngresos.setForeground(ColoresBlaugrana.VERDE_ACTIVO);
        filas.add(lblIngresos);
        filas.add(new JLabel("Egresos:"));
        lblEgresos.setForeground(ColoresBlaugrana.ROJO_ALERTA);
        filas.add(lblEgresos);
        filas.add(new JLabel("Balance:"));
        filas.add(lblBalance);

        contenedor.add(filas, BorderLayout.CENTER);
        return contenedor;
    }

    @Override
    public void refrescar() {
        try {
            lblPlantilla.setText(String.valueOf(jugadorService.contarActivos()));
            lblContratos.setText(String.valueOf(contratoService.contarVigentes()));
            lblPartidos.setText(String.valueOf(partidoService.contarFinalizados()));

            Optional<Partido> proximo = partidoService.buscarProximo();
            if (proximo.isPresent()) {
                Partido p = proximo.get();
                lblProximoPartido.setText("<html>" + p.getCompeticion() + "<br><b>FC Barcelona</b> vs <b>" + p.getRival()
                        + "</b><br>" + p.getFecha() + "</html>");
            } else {
                lblProximoPartido.setText("Sin partidos programados");
            }

            BigDecimal ingresos = finanzasService.totalIngresos();
            BigDecimal egresos = finanzasService.totalEgresos();
            BigDecimal balance = ingresos.subtract(egresos);
            lblIngresos.setText(FormatoMoneda.formatear(ingresos));
            lblEgresos.setText(FormatoMoneda.formatear(egresos));
            lblBalance.setText(FormatoMoneda.formatear(balance));
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el dashboard:\n" + e.getMessage());
        }
    }
}
