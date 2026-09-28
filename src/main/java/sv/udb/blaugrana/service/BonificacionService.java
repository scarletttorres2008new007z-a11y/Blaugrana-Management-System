package sv.udb.blaugrana.service;

import sv.udb.blaugrana.config.ConexionBD;
import sv.udb.blaugrana.dao.BonificacionDAO;
import sv.udb.blaugrana.dao.ParticipacionPartidoDAO;
import sv.udb.blaugrana.dao.PartidoDAO;
import sv.udb.blaugrana.model.Bonificacion;
import sv.udb.blaugrana.model.ParticipacionPartido;
import sv.udb.blaugrana.model.Partido;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Genera bonificaciones economicas a partir del rendimiento de los
 * jugadores en un partido finalizado. Reglas de negocio (valores de
 * demostracion para el proyecto academico):
 *   - Gol convertido:        $500
 *   - Asistencia:             $250
 *   - Victoria del equipo:  $1,000 (a cada titular)
 */
public class BonificacionService {

    public static final BigDecimal VALOR_GOL = new BigDecimal("500.00");
    public static final BigDecimal VALOR_ASISTENCIA = new BigDecimal("250.00");
    public static final BigDecimal VALOR_VICTORIA = new BigDecimal("1000.00");

    private final BonificacionDAO bonificacionDAO = new BonificacionDAO();
    private final ParticipacionPartidoDAO participacionDAO = new ParticipacionPartidoDAO();
    private final PartidoDAO partidoDAO = new PartidoDAO();

    public List<Bonificacion> listar() throws SQLException {
        return bonificacionDAO.listar();
    }

    public List<Bonificacion> listarPorJugador(int idJugador) throws SQLException {
        return bonificacionDAO.listarPorJugador(idJugador);
    }

    public BigDecimal totalPorJugador(int idJugador) throws SQLException {
        return bonificacionDAO.sumarPorJugador(idJugador);
    }

    public BigDecimal totalPorJugadorYPeriodo(int idJugador, String periodoAnioMes) throws SQLException {
        return bonificacionDAO.sumarPorJugadorYPeriodo(idJugador, periodoAnioMes);
    }

    public BigDecimal totalMesActual() throws SQLException {
        return bonificacionDAO.sumarMesActual();
    }

    public void eliminar(int idBonificacion) throws SQLException {
        bonificacionDAO.eliminar(idBonificacion);
    }

    public void registrarManual(Bonificacion bonificacion) throws SQLException {
        bonificacionDAO.insertar(bonificacion);
    }

    /**
     * Genera las bonificaciones de un partido finalizado a partir de las
     * participaciones registradas, en una sola transaccion: si algo falla
     * a mitad de camino no queda ninguna bonificacion parcial guardada.
     * Si ya existen bonificaciones para el partido, no se generan duplicados.
     */
    public int generarBonificacionesPartido(int idPartido) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                int generadas = generarBonificacionesEnTransaccion(con, idPartido);
                con.commit();
                return generadas;
            } catch (RuntimeException | SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private int generarBonificacionesEnTransaccion(Connection con, int idPartido) throws SQLException {
        Partido partido = partidoDAO.buscarPorId(con, idPartido)
                .orElseThrow(() -> new IllegalArgumentException("El partido indicado no existe."));

        if (!Partido.ESTADO_FINALIZADO.equals(partido.getEstado())) {
            throw new IllegalStateException("Solo se pueden generar bonificaciones de partidos finalizados.");
        }
        if (bonificacionDAO.existeParaPartido(con, idPartido)) {
            throw new IllegalStateException("Ya se generaron bonificaciones para este partido.");
        }

        List<ParticipacionPartido> participaciones = participacionDAO.listarPorPartido(con, idPartido);
        if (participaciones.isEmpty()) {
            throw new IllegalStateException("Este partido no tiene participaciones registradas. Registre el "
                    + "rendimiento de los jugadores antes de generar bonificaciones.");
        }

        boolean victoria = Partido.RESULTADO_GANADO.equals(partido.getResultado());
        int generadas = 0;

        for (ParticipacionPartido participacion : participaciones) {
            for (int i = 0; i < participacion.getGoles(); i++) {
                registrar(con, participacion.getIdJugador(), idPartido, "Gol convertido", VALOR_GOL, partido.getFecha());
                generadas++;
            }
            for (int i = 0; i < participacion.getAsistencias(); i++) {
                registrar(con, participacion.getIdJugador(), idPartido, "Asistencia", VALOR_ASISTENCIA, partido.getFecha());
                generadas++;
            }
            if (victoria && participacion.isTitular()) {
                registrar(con, participacion.getIdJugador(), idPartido, "Victoria del equipo", VALOR_VICTORIA, partido.getFecha());
                generadas++;
            }
        }
        return generadas;
    }

    private void registrar(Connection con, int idJugador, int idPartido, String concepto, BigDecimal valor,
                            LocalDate fecha) throws SQLException {
        Bonificacion bonificacion = new Bonificacion();
        bonificacion.setIdJugador(idJugador);
        bonificacion.setIdPartido(idPartido);
        bonificacion.setConcepto(concepto);
        bonificacion.setValor(valor);
        bonificacion.setFechaGeneracion(fecha);
        bonificacionDAO.insertar(con, bonificacion);
    }
}
