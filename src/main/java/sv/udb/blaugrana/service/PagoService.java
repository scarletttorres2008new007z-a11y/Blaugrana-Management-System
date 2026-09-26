package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.ContratoDAO;
import sv.udb.blaugrana.dao.PagoDAO;
import sv.udb.blaugrana.model.Contrato;
import sv.udb.blaugrana.model.Pago;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Calcula y registra el pago mensual de un jugador combinando su salario
 * de contrato con las bonificaciones generadas en el periodo indicado.
 */
public class PagoService {

    private final PagoDAO pagoDAO = new PagoDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();
    private final BonificacionService bonificacionService = new BonificacionService();

    public List<Pago> listar() throws SQLException {
        return pagoDAO.listar();
    }

    public List<Pago> listarPorJugador(int idJugador) throws SQLException {
        return pagoDAO.listarPorJugador(idJugador);
    }

    /**
     * Calcula el pago de un jugador para un periodo (formato "yyyy-MM") a
     * partir de su contrato vigente y las bonificaciones de ese periodo.
     * El pago se registra con estado PENDIENTE.
     */
    public Pago generarPago(int idJugador, int idContrato, String periodo, BigDecimal deducciones) throws SQLException {
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

    public void marcarComoPagado(int idPago) throws SQLException {
        pagoDAO.marcarPagado(idPago, LocalDate.now());
    }

    public void eliminar(int idPago) throws SQLException {
        pagoDAO.eliminar(idPago);
    }
}
