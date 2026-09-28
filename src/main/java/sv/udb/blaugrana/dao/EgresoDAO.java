package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Egreso;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EgresoDAO {

    private static final String SELECT_BASE =
            "SELECT e.id_egreso, e.id_categoria_egreso, c.nombre AS nombre_categoria, e.descripcion, " +
            "       e.monto, e.fecha FROM EGRESO e JOIN CATEGORIA_EGRESO c ON c.id_categoria_egreso = e.id_categoria_egreso";

    public List<Egreso> listar() throws SQLException {
        List<Egreso> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY e.fecha DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public BigDecimal sumarTotal() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT ISNULL(SUM(monto), 0) FROM EGRESO");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    public void insertar(Egreso egreso) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            insertar(con, egreso);
        }
    }

    /**
     * Variante transaccional: usa una conexion ya abierta por el llamador
     * (que controla commit/rollback) en lugar de abrir una propia.
     */
    public void insertar(Connection con, Egreso egreso) throws SQLException {
        String sql = "INSERT INTO EGRESO (id_categoria_egreso, descripcion, monto, fecha) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(ps, egreso);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    egreso.setIdEgreso(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Egreso egreso) throws SQLException {
        String sql = "UPDATE EGRESO SET id_categoria_egreso = ?, descripcion = ?, monto = ?, fecha = ? WHERE id_egreso = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            establecerParametros(ps, egreso);
            ps.setInt(5, egreso.getIdEgreso());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idEgreso) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM EGRESO WHERE id_egreso = ?")) {
            ps.setInt(1, idEgreso);
            ps.executeUpdate();
        }
    }

    private void establecerParametros(PreparedStatement ps, Egreso egreso) throws SQLException {
        ps.setInt(1, egreso.getIdCategoriaEgreso());
        ps.setString(2, egreso.getDescripcion());
        ps.setBigDecimal(3, egreso.getMonto());
        ps.setDate(4, Date.valueOf(egreso.getFecha()));
    }

    private Egreso mapear(ResultSet rs) throws SQLException {
        Egreso egreso = new Egreso();
        egreso.setIdEgreso(rs.getInt("id_egreso"));
        egreso.setIdCategoriaEgreso(rs.getInt("id_categoria_egreso"));
        egreso.setNombreCategoria(rs.getString("nombre_categoria"));
        egreso.setDescripcion(rs.getString("descripcion"));
        egreso.setMonto(rs.getBigDecimal("monto"));
        egreso.setFecha(rs.getDate("fecha").toLocalDate());
        return egreso;
    }
}
