package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Jugador;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JugadorDAO {

    private static final String SELECT_BASE =
            "SELECT id_jugador, nombre, apellido, documento, fecha_nacimiento, nacionalidad, " +
            "       posicion, numero_camiseta, fecha_ingreso, estado FROM JUGADOR";

    public List<Jugador> listar() throws SQLException {
        List<Jugador> jugadores = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY numero_camiseta");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                jugadores.add(mapear(rs));
            }
        }
        return jugadores;
    }

    public List<Jugador> listarActivos() throws SQLException {
        List<Jugador> jugadores = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE estado = 'ACTIVO' ORDER BY numero_camiseta");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                jugadores.add(mapear(rs));
            }
        }
        return jugadores;
    }

    public Optional<Jugador> buscarPorId(int idJugador) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE id_jugador = ?")) {
            ps.setInt(1, idJugador);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existeNumeroCamiseta(int numeroCamiseta, Integer idJugadorExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM JUGADOR WHERE numero_camiseta = ?"
                + (idJugadorExcluir != null ? " AND id_jugador <> ?" : "");
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numeroCamiseta);
            if (idJugadorExcluir != null) {
                ps.setInt(2, idJugadorExcluir);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int contarActivos() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM JUGADOR WHERE estado = 'ACTIVO'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void insertar(Jugador jugador) throws SQLException {
        String sql = "INSERT INTO JUGADOR (nombre, apellido, documento, fecha_nacimiento, nacionalidad, " +
                "posicion, numero_camiseta, fecha_ingreso, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(ps, jugador);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    jugador.setIdJugador(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Jugador jugador) throws SQLException {
        String sql = "UPDATE JUGADOR SET nombre = ?, apellido = ?, documento = ?, fecha_nacimiento = ?, " +
                "nacionalidad = ?, posicion = ?, numero_camiseta = ?, fecha_ingreso = ?, estado = ? " +
                "WHERE id_jugador = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            establecerParametros(ps, jugador);
            ps.setInt(10, jugador.getIdJugador());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idJugador) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM JUGADOR WHERE id_jugador = ?")) {
            ps.setInt(1, idJugador);
            ps.executeUpdate();
        }
    }

    private void establecerParametros(PreparedStatement ps, Jugador jugador) throws SQLException {
        ps.setString(1, jugador.getNombre());
        ps.setString(2, jugador.getApellido());
        ps.setString(3, jugador.getDocumento());
        ps.setDate(4, jugador.getFechaNacimiento() != null ? Date.valueOf(jugador.getFechaNacimiento()) : null);
        ps.setString(5, jugador.getNacionalidad());
        ps.setString(6, jugador.getPosicion());
        ps.setInt(7, jugador.getNumeroCamiseta());
        ps.setDate(8, jugador.getFechaIngreso() != null ? Date.valueOf(jugador.getFechaIngreso()) : null);
        ps.setString(9, jugador.getEstado());
    }

    private Jugador mapear(ResultSet rs) throws SQLException {
        Jugador jugador = new Jugador();
        jugador.setIdJugador(rs.getInt("id_jugador"));
        jugador.setNombre(rs.getString("nombre"));
        jugador.setApellido(rs.getString("apellido"));
        jugador.setDocumento(rs.getString("documento"));
        if (rs.getDate("fecha_nacimiento") != null) {
            jugador.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        }
        jugador.setNacionalidad(rs.getString("nacionalidad"));
        jugador.setPosicion(rs.getString("posicion"));
        jugador.setNumeroCamiseta(rs.getInt("numero_camiseta"));
        if (rs.getDate("fecha_ingreso") != null) {
            jugador.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
        }
        jugador.setEstado(rs.getString("estado"));
        return jugador;
    }
}
