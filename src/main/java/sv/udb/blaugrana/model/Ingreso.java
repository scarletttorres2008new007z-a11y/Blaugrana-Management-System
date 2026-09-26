package sv.udb.blaugrana.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Ingreso {

    private int idIngreso;
    private int idCategoriaIngreso;
    private String nombreCategoria;
    private String descripcion;
    private BigDecimal monto;
    private LocalDate fecha;

    public Ingreso() {
    }

    public int getIdIngreso() {
        return idIngreso;
    }

    public void setIdIngreso(int idIngreso) {
        this.idIngreso = idIngreso;
    }

    public int getIdCategoriaIngreso() {
        return idCategoriaIngreso;
    }

    public void setIdCategoriaIngreso(int idCategoriaIngreso) {
        this.idCategoriaIngreso = idCategoriaIngreso;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
