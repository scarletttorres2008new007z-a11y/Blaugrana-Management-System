package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.ContratoDAO;
import sv.udb.blaugrana.model.Contrato;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ContratoService {

    private final ContratoDAO contratoDAO = new ContratoDAO();

    public List<Contrato> listar() throws SQLException {
        return contratoDAO.listar();
    }

    public List<Contrato> listarPorJugador(int idJugador) throws SQLException {
        return contratoDAO.listarPorJugador(idJugador);
    }

    public Optional<Contrato> buscarPorId(int idContrato) throws SQLException {
        return contratoDAO.buscarPorId(idContrato);
    }

    public int contarVigentes() throws SQLException {
        return contratoDAO.contarVigentes();
    }

    public BigDecimal nominaMensual() throws SQLException {
        return contratoDAO.sumarNominaMensualVigente();
    }

    public int contarPorVencer(int dias) throws SQLException {
        return contratoDAO.contarPorVencerEnDias(dias);
    }

    public void guardar(Contrato contrato) throws SQLException {
        if (!contrato.getFechaFin().isAfter(contrato.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de finalización debe ser posterior a la fecha de inicio.");
        }

        if ("VIGENTE".equals(contrato.getEstado())) {
            Integer idActual = contrato.getIdContrato() == 0 ? null : contrato.getIdContrato();
            if (contratoDAO.existeContratoVigente(contrato.getIdJugador(), idActual)) {
                throw new IllegalStateException("Este jugador ya tiene un contrato vigente. Finalice o rescinda "
                        + "el contrato actual antes de crear uno nuevo.");
            }
        }

        if (contrato.getIdContrato() == 0) {
            contratoDAO.insertar(contrato);
        } else {
            contratoDAO.actualizar(contrato);
        }
    }

    public void eliminar(int idContrato) throws SQLException {
        contratoDAO.eliminar(idContrato);
    }
}
