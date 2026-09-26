package sv.udb.blaugrana.model;

import java.time.LocalDate;

public class Partido {

    public static final String CONDICION_LOCAL = "LOCAL";
    public static final String CONDICION_VISITANTE = "VISITANTE";

    public static final String ESTADO_PROGRAMADO = "PROGRAMADO";
    public static final String ESTADO_FINALIZADO = "FINALIZADO";

    public static final String RESULTADO_GANADO = "GANADO";
    public static final String RESULTADO_PERDIDO = "PERDIDO";
    public static final String RESULTADO_EMPATADO = "EMPATADO";

    private int idPartido;
    private String competicion;
    private LocalDate fecha;
    private String rival;
    private String condicion;
    private int golesFavor;
    private int golesContra;
    private String resultado;
    private String estado;

    public Partido() {
    }

    public int getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(int idPartido) {
        this.idPartido = idPartido;
    }

    public String getCompeticion() {
        return competicion;
    }

    public void setCompeticion(String competicion) {
        this.competicion = competicion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getRival() {
        return rival;
    }

    public void setRival(String rival) {
        this.rival = rival;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public int getGolesFavor() {
        return golesFavor;
    }

    public void setGolesFavor(int golesFavor) {
        this.golesFavor = golesFavor;
    }

    public int getGolesContra() {
        return golesContra;
    }

    public void setGolesContra(int golesContra) {
        this.golesContra = golesContra;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMarcador() {
        return golesFavor + " - " + golesContra;
    }

    @Override
    public String toString() {
        return competicion + " | FC Barcelona vs " + rival + " (" + fecha + ")";
    }
}
