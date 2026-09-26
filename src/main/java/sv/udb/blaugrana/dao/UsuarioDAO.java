package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {

    private static final String SELECT_BASE =
            "SELECT u.id_usuario, u.nombre_usuario, u.contrasena_hash, u.id_rol, r.nombre_rol, " +
            "       u.nombre_completo, u.correo, u.estado, u.fecha_creacion " +
            "FROM USUARIO u JOIN ROL r ON r.id_rol = u.id_rol";

    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY u.nombre_usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
        }
        return usuarios;
    }

    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE u.nombre_usuario = ?")) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO USUARIO (nombre_usuario, contrasena_hash, id_rol, nombre_completo, correo, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getContrasenaHash());
            ps.setInt(3, usuario.getIdRol());
            ps.setString(4, usuario.getNombreCompleto());
            ps.setString(5, usuario.getCorreo());
            ps.setString(6, usuario.getEstado());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setIdUsuario(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE USUARIO SET id_rol = ?, nombre_completo = ?, correo = ?, estado = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuario.getIdRol());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getEstado());
            ps.setInt(5, usuario.getIdUsuario());
            ps.executeUpdate();
        }
    }

    public void actualizarContrasena(int idUsuario, String contrasenaHash) throws SQLException {
        String sql = "UPDATE USUARIO SET contrasena_hash = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, contrasenaHash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idUsuario) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM USUARIO WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setContrasenaHash(rs.getString("contrasena_hash"));
        usuario.setIdRol(rs.getInt("id_rol"));
        usuario.setNombreRol(rs.getString("nombre_rol"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setEstado(rs.getString("estado"));
        if (rs.getDate("fecha_creacion") != null) {
            usuario.setFechaCreacion(rs.getDate("fecha_creacion").toLocalDate());
        }
        return usuario;
    }
}
