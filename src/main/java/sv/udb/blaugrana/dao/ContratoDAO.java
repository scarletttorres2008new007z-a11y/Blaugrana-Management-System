package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Contrato;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContratoDAO {

    private static final String SELECT_BASE =
            "SELECT c.id_contrato, c.id_jugador, j.nombre + ' ' + j.apellido AS nombre_jugador, " +
            "       c.fecha_inicio, c.fecha_fin, c.salario_base, c.condiciones, c.estado " +
            "FROM CONTRATO c JOIN JUGADOR j ON j.id_jugador = c.id_jugador";

    public List<Contrato> listar() throws SQLException {
        List<Contrato> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY c.id_contrato DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Contrato> listarPorJugador(int idJugador) throws SQLException {
        List<Contrato> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE c.id_jugador = ? ORDER BY c.fecha_inicio DESC")) {
            ps.setInt(1, idJugador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Optional<Contrato> buscarPorId(int idContrato) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE c.id_contrato = ?")) {
            ps.setInt(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existeContratoVigente(int idJugador, Integer idContratoExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CONTRATO WHERE id_jugador = ? AND estado = 'VIGENTE'"
                + (idContratoExcluir != null ? " AND id_contrato <> ?" : "");
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            if (idContratoExcluir != null) {
                ps.setInt(2, idContratoExcluir);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int contarVigentes() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM CONTRATO WHERE estado = 'VIGENTE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public BigDecimal sumarNominaMensualVigente() throws SQLException {
        String sql = "SELECT ISNULL(SUM(salario_base), 0) / 12 FROM CONTRATO WHERE estado = 'VIGENTE'";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    public int contarPorVencerEnDias(int dias) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CONTRATO WHERE estado = 'VIGENTE' " +
                "AND fecha_fin BETWEEN CAST(GETDATE() AS DATE) AND DATEADD(DAY, ?, CAST(GETDATE() AS DATE))";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void insertar(Contrato contrato) throws SQLException {
        String sql = "INSERT INTO CONTRATO (id_jugador, fecha_inicio, fecha_fin, salario_base, condiciones, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, contrato.getIdJugador());
            ps.setDate(2, Date.valueOf(contrato.getFechaInicio()));
            ps.setDate(3, Date.valueOf(contrato.getFechaFin()));
            ps.setBigDecimal(4, contrato.getSalarioBase());
            ps.setString(5, contrato.getCondiciones());
            ps.setString(6, contrato.getEstado());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    contrato.setIdContrato(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Contrato contrato) throws SQLException {
        String sql = "UPDATE CONTRATO SET id_jugador = ?, fecha_inicio = ?, fecha_fin = ?, salario_base = ?, " +
                "condiciones = ?, estado = ? WHERE id_contrato = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, contrato.getIdJugador());
            ps.setDate(2, Date.valueOf(contrato.getFechaInicio()));
            ps.setDate(3, Date.valueOf(contrato.getFechaFin()));
            ps.setBigDecimal(4, contrato.getSalarioBase());
            ps.setString(5, contrato.getCondiciones());
            ps.setString(6, contrato.getEstado());
            ps.setInt(7, contrato.getIdContrato());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idContrato) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM CONTRATO WHERE id_contrato = ?")) {
            ps.setInt(1, idContrato);
            ps.executeUpdate();
        }
    }

    private Contrato mapear(ResultSet rs) throws SQLException {
        Contrato contrato = new Contrato();
        contrato.setIdContrato(rs.getInt("id_contrato"));
        contrato.setIdJugador(rs.getInt("id_jugador"));
        contrato.setNombreJugador(rs.getString("nombre_jugador"));
        contrato.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
        contrato.setFechaFin(rs.getDate("fecha_fin").toLocalDate());
        BigDecimal salario = rs.getBigDecimal("salario_base");
        contrato.setSalarioBase(salario);
        contrato.setCondiciones(rs.getString("condiciones"));
        contrato.setEstado(rs.getString("estado"));
        return contrato;
    }
}
