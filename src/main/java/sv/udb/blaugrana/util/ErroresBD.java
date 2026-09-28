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
        if (mensaje.contains("CK_PAGO_TOTAL")) {
            return "El total del pago no coincide con salario + bonificaciones - deducciones.";
        }
        if (mensaje.contains("CK_PARTICIPACION_VALORES")) {
            return "Los minutos, goles, asistencias o tarjetas tienen un valor fuera de rango.";
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
