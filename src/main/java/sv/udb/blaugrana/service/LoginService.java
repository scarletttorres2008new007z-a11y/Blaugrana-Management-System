package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.UsuarioDAO;
import sv.udb.blaugrana.model.Usuario;
import sv.udb.blaugrana.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class LoginService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Valida credenciales contra la base de datos.
     *
     * @return el usuario autenticado, o vacio si las credenciales son invalidas.
     */
    public Optional<Usuario> autenticar(String nombreUsuario, String contrasena) throws SQLException {
        Optional<Usuario> usuario = usuarioDAO.buscarPorNombreUsuario(nombreUsuario);
        if (usuario.isEmpty()) {
            return Optional.empty();
        }
        Usuario encontrado = usuario.get();
        if (!"ACTIVO".equalsIgnoreCase(encontrado.getEstado())) {
            return Optional.empty();
        }
        if (!PasswordUtil.coincide(contrasena, encontrado.getContrasenaHash())) {
            return Optional.empty();
        }
        return Optional.of(encontrado);
    }
}
