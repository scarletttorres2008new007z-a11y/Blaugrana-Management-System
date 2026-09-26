package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.DetallePresupuesto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DetallePresupuestoDAO {

    private static final String SELECT_BASE =
            "SELECT id_detalle, id_presupuesto, categoria, monto_presupuestado, monto_ejecutado FROM DETALLE_PRESUPUESTO";

    public List<DetallePresupuesto> listarPorPresupuesto(int idPresupuesto) throws SQLException {
        List<DetallePresupuesto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE id_presupuesto = ? ORDER BY categoria")) {
            ps.setInt(1, idPresupuesto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void insertar(DetallePresupuesto detalle) throws SQLException {
        String sql = "INSERT INTO DETALLE_PRESUPUESTO (id_presupuesto, categoria, monto_presupuestado, monto_ejecutado) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(ps, detalle);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    detalle.setIdDetalle(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(DetallePresupuesto detalle) throws SQLException {
        String sql = "UPDATE DETALLE_PRESUPUESTO SET id_presupuesto = ?, categoria = ?, monto_presupuestado = ?, " +
                "monto_ejecutado = ? WHERE id_detalle = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            establecerParametros(ps, detalle);
            ps.setInt(5, detalle.getIdDetalle());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idDetalle) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM DETALLE_PRESUPUESTO WHERE id_detalle = ?")) {
            ps.setInt(1, idDetalle);
            ps.executeUpdate();
        }
    }

    private void establecerParametros(PreparedStatement ps, DetallePresupuesto detalle) throws SQLException {
        ps.setInt(1, detalle.getIdPresupuesto());
        ps.setString(2, detalle.getCategoria());
        ps.setBigDecimal(3, detalle.getMontoPresupuestado());
        ps.setBigDecimal(4, detalle.getMontoEjecutado());
    }

    private DetallePresupuesto mapear(ResultSet rs) throws SQLException {
        DetallePresupuesto detalle = new DetallePresupuesto();
        detalle.setIdDetalle(rs.getInt("id_detalle"));
        detalle.setIdPresupuesto(rs.getInt("id_presupuesto"));
        detalle.setCategoria(rs.getString("categoria"));
        detalle.setMontoPresupuestado(rs.getBigDecimal("monto_presupuestado"));
        detalle.setMontoEjecutado(rs.getBigDecimal("monto_ejecutado"));
        return detalle;
    }
}
