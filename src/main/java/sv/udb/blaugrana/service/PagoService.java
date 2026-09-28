package sv.udb.blaugrana.service;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.dao.CategoriaEgresoDAO;
import sv.udb.blaugrana.dao.ContratoDAO;
import sv.udb.blaugrana.dao.EgresoDAO;
import sv.udb.blaugrana.dao.PagoDAO;
import sv.udb.blaugrana.model.Contrato;
import sv.udb.blaugrana.model.Egreso;
import sv.udb.blaugrana.model.Pago;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Calcula y registra el pago mensual de un jugador combinando su salario
 * de contrato con las bonificaciones generadas en el periodo indicado.
 */
public class PagoService {

    private static final String CATEGORIA_EGRESO_SALARIOS = "Salarios";

    private final PagoDAO pagoDAO = new PagoDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();
    private final EgresoDAO egresoDAO = new EgresoDAO();
    private final CategoriaEgresoDAO categoriaEgresoDAO = new CategoriaEgresoDAO();
    private final BonificacionService bonificacionService = new BonificacionService();

    public List<Pago> listar() throws SQLException {
        return pagoDAO.listar();
    }

    public List<Pago> listarPorJugador(int idJugador) throws SQLException {
        return pagoDAO.listarPorJugador(idJugador);
    }

    public int contarPendientes() throws SQLException {
        return pagoDAO.contarPendientes();
    }

    public BigDecimal sumarPendientes() throws SQLException {
        return pagoDAO.sumarPendientes();
    }

    /**
     * Calcula el pago de un jugador para un periodo (formato "yyyy-MM") a
     * partir de su contrato vigente y las bonificaciones de ese periodo.
     * El pago se registra con estado PENDIENTE.
     */
    public Pago generarPago(int idJugador, int idContrato, String periodo, BigDecimal deducciones) throws SQLException {
        if (pagoDAO.existePagoPorJugadorYPeriodo(idJugador, periodo)) {
            throw new IllegalStateException("Ya existe un pago registrado para este jugador en el periodo " + periodo + ".");
        }

        Contrato contrato = contratoDAO.buscarPorId(idContrato)
                .orElseThrow(() -> new IllegalArgumentException("El contrato indicado no existe."));

        BigDecimal salarioMensual = contrato.getSalarioBase()
                .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        BigDecimal bonificaciones = bonificacionService.totalPorJugadorYPeriodo(idJugador, periodo);
        BigDecimal deduccionesFinal = deducciones != null ? deducciones : BigDecimal.ZERO;
        BigDecimal total = salarioMensual.add(bonificaciones).subtract(deduccionesFinal);

        Pago pago = new Pago();
        pago.setIdJugador(idJugador);
        pago.setIdContrato(idContrato);
        pago.setPeriodo(periodo);
        pago.setSalarioBase(salarioMensual);
        pago.setBonificaciones(bonificaciones);
        pago.setDeducciones(deduccionesFinal);
        pago.setTotal(total);
        pago.setEstado(Pago.ESTADO_PENDIENTE);
        pagoDAO.insertar(pago);
        return pago;
    }

    /**
     * Marca un pago como PAGADO y, en la misma transaccion, registra el
     * egreso correspondiente (categoria "Salarios") para que la situacion
     * financiera del club quede actualizada automaticamente.
     */
    public void marcarComoPagado(int idPago) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                Pago pago = pagoDAO.buscarPorId(con, idPago)
                        .orElseThrow(() -> new IllegalArgumentException("El pago indicado no existe."));
                if (Pago.ESTADO_PAGADO.equals(pago.getEstado())) {
                    throw new IllegalStateException("Este pago ya estaba marcado como pagado.");
                }

                LocalDate fechaPago = LocalDate.now();
                pagoDAO.marcarPagado(con, idPago, fechaPago);

                int idCategoriaSalarios = categoriaEgresoDAO.obtenerOCrear(con, CATEGORIA_EGRESO_SALARIOS);
                Egreso egreso = new Egreso();
                egreso.setIdCategoriaEgreso(idCategoriaSalarios);
                egreso.setDescripcion("Pago de salario - " + pago.getNombreJugador() + " - periodo " + pago.getPeriodo());
                egreso.setMonto(pago.getTotal());
                egreso.setFecha(fechaPago);
                egresoDAO.insertar(con, egreso);

                con.commit();
            } catch (RuntimeException | SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void eliminar(int idPago) throws SQLException {
        pagoDAO.eliminar(idPago);
    }
}
