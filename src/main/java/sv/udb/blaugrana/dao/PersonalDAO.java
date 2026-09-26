package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Personal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonalDAO {

    private static final String SELECT_BASE =
            "SELECT id_personal, nombre, apellido, documento, cargo, area, fecha_nacimiento, " +
            "       nacionalidad, fecha_ingreso, estado FROM PERSONAL";

    public List<Personal> listar() throws SQLException {
        List<Personal> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY apellido, nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int contarActivos() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM PERSONAL WHERE estado = 'ACTIVO'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void insertar(Personal personal) throws SQLException {
        String sql = "INSERT INTO PERSONAL (nombre, apellido, documento, cargo, area, fecha_nacimiento, " +
                "nacionalidad, fecha_ingreso, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(ps, personal);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    personal.setIdPersonal(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Personal personal) throws SQLException {
        String sql = "UPDATE PERSONAL SET nombre = ?, apellido = ?, documento = ?, cargo = ?, area = ?, " +
                "fecha_nacimiento = ?, nacionalidad = ?, fecha_ingreso = ?, estado = ? WHERE id_personal = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            establecerParametros(ps, personal);
            ps.setInt(10, personal.getIdPersonal());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idPersonal) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM PERSONAL WHERE id_personal = ?")) {
            ps.setInt(1, idPersonal);
            ps.executeUpdate();
        }
    }

    private void establecerParametros(PreparedStatement ps, Personal personal) throws SQLException {
        ps.setString(1, personal.getNombre());
        ps.setString(2, personal.getApellido());
        ps.setString(3, personal.getDocumento());
        ps.setString(4, personal.getCargo());
        ps.setString(5, personal.getArea());
        ps.setDate(6, personal.getFechaNacimiento() != null ? Date.valueOf(personal.getFechaNacimiento()) : null);
        ps.setString(7, personal.getNacionalidad());
        ps.setDate(8, personal.getFechaIngreso() != null ? Date.valueOf(personal.getFechaIngreso()) : null);
        ps.setString(9, personal.getEstado());
    }

    private Personal mapear(ResultSet rs) throws SQLException {
        Personal personal = new Personal();
        personal.setIdPersonal(rs.getInt("id_personal"));
        personal.setNombre(rs.getString("nombre"));
        personal.setApellido(rs.getString("apellido"));
        personal.setDocumento(rs.getString("documento"));
        personal.setCargo(rs.getString("cargo"));
        personal.setArea(rs.getString("area"));
        if (rs.getDate("fecha_nacimiento") != null) {
            personal.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        }
        personal.setNacionalidad(rs.getString("nacionalidad"));
        if (rs.getDate("fecha_ingreso") != null) {
            personal.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
        }
        personal.setEstado(rs.getString("estado"));
        return personal;
    }
}
