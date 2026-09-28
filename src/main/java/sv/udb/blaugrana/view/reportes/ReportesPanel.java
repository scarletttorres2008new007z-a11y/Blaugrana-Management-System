package sv.udb.blaugrana.view.reportes;

import sv.udb.blaugrana.service.ReporteService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.CsvExporter;
import sv.udb.blaugrana.util.Impresion;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.graficos.GraficoBarras;
import sv.udb.blaugrana.view.Refrescable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportesPanel extends JPanel implements Refrescable {

    private final ReporteService reporteService = new ReporteService();

    private static final String[] TIPOS_REPORTE = {
            "Reporte de plantilla por posicion",
            "Reporte de contratos vigentes",
            "Reporte deportivo (resultados)",
            "Reporte de rendimiento por jugador",
            "Reporte de bonificaciones por jugador",
            "Reporte de pagos por periodo",
            "Reporte de ingresos por categoria",
            "Reporte de egresos por categoria",
            "Reporte de balance general",
            "Reporte presupuestario"
    };

    /** Indice de reporte -> {columna de etiqueta, columna de valor} para graficarlo. */
    private static final Map<Integer, int[]> REPORTES_GRAFICABLES = Map.of(
            0, new int[]{0, 1},
            4, new int[]{1, 2},
            6, new int[]{0, 1},
            7, new int[]{0, 1}
    );

    private final JComboBox<String> cmbTipoReporte = new JComboBox<>(TIPOS_REPORTE);
    private final DefaultTableModel modeloTabla = new DefaultTableModel();
    private final JTable tabla = new JTable(modeloTabla);
    private final GraficoBarras grafico = new GraficoBarras();
    private final JCheckBox chkVerGrafico = new JCheckBox("Ver como gráfico");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);

    public ReportesPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("REPORTES - Consultas de club, deportivo y finanzas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(ColoresBlaugrana.AZUL_OSCURO);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selector.add(new JLabel("Tipo de reporte:"));
        selector.add(cmbTipoReporte);
        JButton btnGenerar = new JButton("Generar reporte");
        btnGenerar.addActionListener(e -> generarReporte());
        selector.add(btnGenerar);
        chkVerGrafico.setEnabled(false);
        chkVerGrafico.addActionListener(e -> actualizarVista());
        selector.add(chkVerGrafico);

        JButton btnExportar = new JButton("Exportar CSV");
        btnExportar.addActionListener(e -> CsvExporter.exportarTabla(this,
                (String) cmbTipoReporte.getSelectedItem() + ".csv", tabla));
        selector.add(btnExportar);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(e -> Impresion.imprimirTabla(this, tabla,
                String.valueOf(cmbTipoReporte.getSelectedItem())));
        selector.add(btnImprimir);

        tabla.setRowHeight(24);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo, BorderLayout.NORTH);
        norte.add(selector, BorderLayout.SOUTH);

        panelContenido.add(new JScrollPane(tabla), "tabla");
        panelContenido.add(new JScrollPane(grafico), "grafico");

        add(norte, BorderLayout.NORTH);
        add(panelContenido, BorderLayout.CENTER);
    }

    private void generarReporte() {
        int indice = cmbTipoReporte.getSelectedIndex();
        try {
            switch (indice) {
                case 0 -> mostrar(new String[]{"Posicion", "Total jugadores"}, reporteService.reportePlantillaPorPosicion());
                case 1 -> mostrar(new String[]{"Nº", "Jugador", "Inicio", "Fin", "Salario base"}, reporteService.reporteContratosVigentes());
                case 2 -> mostrar(new String[]{"Competicion", "Fecha", "Rival", "Condicion", "GF", "GC", "Resultado"}, reporteService.reporteResultadosPartidos());
                case 3 -> mostrar(new String[]{"Nº", "Jugador", "Minutos", "Goles", "Asistencias"}, reporteService.reporteRendimientoJugadores());
                case 4 -> mostrar(new String[]{"Nº", "Jugador", "Total bonificaciones"}, reporteService.reporteBonificacionesPorJugador());
                case 5 -> mostrar(new String[]{"Periodo", "Jugador", "Salario", "Bonif.", "Deduc.", "Total", "Estado"}, reporteService.reportePagosPorPeriodo());
                case 6 -> mostrar(new String[]{"Categoria", "Total"}, reporteService.reporteIngresosPorCategoria());
                case 7 -> mostrar(new String[]{"Categoria", "Total"}, reporteService.reporteEgresosPorCategoria());
                case 8 -> {
                    List<Object[]> filaBalance = new ArrayList<>();
                    filaBalance.add(reporteService.reporteBalanceGeneral());
                    mostrar(new String[]{"Total ingresos", "Total egresos", "Balance"}, filaBalance);
                }
                case 9 -> mostrar(new String[]{"Temporada", "Categoria", "Presupuestado", "Ejecutado", "Disponible"}, reporteService.reportePresupuestario());
                default -> { }
            }

            int[] columnasGrafico = REPORTES_GRAFICABLES.get(indice);
            chkVerGrafico.setEnabled(columnasGrafico != null);
            if (columnasGrafico == null) {
                chkVerGrafico.setSelected(false);
            }
            actualizarGrafico(columnasGrafico);
            actualizarVista();
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo generar el reporte", e);
        }
    }

    private void mostrar(String[] columnas, List<Object[]> filas) {
        modeloTabla.setDataVector(filas.toArray(new Object[0][]), columnas);
    }

    private void actualizarGrafico(int[] columnasGrafico) {
        if (columnasGrafico == null) {
            return;
        }
        List<String> etiquetas = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        for (int f = 0; f < modeloTabla.getRowCount(); f++) {
            Object etiqueta = modeloTabla.getValueAt(f, columnasGrafico[0]);
            Object valor = modeloTabla.getValueAt(f, columnasGrafico[1]);
            etiquetas.add(String.valueOf(etiqueta));
            valores.add(valor instanceof Number numero ? numero.doubleValue() : 0.0);
        }
        grafico.setDatos(String.valueOf(cmbTipoReporte.getSelectedItem()), etiquetas, valores);
    }

    private void actualizarVista() {
        cardLayout.show(panelContenido, chkVerGrafico.isSelected() ? "grafico" : "tabla");
    }

    @Override
    public void refrescar() {
        generarReporte();
    }
}
