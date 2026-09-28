package sv.udb.blaugrana.session;

import java.util.Map;
import java.util.Set;

/**
 * Matriz de permisos por rol: que modulos (claves usadas por el
 * CardLayout de FrmPrincipal) puede ver cada rol, y que roles solo
 * pueden consultar informacion sin poder crear, editar o eliminar.
 */
public final class Permisos {

    private static final Map<String, Set<String>> MODULOS_POR_ROL = Map.of(
            "Administrador", Set.of("dashboard", "jugadores", "personal", "contratos", "partidos",
                    "rendimiento", "bonificaciones", "pagos", "ingresos", "egresos", "presupuesto",
                    "reportes", "usuarios"),
            "Directivo", Set.of("dashboard", "jugadores", "personal", "contratos", "partidos",
                    "rendimiento", "bonificaciones", "pagos", "ingresos", "egresos", "presupuesto",
                    "reportes"),
            "Gestor Deportivo", Set.of("dashboard", "jugadores", "partidos", "rendimiento",
                    "bonificaciones", "reportes"),
            "Gestor Financiero", Set.of("dashboard", "contratos", "pagos", "ingresos", "egresos",
                    "presupuesto", "bonificaciones", "reportes")
    );

    private static final Set<String> ROLES_SOLO_LECTURA = Set.of("Directivo");

    private Permisos() {
    }

    public static boolean puedeAcceder(String rol, String modulo) {
        return MODULOS_POR_ROL.getOrDefault(rol, Set.of()).contains(modulo);
    }

    public static boolean esSoloLectura(String rol) {
        return ROLES_SOLO_LECTURA.contains(rol);
    }
}
