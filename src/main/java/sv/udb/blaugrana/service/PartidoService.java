package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.PartidoDAO;
import sv.udb.blaugrana.model.Partido;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PartidoService {

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

    public void guardar(Partido partido) throws SQLException {
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
