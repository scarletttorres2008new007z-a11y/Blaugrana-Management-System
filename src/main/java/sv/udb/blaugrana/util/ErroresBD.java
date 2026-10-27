package sv.udb.blaugrana.util;

import java.sql.SQLException;

/**
 * Traduce las excepciones tecnicas de JDBC / SQL Server (violaciones de
 * CHECK, UNIQUE, FOREIGN KEY, fallas de conexion o de login) a mensajes
 * en español que un usuario final puede entender.
 */
public final class ErroresBD {

    private ErroresBD() {
    }

    public static String mensajeAmigable(SQLException e) {
        String mensaje = e.getMessage() == null ? "" : e.getMessage();

        // Restricciones especificas (mensaje mas util que el generico)
        if (mensaje.contains("UQ_JUGADOR_CAMISETA")) {
            return "Ya existe un jugador con ese número de camiseta.";
        }
        if (mensaje.contains("UQ_PARTICIPACION_PARTIDO_JUGADOR")) {
            return "Este jugador ya tiene una participación registrada en este partido.";
        }
        if (mensaje.contains("UQ_PAGO_JUGADOR_PERIODO")) {
            return "Ya existe un pago registrado para este jugador en este periodo.";
        }
        if (mensaje.contains("UX_CONTRATO_JUGADOR_VIGENTE")) {
            return "Este jugador ya tiene un contrato vigente. Finalice o rescinda el contrato "
                    + "actual antes de crear uno nuevo.";
        }
        if (mensaje.contains("UQ_PARTIDO_UNICO")) {
            return "Ya existe un partido registrado con esa competición, fecha, rival y condición.";
        }
        if (mensaje.contains("UX_PERSONAL_DOCUMENTO")) {
            return "Ya existe un miembro del personal registrado con ese documento.";
        }
        if (mensaje.contains("CK_JUGADOR_CAMISETA")) {
            return "El número de camiseta debe estar entre 1 y 99.";
        }
        if (mensaje.contains("CK_JUGADOR_POSICION")) {
            return "La posición debe ser Portero, Defensa, Centrocampista o Delantero.";
        }
        if (mensaje.contains("CK_CONTRATO_FECHAS")) {
            return "La fecha de finalización del contrato debe ser posterior a la fecha de inicio.";
        }
        if (mensaje.contains("CK_CONTRATO_SALARIO")) {
            return "El salario base del contrato debe ser mayor que cero.";
        }
        if (mensaje.contains("CK_CONTRATO_ESTADO")) {
            return "El estado del contrato debe ser VIGENTE, FINALIZADO o RESCINDIDO.";
        }
        if (mensaje.contains("CK_PARTIDO_GOLES")) {
            return "Los goles a favor y en contra no pueden ser negativos.";
        }
        if (mensaje.contains("CK_PARTIDO_CONDICION")) {
            return "La condición del partido debe ser LOCAL o VISITANTE.";
        }
        if (mensaje.contains("CK_PARTIDO_ESTADO")) {
            return "El estado del partido debe ser PROGRAMADO o FINALIZADO.";
        }
        if (mensaje.contains("CK_PARTIDO_RESULTADO")) {
            return "El resultado del partido debe ser GANADO, PERDIDO o EMPATADO.";
        }
        if (mensaje.contains("CK_PARTICIPACION_VALORES")) {
            return "Los minutos (0-130), goles, asistencias o tarjetas (amarillas 0-2, rojas 0-1) "
                    + "tienen un valor fuera de rango.";
        }
        if (mensaje.contains("CK_BONIFICACION_VALOR")) {
            return "El valor de la bonificación debe ser mayor que cero.";
        }
        if (mensaje.contains("CK_PAGO_MONTOS")) {
            return "El salario, las bonificaciones y las deducciones del pago no pueden ser negativos.";
        }
        if (mensaje.contains("CK_PAGO_ESTADO")) {
            return "El estado del pago debe ser PENDIENTE o PAGADO.";
        }
        if (mensaje.contains("CK_PAGO_TOTAL")) {
            return "El total del pago no coincide con salario + bonificaciones - deducciones.";
        }
        if (mensaje.contains("CK_INGRESO_MONTO")) {
            return "El monto del ingreso debe ser mayor que cero.";
        }
        if (mensaje.contains("CK_EGRESO_MONTO")) {
            return "El monto del egreso debe ser mayor que cero.";
        }
        if (mensaje.contains("CK_DETALLE_MONTOS")) {
            return "Los montos presupuestado y ejecutado no pueden ser negativos.";
        }
        if (mensaje.contains("CK_USUARIO_ESTADO")) {
            return "El estado del usuario debe ser ACTIVO o INACTIVO.";
        }

        // Patrones genericos de SQL Server
        String minuscula = mensaje.toLowerCase();
        if (minuscula.contains("login failed")) {
            return "No se pudo iniciar sesión en la base de datos. Verifique el usuario y la "
                    + "contraseña en config.properties.";
        }
        if (minuscula.contains("cannot open database")) {
            return "La base de datos no existe o el usuario no tiene acceso a ella.";
        }
        if (minuscula.contains("connection refused") || minuscula.contains("connect timed out")) {
            return "No se pudo conectar con SQL Server. Verifique que el servicio esté iniciado y "
                    + "que TCP/IP esté habilitado.";
        }
        if (minuscula.contains("unique key constraint") || minuscula.contains("duplicate key")) {
            return "Ya existe un registro con esos mismos datos.";
        }
        if (minuscula.contains("check constraint")) {
            return "Uno de los valores ingresados no cumple con las reglas del sistema.";
        }
        if (minuscula.contains("reference constraint") || minuscula.contains("foreign key constraint")) {
            if (minuscula.contains("delete") || minuscula.contains("truncate")) {
                return "No se puede eliminar este registro porque tiene información relacionada "
                        + "(contratos, partidos, pagos u otros registros).";
            }
            return "El dato relacionado seleccionado no existe o no es válido.";
        }

        return mensaje;
    }
}
