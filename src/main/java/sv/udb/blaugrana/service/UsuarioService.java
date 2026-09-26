package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.RolDAO;
import sv.udb.blaugrana.dao.UsuarioDAO;
import sv.udb.blaugrana.model.Rol;
import sv.udb.blaugrana.model.Usuario;
import sv.udb.blaugrana.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    public List<Usuario> listar() throws SQLException {
        return usuarioDAO.listar();
    }

    public List<Rol> listarRoles() throws SQLException {
        return rolDAO.listar();
    }

    public void crear(Usuario usuario, String contrasenaPlano) throws SQLException {
        usuario.setContrasenaHash(PasswordUtil.hash(contrasenaPlano));
        usuarioDAO.insertar(usuario);
    }

    public void actualizar(Usuario usuario) throws SQLException {
        usuarioDAO.actualizar(usuario);
    }

    public void cambiarContrasena(int idUsuario, String nuevaContrasena) throws SQLException {
        usuarioDAO.actualizarContrasena(idUsuario, PasswordUtil.hash(nuevaContrasena));
    }

    public void eliminar(int idUsuario) throws SQLException {
        usuarioDAO.eliminar(idUsuario);
    }
}
