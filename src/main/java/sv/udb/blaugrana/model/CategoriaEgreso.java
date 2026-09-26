package sv.udb.blaugrana.model;

public class CategoriaEgreso {

    private int idCategoriaEgreso;
    private String nombre;

    public CategoriaEgreso() {
    }

    public CategoriaEgreso(int idCategoriaEgreso, String nombre) {
        this.idCategoriaEgreso = idCategoriaEgreso;
        this.nombre = nombre;
    }

    public int getIdCategoriaEgreso() {
        return idCategoriaEgreso;
    }

    public void setIdCategoriaEgreso(int idCategoriaEgreso) {
        this.idCategoriaEgreso = idCategoriaEgreso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
