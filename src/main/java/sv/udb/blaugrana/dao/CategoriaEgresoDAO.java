package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.CategoriaEgreso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaEgresoDAO {

    public List<CategoriaEgreso> listar() throws SQLException {
        List<CategoriaEgreso> lista = new ArrayList<>();
        String sql = "SELECT id_categoria_egreso, nombre FROM CATEGORIA_EGRESO ORDER BY nombre";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new CategoriaEgreso(rs.getInt("id_categoria_egreso"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    /**
     * Devuelve el id de la categoria con ese nombre, creandola si no existe
     * todavia. Usa la conexion (y transaccion) proporcionada por el llamador.
     */
    public int obtenerOCrear(Connection con, String nombre) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_categoria_egreso FROM CATEGORIA_EGRESO WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO CATEGORIA_EGRESO (nombre) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }
}
