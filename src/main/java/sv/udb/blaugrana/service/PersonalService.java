package sv.udb.blaugrana.service;

import sv.udb.blaugrana.dao.PersonalDAO;
import sv.udb.blaugrana.model.Personal;

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
