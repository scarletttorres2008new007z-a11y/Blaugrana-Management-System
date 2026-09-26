package sv.udb.blaugrana.model;

import java.math.BigDecimal;

public class DetallePresupuesto {

    private int idDetalle;
    private int idPresupuesto;
    private String categoria;
    private BigDecimal montoPresupuestado;
    private BigDecimal montoEjecutado;

    public DetallePresupuesto() {
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdPresupuesto() {
        return idPresupuesto;
    }

    public void setIdPresupuesto(int idPresupuesto) {
        this.idPresupuesto = idPresupuesto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getMontoPresupuestado() {
        return montoPresupuestado;
    }

    public void setMontoPresupuestado(BigDecimal montoPresupuestado) {
        this.montoPresupuestado = montoPresupuestado;
    }

    public BigDecimal getMontoEjecutado() {
        return montoEjecutado;
    }

    public void setMontoEjecutado(BigDecimal montoEjecutado) {
        this.montoEjecutado = montoEjecutado;
    }

    public BigDecimal getDisponible() {
        return montoPresupuestado.subtract(montoEjecutado);
    }
}
