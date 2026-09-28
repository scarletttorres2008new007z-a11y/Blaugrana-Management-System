package sv.udb.blaugrana.view;

import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.service.BonificacionService;
import sv.udb.blaugrana.service.ContratoService;
import sv.udb.blaugrana.service.FinanzasService;
import sv.udb.blaugrana.service.JugadorService;
import sv.udb.blaugrana.service.PagoService;
import sv.udb.blaugrana.service.PartidoService;
import sv.udb.blaugrana.service.ReporteService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.FormatoMoneda;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.graficos.GraficoBarras;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DashboardPanel extends JPanel implements Refrescable {

    private final JugadorService jugadorService = new JugadorService();
    private final ContratoService contratoService = new ContratoService();
    private final PartidoService partidoService = new PartidoService();
    private final FinanzasService finanzasService = new FinanzasService();
    private final PagoService pagoService = new PagoService();
    private final BonificacionService bonificacionService = new BonificacionService();
    private final ReporteService reporteService = new ReporteService();

    private static final int DIAS_ALERTA_CONTRATO = 60;

    private final JLabel lblPlantilla = valorTarjeta("0", 30);
    private final JLabel lblContratos = valorTarjeta("0", 30);
    private final JLabel lblPartidos = valorTarjeta("0", 30);

    private final JLabel lblVictorias = valorTarjeta("0", 22);
    private final JLabel lblEmpates = valorTarjeta("0", 22);
    private final JLabel lblDerrotas = valorTarjeta("0", 22);
    private final JLabel lblGolesFavor = valorTarjeta("0", 22);
    private final JLabel lblGolesContra = valorTarjeta("0", 22);

    private final JLabel lblIngresosMes = valorTarjeta("$0.00", 18);
    private final JLabel lblEgresosMes = valorTarjeta("$0.00", 18);
    private final JLabel lblNomina = valorTarjeta("$0.00", 18);
    private final JLabel lblPagosPendientes = valorTarjeta("0", 18);

    private final JLabel lblProximoPartido = new JLabel("Sin partidos programados");
    private final JLabel lblIngresos = new JLabel("$0.00");
    private final JLabel lblEgresos = new JLabel("$0.00");
    private final JLabel lblBalance = new JLabel("$0.00");

    private final JPanel panelAlertas = new JPanel();
    private final GraficoBarras graficoPosiciones = new GraficoBarras();

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(ColoresBlaugrana.GRIS_CLARO);

        JLabel titulo = new JLabel("FC BARCELONA - Panel general del club");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        titulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        add(titulo, BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        contenido.add(construirFilaTarjetasPrincipales());
        contenido.add(Box.createVerticalStrut(15));
        contenido.add(construirFilaDeportiva());
        contenido.add(Box.createVerticalStrut(15));
        contenido.add(construirFilaFinanciera());
        contenido.add(Box.createVerticalStrut(15));
        contenido.add(construirFilaInferior());
        contenido.add(Box.createVerticalStrut(15));
        contenido.add(construirFilaAlertasYGrafico());

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel construirFilaTarjetasPrincipales() {
        JPanel fila = new JPanel(new GridLayout(1, 3, 15, 15));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        fila.add(tarjeta("PLANTILLA", lblPlantilla));
        fila.add(tarjeta("CONTRATOS VIGENTES", lblContratos));
        fila.add(tarjeta("PARTIDOS FINALIZADOS", lblPartidos));
        return fila;
    }

    private JPanel construirFilaDeportiva() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel etiqueta = new JLabel("DESEMPEÑO DEPORTIVO");
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 12));
        etiqueta.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        etiqueta.setBorder(new EmptyBorder(0, 2, 5, 0));
        contenedor.add(etiqueta, BorderLayout.NORTH);

        JPanel fila = new JPanel(new GridLayout(1, 5, 12, 12));
        fila.setOpaque(false);
        fila.add(tarjetaPequena("VICTORIAS", lblVictorias, ColoresBlaugrana.VERDE_ACTIVO));
        fila.add(tarjetaPequena("EMPATES", lblEmpates, ColoresBlaugrana.GRIS_TEXTO));
        fila.add(tarjetaPequena("DERROTAS", lblDerrotas, ColoresBlaugrana.ROJO_ALERTA));
        fila.add(tarjetaPequena("GOLES A FAVOR", lblGolesFavor, ColoresBlaugrana.AZUL_OSCURO));
        fila.add(tarjetaPequena("GOLES EN CONTRA", lblGolesContra, ColoresBlaugrana.GRANATE));
        contenedor.add(fila, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel construirFilaFinanciera() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel etiqueta = new JLabel("SITUACION FINANCIERA DEL MES");
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 12));
        etiqueta.setForeground(ColoresBlaugrana.GRIS_TEXTO);
        etiqueta.setBorder(new EmptyBorder(0, 2, 5, 0));
        contenedor.add(etiqueta, BorderLayout.NORTH);

        JPanel fila = new JPanel(new GridLayout(1, 4, 12, 12));
        fila.setOpaque(false);
        fila.add(tarjetaPequena("INGRESOS DEL MES", lblIngresosMes, ColoresBlaugrana.VERDE_ACTIVO));
        fila.add(tarjetaPequena("EGRESOS DEL MES", lblEgresosMes, ColoresBlaugrana.ROJO_ALERTA));
        fila.add(tarjetaPequena("NOMINA MENSUAL", lblNomina, ColoresBlaugrana.AZUL_OSCURO));
        fila.add(tarjetaPequena("PAGOS PENDIENTES", lblPagosPendientes, ColoresBlaugrana.GRANATE));
        contenedor.add(fila, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel construirFilaInferior() {
        JPanel fila = new JPanel(new GridLayout(1, 2, 15, 15));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        fila.add(construirPanelProximoPartido());
        fila.add(construirPanelFinanzas());
        return fila;
    }

    private JPanel construirFilaAlertasYGrafico() {
        JPanel fila = new JPanel(new GridLayout(1, 2, 15, 15));
        fila.setOpaque(false);
        fila.setPreferredSize(new Dimension(0, 220));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        fila.add(construirPanelAlertas());
        fila.add(construirPanelGrafico());
        return fila;
    }

    private JPanel construirPanelAlertas() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresBlaugrana.BLANCO);
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.DORADO, 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titulo = new JLabel("ALERTAS");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);
        contenedor.add(titulo, BorderLayout.NORTH);

        panelAlertas.setLayout(new BoxLayout(panelAlertas, BoxLayout.Y_AXIS));
        panelAlertas.setOpaque(false);
        JScrollPane scroll = new JScrollPane(panelAlertas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel construirPanelGrafico() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(ColoresBlaugrana.BLANCO);
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColoresBlaugrana.DORADO, 1),
                new EmptyBorder(10, 10, 10, 10)));
        contenedor.add(graficoPosiciones, BorderLayout.CENTER);
        return contenedor;
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

    private JPanel tarjetaPequena(String titulo, JLabel valor, Color colorValor) {
        valor.setForeground(colorValor);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColoresBlaugrana.BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0), 1),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblTitulo.setForeground(ColoresBlaugrana.GRIS_TEXTO);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel valorTarjeta(String texto, int tamano) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.BOLD, tamano));
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

        JLabel titulo = new JLabel("SITUACION FINANCIERA GENERAL");
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

    private JLabel etiquetaAlerta(String texto, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(color);
        etiqueta.setFont(new Font("SansSerif", Font.PLAIN, 13));
        etiqueta.setBorder(new EmptyBorder(3, 0, 3, 0));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    @Override
    public void refrescar() {
        try {
            lblPlantilla.setText(String.valueOf(jugadorService.contarActivos()));
            lblContratos.setText(String.valueOf(contratoService.contarVigentes()));
            lblPartidos.setText(String.valueOf(partidoService.contarFinalizados()));

            lblVictorias.setText(String.valueOf(partidoService.contarGanados()));
            lblEmpates.setText(String.valueOf(partidoService.contarEmpatados()));
            lblDerrotas.setText(String.valueOf(partidoService.contarPerdidos()));
            lblGolesFavor.setText(String.valueOf(partidoService.sumarGolesFavor()));
            lblGolesContra.setText(String.valueOf(partidoService.sumarGolesContra()));

            lblIngresosMes.setText(FormatoMoneda.formatear(finanzasService.ingresosMesActual()));
            lblEgresosMes.setText(FormatoMoneda.formatear(finanzasService.egresosMesActual()));
            lblNomina.setText(FormatoMoneda.formatear(contratoService.nominaMensual()));

            int pagosPendientes = pagoService.contarPendientes();
            lblPagosPendientes.setText(String.valueOf(pagosPendientes));

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

            int contratosPorVencer = contratoService.contarPorVencer(DIAS_ALERTA_CONTRATO);
            int partidosProgramados = partidoService.contarProgramados();
            BigDecimal montoPendiente = pagoService.sumarPendientes();
            actualizarAlertas(contratosPorVencer, pagosPendientes, montoPendiente, partidosProgramados);

            List<Object[]> plantillaPorPosicion = reporteService.reportePlantillaPorPosicion();
            List<String> etiquetas = new ArrayList<>();
            List<Double> valores = new ArrayList<>();
            for (Object[] fila : plantillaPorPosicion) {
                etiquetas.add(String.valueOf(fila[0]));
                valores.add(((Number) fila[1]).doubleValue());
            }
            graficoPosiciones.setDatos("Plantilla por posición", etiquetas, valores);
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo cargar el dashboard", e);
        }
    }

    private void actualizarAlertas(int contratosPorVencer, int pagosPendientes, BigDecimal montoPendiente,
                                    int partidosProgramados) {
        panelAlertas.removeAll();

        boolean hayAlertas = false;
        if (contratosPorVencer > 0) {
            panelAlertas.add(etiquetaAlerta("⚠ " + contratosPorVencer + " contrato(s) vencen en los próximos "
                    + DIAS_ALERTA_CONTRATO + " días", ColoresBlaugrana.ROJO_ALERTA));
            hayAlertas = true;
        }
        if (pagosPendientes > 0) {
            panelAlertas.add(etiquetaAlerta("⚠ " + pagosPendientes + " pago(s) pendiente(s) por "
                    + FormatoMoneda.formatear(montoPendiente), new Color(0xB8, 0x86, 0x0B)));
            hayAlertas = true;
        }
        if (partidosProgramados > 0) {
            panelAlertas.add(etiquetaAlerta("📅 " + partidosProgramados + " partido(s) programado(s)",
                    ColoresBlaugrana.AZUL_OSCURO));
            hayAlertas = true;
        }
        if (!hayAlertas) {
            panelAlertas.add(etiquetaAlerta("Sin alertas activas.", ColoresBlaugrana.GRIS_TEXTO));
        }

        panelAlertas.revalidate();
        panelAlertas.repaint();
    }
}
