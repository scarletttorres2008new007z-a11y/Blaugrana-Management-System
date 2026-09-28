package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Pago;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PagoDAO {

    private static final String SELECT_BASE =
            "SELECT p.id_pago, p.id_jugador, j.nombre + ' ' + j.apellido AS nombre_jugador, p.id_contrato, " +
            "       p.periodo, p.salario_base, p.bonificaciones, p.deducciones, p.total, p.fecha_pago, p.estado " +
            "FROM PAGO p JOIN JUGADOR j ON j.id_jugador = p.id_jugador";

    public Optional<Pago> buscarPorId(int idPago) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return buscarPorId(con, idPago);
        }
    }

    public Optional<Pago> buscarPorId(Connection con, int idPago) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE p.id_pago = ?")) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int contarPendientes() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM PAGO WHERE estado = 'PENDIENTE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public java.math.BigDecimal sumarPendientes() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT ISNULL(SUM(total), 0) FROM PAGO WHERE estado = 'PENDIENTE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO;
        }
    }

    public boolean existePagoPorJugadorYPeriodo(int idJugador, String periodo) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM PAGO WHERE id_jugador = ? AND periodo = ?")) {
            ps.setInt(1, idJugador);
            ps.setString(2, periodo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Pago> listar() throws SQLException {
        List<Pago> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY p.periodo DESC, p.id_pago DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Pago> listarPorJugador(int idJugador) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE p.id_jugador = ? ORDER BY p.periodo DESC")) {
            ps.setInt(1, idJugador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void insertar(Pago pago) throws SQLException {
        String sql = "INSERT INTO PAGO (id_jugador, id_contrato, periodo, salario_base, bonificaciones, " +
                "deducciones, total, fecha_pago, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(ps, pago);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    pago.setIdPago(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Pago pago) throws SQLException {
        String sql = "UPDATE PAGO SET id_jugador = ?, id_contrato = ?, periodo = ?, salario_base = ?, " +
                "bonificaciones = ?, deducciones = ?, total = ?, fecha_pago = ?, estado = ? WHERE id_pago = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            establecerParametros(ps, pago);
            ps.setInt(10, pago.getIdPago());
            ps.executeUpdate();
        }
    }

    public void marcarPagado(int idPago, LocalDate fechaPago) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            marcarPagado(con, idPago, fechaPago);
        }
    }

    /**
     * Variante transaccional: usa una conexion ya abierta por el llamador
     * (que controla commit/rollback) en lugar de abrir una propia.
     */
    public void marcarPagado(Connection con, int idPago, LocalDate fechaPago) throws SQLException {
        String sql = "UPDATE PAGO SET estado = 'PAGADO', fecha_pago = ? WHERE id_pago = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaPago));
            ps.setInt(2, idPago);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idPago) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM PAGO WHERE id_pago = ?")) {
            ps.setInt(1, idPago);
            ps.executeUpdate();
        }
    }

    private void establecerParametros(PreparedStatement ps, Pago pago) throws SQLException {
        ps.setInt(1, pago.getIdJugador());
        ps.setInt(2, pago.getIdContrato());
        ps.setString(3, pago.getPeriodo());
        ps.setBigDecimal(4, pago.getSalarioBase());
        ps.setBigDecimal(5, pago.getBonificaciones());
        ps.setBigDecimal(6, pago.getDeducciones());
        ps.setBigDecimal(7, pago.getTotal());
        ps.setDate(8, pago.getFechaPago() != null ? Date.valueOf(pago.getFechaPago()) : null);
        ps.setString(9, pago.getEstado());
    }

    private Pago mapear(ResultSet rs) throws SQLException {
        Pago pago = new Pago();
        pago.setIdPago(rs.getInt("id_pago"));
        pago.setIdJugador(rs.getInt("id_jugador"));
        pago.setNombreJugador(rs.getString("nombre_jugador"));
        pago.setIdContrato(rs.getInt("id_contrato"));
        pago.setPeriodo(rs.getString("periodo"));
        pago.setSalarioBase(rs.getBigDecimal("salario_base"));
        pago.setBonificaciones(rs.getBigDecimal("bonificaciones"));
        pago.setDeducciones(rs.getBigDecimal("deducciones"));
        pago.setTotal(rs.getBigDecimal("total"));
        if (rs.getDate("fecha_pago") != null) {
            pago.setFechaPago(rs.getDate("fecha_pago").toLocalDate());
        }
        pago.setEstado(rs.getString("estado"));
        return pago;
    }
}
