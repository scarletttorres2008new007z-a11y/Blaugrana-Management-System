package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.ParticipacionPartidoDAO;
import sv.udb.blaugrana.model.ParticipacionPartido;

import java.sql.SQLException;
import java.util.List;

public class RendimientoService {

    private final ParticipacionPartidoDAO participacionDAO = new ParticipacionPartidoDAO();

    public List<ParticipacionPartido> listarPorPartido(int idPartido) throws SQLException {
        return participacionDAO.listarPorPartido(idPartido);
    }

    public int golesAcumulados(int idJugador) throws SQLException {
        return participacionDAO.sumarGolesPorJugador(idJugador);
    }

    public int asistenciasAcumuladas(int idJugador) throws SQLException {
        return participacionDAO.sumarAsistenciasPorJugador(idJugador);
    }

    public void guardar(ParticipacionPartido participacion) throws SQLException {
        if (participacion.getIdParticipacion() == 0) {
            participacionDAO.insertar(participacion);
        } else {
            participacionDAO.actualizar(participacion);
        }
    }

    public void eliminar(int idParticipacion) throws SQLException {
        participacionDAO.eliminar(idParticipacion);
    }
}
