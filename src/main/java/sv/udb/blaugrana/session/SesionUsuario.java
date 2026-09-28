package sv.udb.blaugrana.session;

import sv.udb.blaugrana.model.Usuario;

/**
 * Mantiene en memoria el usuario que ha iniciado sesion durante la
 * ejecucion de la aplicacion de escritorio.
 */
public final class SesionUsuario {

    private static Usuario usuarioActivo;

    private SesionUsuario() {
    }

    public static void iniciar(Usuario usuario) {
        usuarioActivo = usuario;
    }

    public static Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    public static boolean esAdministrador() {
        return usuarioActivo != null && "Administrador".equalsIgnoreCase(usuarioActivo.getNombreRol());
    }

    public static boolean puedeAcceder(String modulo) {
        return usuarioActivo != null && Permisos.puedeAcceder(usuarioActivo.getNombreRol(), modulo);
    }

    public static boolean esSoloLectura() {
        return usuarioActivo != null && Permisos.esSoloLectura(usuarioActivo.getNombreRol());
    }

    public static void cerrar() {
        usuarioActivo = null;
    }
}
