package sv.udb.blaugrana.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto unico de acceso a la conexion JDBC contra Microsoft SQL Server.
 * Los datos de conexion se leen de src/main/resources/config.properties.
 */
public final class ConexionBD {

    private static final Properties PROPIEDADES = new Properties();

    static {
        try (InputStream entrada = ConexionBD.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (entrada != null) {
                PROPIEDADES.load(entrada);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer config.properties", e);
        }
    }

    private ConexionBD() {
    }

    public static Connection getConexion() throws SQLException {
        String host = PROPIEDADES.getProperty("db.host", "localhost");
        String puerto = PROPIEDADES.getProperty("db.port", "1433");
        String nombreBd = PROPIEDADES.getProperty("db.nombre", "BlaugranaDB");
        String usuario = PROPIEDADES.getProperty("db.usuario", "sa");
        String contrasena = PROPIEDADES.getProperty("db.contrasena", "");
        String encrypt = PROPIEDADES.getProperty("db.encrypt", "false");

        String url = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=true",
                host, puerto, nombreBd, encrypt);

        return DriverManager.getConnection(url, usuario, contrasena);
    }

    public static void cerrar(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignorada) {
                // La conexion ya estaba cerrada o invalida; no hay nada mas que hacer.
            }
        }
    }
}
