package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Bonificacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BonificacionDAO {

    private static final String SELECT_BASE =
            "SELECT b.id_bonificacion, b.id_jugador, j.nombre + ' ' + j.apellido AS nombre_jugador, " +
            "       b.id_partido, b.concepto, b.valor, b.fecha_generacion " +
            "FROM BONIFICACION b JOIN JUGADOR j ON j.id_jugador = b.id_jugador";

    public List<Bonificacion> listar() throws SQLException {
        List<Bonificacion> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY b.fecha_generacion DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Bonificacion> listarPorJugador(int idJugador) throws SQLException {
        List<Bonificacion> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE b.id_jugador = ? ORDER BY b.fecha_generacion DESC")) {
            ps.setInt(1, idJugador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public boolean existeParaPartido(int idPartido) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return existeParaPartido(con, idPartido);
        }
    }

    public boolean existeParaPartido(Connection con, int idPartido) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM BONIFICACION WHERE id_partido = ?")) {
            ps.setInt(1, idPartido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public BigDecimal sumarPorJugador(int idJugador) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT ISNULL(SUM(valor), 0) FROM BONIFICACION WHERE id_jugador = ?")) {
            ps.setInt(1, idJugador);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public BigDecimal sumarPorJugadorYPeriodo(int idJugador, String periodoAnioMes) throws SQLException {
        String sql = "SELECT ISNULL(SUM(valor), 0) FROM BONIFICACION " +
                "WHERE id_jugador = ? AND LEFT(CONVERT(varchar(10), fecha_generacion, 120), 7) = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            ps.setString(2, periodoAnioMes);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public void insertar(Bonificacion bonificacion) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            insertar(con, bonificacion);
        }
    }

    /**
     * Variante transaccional: usa una conexion ya abierta por el llamador
     * (que controla commit/rollback) en lugar de abrir una propia.
     */
    public void insertar(Connection con, Bonificacion bonificacion) throws SQLException {
        String sql = "INSERT INTO BONIFICACION (id_jugador, id_partido, concepto, valor, fecha_generacion) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bonificacion.getIdJugador());
            if (bonificacion.getIdPartido() != null) {
                ps.setInt(2, bonificacion.getIdPartido());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, bonificacion.getConcepto());
            ps.setBigDecimal(4, bonificacion.getValor());
            ps.setDate(5, Date.valueOf(bonificacion.getFechaGeneracion()));
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    bonificacion.setIdBonificacion(claves.getInt(1));
                }
            }
        }
    }

    public void eliminar(int idBonificacion) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM BONIFICACION WHERE id_bonificacion = ?")) {
            ps.setInt(1, idBonificacion);
            ps.executeUpdate();
        }
    }

    private Bonificacion mapear(ResultSet rs) throws SQLException {
        Bonificacion bonificacion = new Bonificacion();
        bonificacion.setIdBonificacion(rs.getInt("id_bonificacion"));
        bonificacion.setIdJugador(rs.getInt("id_jugador"));
        bonificacion.setNombreJugador(rs.getString("nombre_jugador"));
        int idPartido = rs.getInt("id_partido");
        bonificacion.setIdPartido(rs.wasNull() ? null : idPartido);
        bonificacion.setConcepto(rs.getString("concepto"));
        bonificacion.setValor(rs.getBigDecimal("valor"));
        bonificacion.setFechaGeneracion(rs.getDate("fecha_generacion").toLocalDate());
        return bonificacion;
    }
}
