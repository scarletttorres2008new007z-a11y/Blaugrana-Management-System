package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.PartidoDAO;
import sv.udb.blaugrana.model.Partido;
import sv.udb.blaugrana.util.Validaciones;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PartidoService {

    private static final Set<String> CONDICIONES_VALIDAS = Set.of(Partido.CONDICION_LOCAL, Partido.CONDICION_VISITANTE);

    private final PartidoDAO partidoDAO = new PartidoDAO();

    public List<Partido> listar() throws SQLException {
        return partidoDAO.listar();
    }

    public Optional<Partido> buscarPorId(int idPartido) throws SQLException {
        return partidoDAO.buscarPorId(idPartido);
    }

    public Optional<Partido> buscarProximo() throws SQLException {
        return partidoDAO.buscarProximo();
    }

    public int contarFinalizados() throws SQLException {
        return partidoDAO.contarFinalizados();
    }

    public int contarProgramados() throws SQLException {
        return partidoDAO.contarProgramados();
    }

    public int contarGanados() throws SQLException {
        return partidoDAO.contarPorResultado(Partido.RESULTADO_GANADO);
    }

    public int contarEmpatados() throws SQLException {
        return partidoDAO.contarPorResultado(Partido.RESULTADO_EMPATADO);
    }

    public int contarPerdidos() throws SQLException {
        return partidoDAO.contarPorResultado(Partido.RESULTADO_PERDIDO);
    }

    public int sumarGolesFavor() throws SQLException {
        return partidoDAO.sumarGolesFavor();
    }

    public int sumarGolesContra() throws SQLException {
        return partidoDAO.sumarGolesContra();
    }

    public void guardar(Partido partido) throws SQLException {
        if (Validaciones.esVacio(partido.getCompeticion()) || Validaciones.esVacio(partido.getRival())) {
            throw new IllegalArgumentException("La competición y el rival son obligatorios.");
        }
        if (partido.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del partido es obligatoria.");
        }
        if (!CONDICIONES_VALIDAS.contains(partido.getCondicion())) {
            throw new IllegalArgumentException("La condición debe ser LOCAL o VISITANTE.");
        }
        if (partido.getGolesFavor() < 0 || partido.getGolesContra() < 0) {
            throw new IllegalArgumentException("Los goles a favor y en contra no pueden ser negativos.");
        }

        if (Partido.ESTADO_FINALIZADO.equals(partido.getEstado())) {
            calcularResultado(partido);
        }
        if (partido.getIdPartido() == 0) {
            partidoDAO.insertar(partido);
        } else {
            partidoDAO.actualizar(partido);
        }
    }

    public void eliminar(int idPartido) throws SQLException {
        partidoDAO.eliminar(idPartido);
    }

    private void calcularResultado(Partido partido) {
        if (partido.getGolesFavor() > partido.getGolesContra()) {
            partido.setResultado(Partido.RESULTADO_GANADO);
        } else if (partido.getGolesFavor() < partido.getGolesContra()) {
            partido.setResultado(Partido.RESULTADO_PERDIDO);
        } else {
            partido.setResultado(Partido.RESULTADO_EMPATADO);
        }
    }
}
