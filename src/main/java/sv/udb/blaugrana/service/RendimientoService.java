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

    public int minutosAcumulados(int idJugador) throws SQLException {
        return participacionDAO.sumarMinutosPorJugador(idJugador);
    }

    public int partidosJugados(int idJugador) throws SQLException {
        return participacionDAO.contarPartidosPorJugador(idJugador);
    }

    public void guardar(ParticipacionPartido participacion) throws SQLException {
        if (participacion.getMinutosJugados() < 0 || participacion.getMinutosJugados() > 130) {
            throw new IllegalArgumentException("Los minutos jugados deben estar entre 0 y 130.");
        }
        if (participacion.getGoles() < 0 || participacion.getAsistencias() < 0) {
            throw new IllegalArgumentException("Los goles y las asistencias no pueden ser negativos.");
        }
        if (participacion.getTarjetasAmarillas() < 0 || participacion.getTarjetasAmarillas() > 2) {
            throw new IllegalArgumentException("Las tarjetas amarillas deben estar entre 0 y 2.");
        }
        if (participacion.getTarjetasRojas() < 0 || participacion.getTarjetasRojas() > 1) {
            throw new IllegalArgumentException("Las tarjetas rojas deben estar entre 0 y 1.");
        }

        if (participacion.getIdParticipacion() == 0) {
            if (participacionDAO.existeParticipacion(participacion.getIdPartido(), participacion.getIdJugador())) {
                throw new IllegalStateException("Este jugador ya tiene una participación registrada en este partido.");
            }
            participacionDAO.insertar(participacion);
        } else {
            participacionDAO.actualizar(participacion);
        }
    }

    public void eliminar(int idParticipacion) throws SQLException {
        participacionDAO.eliminar(idParticipacion);
    }
}
