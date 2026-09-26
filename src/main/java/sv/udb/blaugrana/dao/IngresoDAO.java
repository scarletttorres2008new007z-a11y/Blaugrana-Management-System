package sv.udb.blaugrana.dao;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.model.Ingreso;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IngresoDAO {

    private static final String SELECT_BASE =
            "SELECT i.id_ingreso, i.id_categoria_ingreso, c.nombre AS nombre_categoria, i.descripcion, " +
            "       i.monto, i.fecha FROM INGRESO i JOIN CATEGORIA_INGRESO c ON c.id_categoria_ingreso = i.id_categoria_ingreso";

    public List<Ingreso> listar() throws SQLException {
        List<Ingreso> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " ORDER BY i.fecha DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public BigDecimal sumarTotal() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT ISNULL(SUM(monto), 0) FROM INGRESO");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    public void insertar(Ingreso ingreso) throws SQLException {
        String sql = "INSERT INTO INGRESO (id_categoria_ingreso, descripcion, monto, fecha) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerParametros(ps, ingreso);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    ingreso.setIdIngreso(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Ingreso ingreso) throws SQLException {
        String sql = "UPDATE INGRESO SET id_categoria_ingreso = ?, descripcion = ?, monto = ?, fecha = ? WHERE id_ingreso = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            establecerParametros(ps, ingreso);
            ps.setInt(5, ingreso.getIdIngreso());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idIngreso) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM INGRESO WHERE id_ingreso = ?")) {
            ps.setInt(1, idIngreso);
            ps.executeUpdate();
        }
    }

    private void establecerParametros(PreparedStatement ps, Ingreso ingreso) throws SQLException {
        ps.setInt(1, ingreso.getIdCategoriaIngreso());
        ps.setString(2, ingreso.getDescripcion());
        ps.setBigDecimal(3, ingreso.getMonto());
        ps.setDate(4, Date.valueOf(ingreso.getFecha()));
    }

    private Ingreso mapear(ResultSet rs) throws SQLException {
        Ingreso ingreso = new Ingreso();
        ingreso.setIdIngreso(rs.getInt("id_ingreso"));
        ingreso.setIdCategoriaIngreso(rs.getInt("id_categoria_ingreso"));
        ingreso.setNombreCategoria(rs.getString("nombre_categoria"));
        ingreso.setDescripcion(rs.getString("descripcion"));
        ingreso.setMonto(rs.getBigDecimal("monto"));
        ingreso.setFecha(rs.getDate("fecha").toLocalDate());
        return ingreso;
    }
}
