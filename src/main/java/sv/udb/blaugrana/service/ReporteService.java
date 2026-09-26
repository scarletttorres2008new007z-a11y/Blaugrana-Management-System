package sv.udb.blaugrana.service;

import sv.udb.blaugrana.config.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas de solo lectura que alimentan el modulo de reportes.
 * Cada metodo corresponde a una de las consultas documentadas en
 * database/05_consultas_reportes.sql.
 */
public class ReporteService {

    public List<Object[]> reportePlantillaPorPosicion() throws SQLException {
        String sql = "SELECT posicion, COUNT(*) FROM JUGADOR WHERE estado = 'ACTIVO' GROUP BY posicion ORDER BY posicion";
        return ejecutar(sql, 2);
    }

    public List<Object[]> reporteContratosVigentes() throws SQLException {
        String sql = "SELECT j.numero_camiseta, j.nombre + ' ' + j.apellido, c.fecha_inicio, c.fecha_fin, c.salario_base " +
                "FROM CONTRATO c JOIN JUGADOR j ON j.id_jugador = c.id_jugador " +
                "WHERE c.estado = 'VIGENTE' ORDER BY j.numero_camiseta";
        return ejecutar(sql, 5);
    }

    public List<Object[]> reporteResultadosPartidos() throws SQLException {
        String sql = "SELECT competicion, fecha, rival, condicion, goles_favor, goles_contra, resultado " +
                "FROM PARTIDO WHERE estado = 'FINALIZADO' ORDER BY fecha DESC";
        return ejecutar(sql, 7);
    }

    public List<Object[]> reporteRendimientoJugadores() throws SQLException {
        String sql = "SELECT j.numero_camiseta, j.nombre + ' ' + j.apellido, " +
                "SUM(pp.minutos_jugados), SUM(pp.goles), SUM(pp.asistencias) " +
                "FROM PARTICIPACION_PARTIDO pp JOIN JUGADOR j ON j.id_jugador = pp.id_jugador " +
                "GROUP BY j.numero_camiseta, j.nombre, j.apellido ORDER BY SUM(pp.goles) DESC";
        return ejecutar(sql, 5);
    }

    public List<Object[]> reporteBonificacionesPorJugador() throws SQLException {
        String sql = "SELECT j.numero_camiseta, j.nombre + ' ' + j.apellido, SUM(b.valor) " +
                "FROM BONIFICACION b JOIN JUGADOR j ON j.id_jugador = b.id_jugador " +
                "GROUP BY j.numero_camiseta, j.nombre, j.apellido ORDER BY SUM(b.valor) DESC";
        return ejecutar(sql, 3);
    }

    public List<Object[]> reportePagosPorPeriodo() throws SQLException {
        String sql = "SELECT p.periodo, j.nombre + ' ' + j.apellido, p.salario_base, p.bonificaciones, " +
                "p.deducciones, p.total, p.estado FROM PAGO p JOIN JUGADOR j ON j.id_jugador = p.id_jugador " +
                "ORDER BY p.periodo DESC, p.total DESC";
        return ejecutar(sql, 7);
    }

    public List<Object[]> reporteIngresosPorCategoria() throws SQLException {
        String sql = "SELECT ci.nombre, SUM(i.monto) FROM INGRESO i " +
                "JOIN CATEGORIA_INGRESO ci ON ci.id_categoria_ingreso = i.id_categoria_ingreso " +
                "GROUP BY ci.nombre ORDER BY SUM(i.monto) DESC";
        return ejecutar(sql, 2);
    }

    public List<Object[]> reporteEgresosPorCategoria() throws SQLException {
        String sql = "SELECT ce.nombre, SUM(e.monto) FROM EGRESO e " +
                "JOIN CATEGORIA_EGRESO ce ON ce.id_categoria_egreso = e.id_categoria_egreso " +
                "GROUP BY ce.nombre ORDER BY SUM(e.monto) DESC";
        return ejecutar(sql, 2);
    }

    public Object[] reporteBalanceGeneral() throws SQLException {
        String sql = "SELECT (SELECT ISNULL(SUM(monto), 0) FROM INGRESO), " +
                "(SELECT ISNULL(SUM(monto), 0) FROM EGRESO), " +
                "(SELECT ISNULL(SUM(monto), 0) FROM INGRESO) - (SELECT ISNULL(SUM(monto), 0) FROM EGRESO)";
        List<Object[]> filas = ejecutar(sql, 3);
        return filas.isEmpty() ? new Object[]{0, 0, 0} : filas.get(0);
    }

    public List<Object[]> reportePresupuestario() throws SQLException {
        String sql = "SELECT pr.temporada, dp.categoria, dp.monto_presupuestado, dp.monto_ejecutado, " +
                "(dp.monto_presupuestado - dp.monto_ejecutado) " +
                "FROM DETALLE_PRESUPUESTO dp JOIN PRESUPUESTO pr ON pr.id_presupuesto = dp.id_presupuesto " +
                "ORDER BY pr.temporada, dp.categoria";
        return ejecutar(sql, 5);
    }

    private List<Object[]> ejecutar(String sql, int columnas) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] fila = new Object[columnas];
                for (int i = 0; i < columnas; i++) {
                    fila[i] = rs.getObject(i + 1);
                }
                filas.add(fila);
            }
        }
        return filas;
    }
}
