package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Presupuesto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoDAO {

    private static final String SELECT_BASE =
            "SELECT id_presupuesto, temporada, nombre, fecha_creacion FROM PRESUPUESTO";

    public List<Presupuesto> listar() throws SQLException {
        List<Presupuesto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY temporada DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void insertar(Presupuesto presupuesto) throws SQLException {
        String sql = "INSERT INTO PRESUPUESTO (temporada, nombre) VALUES (?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, presupuesto.getTemporada());
            ps.setString(2, presupuesto.getNombre());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    presupuesto.setIdPresupuesto(claves.getInt(1));
                }
            }
        }
    }

    public void eliminar(int idPresupuesto) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM PRESUPUESTO WHERE id_presupuesto = ?")) {
            ps.setInt(1, idPresupuesto);
            ps.executeUpdate();
        }
    }

    private Presupuesto mapear(ResultSet rs) throws SQLException {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(rs.getInt("id_presupuesto"));
        presupuesto.setTemporada(rs.getString("temporada"));
        presupuesto.setNombre(rs.getString("nombre"));
        presupuesto.setFechaCreacion(rs.getDate("fecha_creacion").toLocalDate());
        return presupuesto;
    }
}
