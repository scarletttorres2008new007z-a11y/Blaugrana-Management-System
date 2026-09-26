package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.CategoriaEgreso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
