package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.PersonalDAO;
import sv.udb.blaugrana.model.Personal;
import sv.udb.blaugrana.util.Validaciones;

import java.sql.SQLException;
import java.util.List;

public class PersonalService {

    private final PersonalDAO personalDAO = new PersonalDAO();

    public List<Personal> listar() throws SQLException {
        return personalDAO.listar();
    }

    public int contarActivos() throws SQLException {
        return personalDAO.contarActivos();
    }

    public void guardar(Personal personal) throws SQLException {
        if (Validaciones.esVacio(personal.getNombre()) || Validaciones.esVacio(personal.getApellido())
                || Validaciones.esVacio(personal.getCargo())) {
            throw new IllegalArgumentException("El nombre, el apellido y el cargo son obligatorios.");
        }

        if (!Validaciones.esVacio(personal.getDocumento())) {
            Integer idActual = personal.getIdPersonal() == 0 ? null : personal.getIdPersonal();
            if (personalDAO.existeDocumento(personal.getDocumento(), idActual)) {
                throw new IllegalStateException("Ya existe un miembro del personal registrado con ese documento.");
            }
        }

        if (personal.getIdPersonal() == 0) {
            personalDAO.insertar(personal);
        } else {
            personalDAO.actualizar(personal);
        }
    }

    public void eliminar(int idPersonal) throws SQLException {
        personalDAO.eliminar(idPersonal);
    }
}
