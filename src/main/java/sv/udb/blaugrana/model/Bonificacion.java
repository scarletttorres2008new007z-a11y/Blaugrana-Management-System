package sv.udb.blaugrana.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Bonificacion {

    private int idBonificacion;
    private int idJugador;
    private String nombreJugador;
    private Integer idPartido;
    private String concepto;
    private BigDecimal valor;
    private LocalDate fechaGeneracion;

    public Bonificacion() {
    }

    public int getIdBonificacion() {
        return idBonificacion;
    }

    public void setIdBonificacion(int idBonificacion) {
        this.idBonificacion = idBonificacion;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(int idJugador) {
        this.idJugador = idJugador;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public Integer getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(Integer idPartido) {
        this.idPartido = idPartido;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
}
