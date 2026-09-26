package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.CategoriaIngreso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaIngresoDAO {

    public List<CategoriaIngreso> listar() throws SQLException {
        List<CategoriaIngreso> lista = new ArrayList<>();
        String sql = "SELECT id_categoria_ingreso, nombre FROM CATEGORIA_INGRESO ORDER BY nombre";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new CategoriaIngreso(rs.getInt("id_categoria_ingreso"), rs.getString("nombre")));
            }
        }
        return lista;
    }
}
