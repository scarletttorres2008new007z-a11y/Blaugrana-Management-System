package sv.udb.blaugrana.view.reportes;

import sv.udb.blaugrana.service.ReporteService;
import sv.udb.blaugrana.util.ColoresBlaugrana;
import sv.udb.blaugrana.util.Mensajes;
import sv.udb.blaugrana.view.Refrescable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

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

    private final JComboBox<String> cmbTipoReporte = new JComboBox<>(TIPOS_REPORTE);
    private final DefaultTableModel modeloTabla = new DefaultTableModel();
    private final JTable tabla = new JTable(modeloTabla);

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

        tabla.setRowHeight(24);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo, BorderLayout.NORTH);
        norte.add(selector, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
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
                    List<Object[]> filaBalance = new java.util.ArrayList<>();
                    filaBalance.add(reporteService.reporteBalanceGeneral());
                    mostrar(new String[]{"Total ingresos", "Total egresos", "Balance"}, filaBalance);
                }
                case 9 -> mostrar(new String[]{"Temporada", "Categoria", "Presupuestado", "Ejecutado", "Disponible"}, reporteService.reportePresupuestario());
                default -> { }
            }
        } catch (SQLException e) {
            Mensajes.error(this, "No se pudo generar el reporte", e);
        }
    }

    private void mostrar(String[] columnas, List<Object[]> filas) {
        modeloTabla.setDataVector(filas.toArray(new Object[0][]), columnas);
    }

    @Override
    public void refrescar() {
        generarReporte();
    }
}
