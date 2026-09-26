package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.CategoriaEgresoDAO;
import sv.udb.blaugrana.dao.CategoriaIngresoDAO;
import sv.udb.blaugrana.dao.EgresoDAO;
import sv.udb.blaugrana.dao.IngresoDAO;
import sv.udb.blaugrana.model.CategoriaEgreso;
import sv.udb.blaugrana.model.CategoriaIngreso;
import sv.udb.blaugrana.model.Egreso;
import sv.udb.blaugrana.model.Ingreso;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class FinanzasService {

    private final IngresoDAO ingresoDAO = new IngresoDAO();
    private final EgresoDAO egresoDAO = new EgresoDAO();
    private final CategoriaIngresoDAO categoriaIngresoDAO = new CategoriaIngresoDAO();
    private final CategoriaEgresoDAO categoriaEgresoDAO = new CategoriaEgresoDAO();

    public List<Ingreso> listarIngresos() throws SQLException {
        return ingresoDAO.listar();
    }

    public List<Egreso> listarEgresos() throws SQLException {
        return egresoDAO.listar();
    }

    public List<CategoriaIngreso> listarCategoriasIngreso() throws SQLException {
        return categoriaIngresoDAO.listar();
    }

    public List<CategoriaEgreso> listarCategoriasEgreso() throws SQLException {
        return categoriaEgresoDAO.listar();
    }

    public void guardarIngreso(Ingreso ingreso) throws SQLException {
        if (ingreso.getIdIngreso() == 0) {
            ingresoDAO.insertar(ingreso);
        } else {
            ingresoDAO.actualizar(ingreso);
        }
    }

    public void eliminarIngreso(int idIngreso) throws SQLException {
        ingresoDAO.eliminar(idIngreso);
    }

    public void guardarEgreso(Egreso egreso) throws SQLException {
        if (egreso.getIdEgreso() == 0) {
            egresoDAO.insertar(egreso);
        } else {
            egresoDAO.actualizar(egreso);
        }
    }

    public void eliminarEgreso(int idEgreso) throws SQLException {
        egresoDAO.eliminar(idEgreso);
    }

    public BigDecimal totalIngresos() throws SQLException {
        return ingresoDAO.sumarTotal();
    }

    public BigDecimal totalEgresos() throws SQLException {
        return egresoDAO.sumarTotal();
    }

    public BigDecimal balance() throws SQLException {
        return totalIngresos().subtract(totalEgresos());
    }
}
