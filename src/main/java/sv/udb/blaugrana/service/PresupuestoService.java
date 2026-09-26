package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.DetallePresupuestoDAO;
import sv.udb.blaugrana.dao.PresupuestoDAO;
import sv.udb.blaugrana.model.DetallePresupuesto;
import sv.udb.blaugrana.model.Presupuesto;

import java.sql.SQLException;
import java.util.List;

public class PresupuestoService {

    private final PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
    private final DetallePresupuestoDAO detalleDAO = new DetallePresupuestoDAO();

    public List<Presupuesto> listar() throws SQLException {
        return presupuestoDAO.listar();
    }

    public List<DetallePresupuesto> listarDetalle(int idPresupuesto) throws SQLException {
        return detalleDAO.listarPorPresupuesto(idPresupuesto);
    }

    public void guardarPresupuesto(Presupuesto presupuesto) throws SQLException {
        presupuestoDAO.insertar(presupuesto);
    }

    public void guardarDetalle(DetallePresupuesto detalle) throws SQLException {
        if (detalle.getIdDetalle() == 0) {
            detalleDAO.insertar(detalle);
        } else {
            detalleDAO.actualizar(detalle);
        }
    }

    public void eliminarDetalle(int idDetalle) throws SQLException {
        detalleDAO.eliminar(idDetalle);
    }
}
