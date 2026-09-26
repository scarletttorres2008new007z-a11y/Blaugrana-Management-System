package sv.udb.blaugrana.model;

public class CategoriaIngreso {

    private int idCategoriaIngreso;
    private String nombre;

    public CategoriaIngreso() {
    }

    public CategoriaIngreso(int idCategoriaIngreso, String nombre) {
        this.idCategoriaIngreso = idCategoriaIngreso;
        this.nombre = nombre;
    }

    public int getIdCategoriaIngreso() {
        return idCategoriaIngreso;
    }

    public void setIdCategoriaIngreso(int idCategoriaIngreso) {
        this.idCategoriaIngreso = idCategoriaIngreso;
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
