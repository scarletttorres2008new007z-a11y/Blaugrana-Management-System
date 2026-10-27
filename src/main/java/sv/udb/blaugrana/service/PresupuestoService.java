package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.DetallePresupuestoDAO;
import sv.udb.blaugrana.dao.PresupuestoDAO;
import sv.udb.blaugrana.model.DetallePresupuesto;
import sv.udb.blaugrana.model.Presupuesto;
import sv.udb.blaugrana.util.Validaciones;

import java.math.BigDecimal;
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
        if (Validaciones.esVacio(presupuesto.getTemporada()) || Validaciones.esVacio(presupuesto.getNombre())) {
            throw new IllegalArgumentException("La temporada y el nombre del presupuesto son obligatorios.");
        }
        presupuestoDAO.insertar(presupuesto);
    }

    public void guardarDetalle(DetallePresupuesto detalle) throws SQLException {
        if (Validaciones.esVacio(detalle.getCategoria())) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        BigDecimal presupuestado = detalle.getMontoPresupuestado();
        BigDecimal ejecutado = detalle.getMontoEjecutado();
        if (presupuestado == null || presupuestado.compareTo(BigDecimal.ZERO) < 0
                || ejecutado == null || ejecutado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los montos presupuestado y ejecutado no pueden ser negativos.");
        }

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
