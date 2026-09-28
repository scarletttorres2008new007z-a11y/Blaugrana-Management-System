package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Partido;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PartidoDAO {

    private static final String SELECT_BASE =
            "SELECT id_partido, competicion, fecha, rival, condicion, goles_favor, goles_contra, resultado, estado " +
            "FROM PARTIDO";

    public List<Partido> listar() throws SQLException {
        List<Partido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY fecha DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Partido> buscarPorId(int idPartido) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return buscarPorId(con, idPartido);
        }
    }

    public Optional<Partido> buscarPorId(Connection con, int idPartido) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE id_partido = ?")) {
            ps.setInt(1, idPartido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Partido> buscarProximo() throws SQLException {
        String sql = SELECT_BASE + " WHERE estado = 'PROGRAMADO' AND fecha >= CAST(GETDATE() AS DATE) " +
                "ORDER BY fecha ASC";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public int contarFinalizados() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM PARTIDO WHERE estado = 'FINALIZADO'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int contarProgramados() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM PARTIDO WHERE estado = 'PROGRAMADO'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int contarPorResultado(String resultado) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM PARTIDO WHERE estado = 'FINALIZADO' AND resultado = ?")) {
            ps.setString(1, resultado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int sumarGolesFavor() throws SQLException {
        return sumarColumna("goles_favor");
    }

    public int sumarGolesContra() throws SQLException {
        return sumarColumna("goles_contra");
    }

    private int sumarColumna(String columna) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT ISNULL(SUM(" + columna + "), 0) FROM PARTIDO WHERE estado = 'FINALIZADO'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void insertar(Partido partido) throws SQLException {
        String sql = "INSERT INTO PARTIDO (competicion, fecha, rival, condicion, goles_favor, goles_contra, " +
                "resultado, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(ps, partido);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    partido.setIdPartido(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Partido partido) throws SQLException {
        String sql = "UPDATE PARTIDO SET competicion = ?, fecha = ?, rival = ?, condicion = ?, goles_favor = ?, " +
                "goles_contra = ?, resultado = ?, estado = ? WHERE id_partido = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            establecerParametros(ps, partido);
            ps.setInt(9, partido.getIdPartido());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idPartido) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM PARTIDO WHERE id_partido = ?")) {
            ps.setInt(1, idPartido);
            ps.executeUpdate();
        }
    }

    private void establecerParametros(PreparedStatement ps, Partido partido) throws SQLException {
        ps.setString(1, partido.getCompeticion());
        ps.setDate(2, Date.valueOf(partido.getFecha()));
        ps.setString(3, partido.getRival());
        ps.setString(4, partido.getCondicion());
        ps.setInt(5, partido.getGolesFavor());
        ps.setInt(6, partido.getGolesContra());
        ps.setString(7, partido.getResultado());
        ps.setString(8, partido.getEstado());
    }

    private Partido mapear(ResultSet rs) throws SQLException {
        Partido partido = new Partido();
        partido.setIdPartido(rs.getInt("id_partido"));
        partido.setCompeticion(rs.getString("competicion"));
        partido.setFecha(rs.getDate("fecha").toLocalDate());
        partido.setRival(rs.getString("rival"));
        partido.setCondicion(rs.getString("condicion"));
        partido.setGolesFavor(rs.getInt("goles_favor"));
        partido.setGolesContra(rs.getInt("goles_contra"));
        partido.setResultado(rs.getString("resultado"));
        partido.setEstado(rs.getString("estado"));
        return partido;
    }
}
