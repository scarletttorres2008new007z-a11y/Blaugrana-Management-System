package sv.udb.blaugrana.view.reportes;

import sv.udb.blaugrana.service.ReporteService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.CsvExporter;
import sv.udb.blaugrana.util.Impresion;
import sv.udb.blaugrana.util.Medidas;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.util.Tipografia;
import sv.udb.blaugrana.util.graficos.GraficoBarras;
import sv.udb.blaugrana.view.Refrescable;
import sv.udb.blaugrana.view.componentes.EncabezadoSeccion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas de club, deportivo y finanzas, organizadas primero por
 * categoria y luego por tipo de reporte dentro de ella.
 */
public class ReportesPanel extends JPanel implements Refrescable {

    private final ReporteService reporteService = new ReporteService();

    /** Etiqueta de un reporte junto con su indice original (usado en el switch de generarReporte). */
    private record OpcionReporte(String etiqueta, int indice) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private static final Map<String, List<OpcionReporte>> REPORTES_POR_CATEGORIA = new LinkedHashMap<>();

    static {
        REPORTES_POR_CATEGORIA.put("Club", List.of(
                new OpcionReporte("Plantilla por posición", 0),
                new OpcionReporte("Contratos vigentes", 1)));
        REPORTES_POR_CATEGORIA.put("Deportivo", List.of(
                new OpcionReporte("Resultados de partidos", 2),
                new OpcionReporte("Rendimiento por jugador", 3),
                new OpcionReporte("Bonificaciones por jugador", 4)));
        REPORTES_POR_CATEGORIA.put("Financiero", List.of(
                new OpcionReporte("Pagos por periodo", 5),
                new OpcionReporte("Ingresos por categoría", 6),
                new OpcionReporte("Egresos por categoría", 7),
                new OpcionReporte("Balance general", 8),
                new OpcionReporte("Presupuestario", 9)));
    }

    /** Indice de reporte -> {columna de etiqueta, columna de valor} para graficarlo. */
    private static final Map<Integer, int[]> REPORTES_GRAFICABLES = Map.of(
            0, new int[]{0, 1},
            4, new int[]{1, 2},
            6, new int[]{0, 1},
            7, new int[]{0, 1}
    );

    private final JComboBox<String> cmbCategoria = new JComboBox<>(REPORTES_POR_CATEGORIA.keySet().toArray(new String[0]));
    private final JComboBox<OpcionReporte> cmbTipoReporte = new JComboBox<>();
    private final DefaultTableModel modeloTabla = new DefaultTableModel();
    private final JTable tabla = new JTable(modeloTabla);
    private final GraficoBarras grafico = new GraficoBarras();
    private final JCheckBox chkVerGrafico = new JCheckBox("Ver como gráfico");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);

    public ReportesPanel() {
        setLayout(new BorderLayout());
        setBackground(ColoresBlaugrana.GRIS_CLARO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(construirEncabezado(), BorderLayout.NORTH);

        tabla.setRowHeight(24);

        JPanel contenedorContenido = new JPanel(new BorderLayout());
        contenedorContenido.setBackground(ColoresBlaugrana.BLANCO);
        contenedorContenido.setBorder(BorderFactory.createLineBorder(ColoresBlaugrana.GRIS_MEDIO, 1));
        panelContenido.setOpaque(false);
        panelContenido.add(new JScrollPane(tabla), "tabla");
        panelContenido.add(new JScrollPane(grafico), "grafico");
        contenedorContenido.add(panelContenido, BorderLayout.CENTER);

        add(contenedorContenido, BorderLayout.CENTER);

        cmbCategoria.addActionListener(e -> actualizarOpcionesReporte());
        actualizarOpcionesReporte();
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(new EncabezadoSeccion("Reportes", "Consultas de club, deportivo y finanzas"), BorderLayout.NORTH);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        selector.setOpaque(false);
        selector.setBorder(new EmptyBorder(0, 0, Medidas.PADDING_SECCION, 0));

        JLabel lblCategoria = new JLabel("Categoria:");
        lblCategoria.setFont(Tipografia.CUERPO);
        JLabel lblTipo = new JLabel("Reporte:");
        lblTipo.setFont(Tipografia.CUERPO);

        JButton btnGenerar = new JButton("Generar reporte");
        btnGenerar.addActionListener(e -> generarReporte());

        chkVerGrafico.setOpaque(false);
        chkVerGrafico.setEnabled(false);
        chkVerGrafico.addActionListener(e -> actualizarVista());

        JButton btnExportar = new JButton("Exportar CSV");
        btnExportar.addActionListener(e -> CsvExporter.exportarTabla(this,
                cmbTipoReporte.getSelectedItem() + ".csv", tabla));

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(e -> Impresion.imprimirTabla(this, tabla,
                String.valueOf(cmbTipoReporte.getSelectedItem())));

        selector.add(lblCategoria);
        selector.add(cmbCategoria);
        selector.add(lblTipo);
        selector.add(cmbTipoReporte);
        selector.add(btnGenerar);
        selector.add(chkVerGrafico);
        selector.add(btnExportar);
        selector.add(btnImprimir);

        panel.add(selector, BorderLayout.SOUTH);
        return panel;
    }

    private void actualizarOpcionesReporte() {
        String categoria = (String) cmbCategoria.getSelectedItem();
        cmbTipoReporte.removeAllItems();
        if (categoria == null) {
            return;
        }
        for (OpcionReporte opcion : REPORTES_POR_CATEGORIA.get(categoria)) {
            cmbTipoReporte.addItem(opcion);
        }
    }

    private void generarReporte() {
        OpcionReporte opcion = (OpcionReporte) cmbTipoReporte.getSelectedItem();
        if (opcion == null) {
            return;
        }
        int indice = opcion.indice();
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
