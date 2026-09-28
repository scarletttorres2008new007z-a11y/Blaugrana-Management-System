package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.JugadorDAO;
import sv.udb.blaugrana.model.Jugador;
import sv.udb.blaugrana.util.Validaciones;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JugadorService {

    private static final Set<String> POSICIONES_VALIDAS =
            Set.of("Portero", "Defensa", "Centrocampista", "Delantero");

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
        if (Validaciones.esVacio(jugador.getNombre()) || Validaciones.esVacio(jugador.getApellido())) {
            throw new IllegalArgumentException("El nombre y el apellido son obligatorios.");
        }
        if (jugador.getNumeroCamiseta() < 1 || jugador.getNumeroCamiseta() > 99) {
            throw new IllegalArgumentException("El número de camiseta debe estar entre 1 y 99.");
        }
        if (!POSICIONES_VALIDAS.contains(jugador.getPosicion())) {
            throw new IllegalArgumentException("La posición debe ser Portero, Defensa, Centrocampista o Delantero.");
        }

        Integer idActual = jugador.getIdJugador() == 0 ? null : jugador.getIdJugador();
        if (jugadorDAO.existeNumeroCamiseta(jugador.getNumeroCamiseta(), idActual)) {
            throw new IllegalStateException("Ya existe un jugador con el número de camiseta "
                    + jugador.getNumeroCamiseta() + ".");
        }

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
