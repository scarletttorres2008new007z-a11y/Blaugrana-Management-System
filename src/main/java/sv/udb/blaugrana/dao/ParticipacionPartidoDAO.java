package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.ParticipacionPartido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ParticipacionPartidoDAO {

    private static final String SELECT_BASE =
            "SELECT pp.id_participacion, pp.id_partido, pp.id_jugador, j.nombre + ' ' + j.apellido AS nombre_jugador, " +
            "       pp.minutos_jugados, pp.goles, pp.asistencias, pp.tarjetas_amarillas, pp.tarjetas_rojas, pp.titular " +
            "FROM PARTICIPACION_PARTIDO pp JOIN JUGADOR j ON j.id_jugador = pp.id_jugador";

    public List<ParticipacionPartido> listarPorPartido(int idPartido) throws SQLException {
        List<ParticipacionPartido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE pp.id_partido = ? ORDER BY j.numero_camiseta")) {
            ps.setInt(1, idPartido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public int sumarGolesPorJugador(int idJugador) throws SQLException {
        return sumarColumnaPorJugador("goles", idJugador);
    }

    public int sumarAsistenciasPorJugador(int idJugador) throws SQLException {
        return sumarColumnaPorJugador("asistencias", idJugador);
    }

    private int sumarColumnaPorJugador(String columna, int idJugador) throws SQLException {
        String sql = "SELECT ISNULL(SUM(" + columna + "), 0) FROM PARTICIPACION_PARTIDO WHERE id_jugador = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idJugador);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void insertar(ParticipacionPartido participacion) throws SQLException {
        String sql = "INSERT INTO PARTICIPACION_PARTIDO (id_partido, id_jugador, minutos_jugados, goles, " +
                "asistencias, tarjetas_amarillas, tarjetas_rojas, titular) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(ps, participacion);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    participacion.setIdParticipacion(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(ParticipacionPartido participacion) throws SQLException {
        String sql = "UPDATE PARTICIPACION_PARTIDO SET id_partido = ?, id_jugador = ?, minutos_jugados = ?, " +
                "goles = ?, asistencias = ?, tarjetas_amarillas = ?, tarjetas_rojas = ?, titular = ? " +
                "WHERE id_participacion = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            establecerParametros(ps, participacion);
            ps.setInt(9, participacion.getIdParticipacion());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idParticipacion) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM PARTICIPACION_PARTIDO WHERE id_participacion = ?")) {
            ps.setInt(1, idParticipacion);
            ps.executeUpdate();
        }
    }

    private void establecerParametros(PreparedStatement ps, ParticipacionPartido participacion) throws SQLException {
        ps.setInt(1, participacion.getIdPartido());
        ps.setInt(2, participacion.getIdJugador());
        ps.setInt(3, participacion.getMinutosJugados());
        ps.setInt(4, participacion.getGoles());
        ps.setInt(5, participacion.getAsistencias());
        ps.setInt(6, participacion.getTarjetasAmarillas());
        ps.setInt(7, participacion.getTarjetasRojas());
        ps.setBoolean(8, participacion.isTitular());
    }

    private ParticipacionPartido mapear(ResultSet rs) throws SQLException {
        ParticipacionPartido participacion = new ParticipacionPartido();
        participacion.setIdParticipacion(rs.getInt("id_participacion"));
        participacion.setIdPartido(rs.getInt("id_partido"));
        participacion.setIdJugador(rs.getInt("id_jugador"));
        participacion.setNombreJugador(rs.getString("nombre_jugador"));
        participacion.setMinutosJugados(rs.getInt("minutos_jugados"));
        participacion.setGoles(rs.getInt("goles"));
        participacion.setAsistencias(rs.getInt("asistencias"));
        participacion.setTarjetasAmarillas(rs.getInt("tarjetas_amarillas"));
        participacion.setTarjetasRojas(rs.getInt("tarjetas_rojas"));
        participacion.setTitular(rs.getBoolean("titular"));
        return participacion;
    }
}
