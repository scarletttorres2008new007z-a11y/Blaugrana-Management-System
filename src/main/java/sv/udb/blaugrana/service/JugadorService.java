package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.JugadorDAO;
import sv.udb.blaugrana.model.Jugador;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JugadorService {

    private final JugadorDAO jugadorDAO = new JugadorDAO();

    public List<Jugador> listar() throws SQLException {
        return jugadorDAO.listar();
    }

    public List<Jugador> listarActivos() throws SQLException {
        return jugadorDAO.listarActivos();
    }

    public Optional<Jugador> buscarPorId(int idJugador) throws SQLException {
        return jugadorDAO.buscarPorId(idJugador);
    }

    public int contarActivos() throws SQLException {
        return jugadorDAO.contarActivos();
    }

    public void guardar(Jugador jugador) throws SQLException {
        if (jugador.getIdJugador() == 0) {
            jugadorDAO.insertar(jugador);
        } else {
            jugadorDAO.actualizar(jugador);
        }
    }

    public void eliminar(int idJugador) throws SQLException {
        jugadorDAO.eliminar(idJugador);
    }
}
