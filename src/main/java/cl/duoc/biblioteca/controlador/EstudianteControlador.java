package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.modelo.Estudiante;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

public class EstudianteControlador {

    private final EstudianteDAO estudianteDAO;

    public EstudianteControlador(EstudianteDAO estudianteDAO) {
        this.estudianteDAO = estudianteDAO;
    }

    public void crear(String nombre, String rut, String curso, String correo) throws SQLException {
        validar(nombre, rut, curso, correo);
        try {
            estudianteDAO.insertar(new Estudiante(nombre.trim(), rut.trim(), curso.trim(), correo.trim()));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Ya existe un estudiante con el RUT " + rut.trim() + ".");
        }
    }

    public List<Estudiante> listar() throws SQLException {
        return estudianteDAO.listarTodos();
    }

    public Optional<Estudiante> buscarPorRut(String rut) throws SQLException {
        return estudianteDAO.buscarPorRut(rut);
    }

    public void actualizar(int id, String nombre, String rut, String curso, String correo) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un estudiante de la tabla.");
        }
        validar(nombre, rut, curso, correo);
        try {
            estudianteDAO.actualizar(new Estudiante(id, nombre.trim(), rut.trim(), curso.trim(), correo.trim()));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Ya existe otro estudiante con el RUT " + rut.trim() + ".");
        }
    }

    public void eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un estudiante de la tabla.");
        }
        try {
            estudianteDAO.eliminar(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("No se puede eliminar el estudiante porque tiene préstamos registrados.");
        }
    }

    private void validar(String nombre, String rut, String curso, String correo) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (rut == null || rut.isBlank()) {
            throw new IllegalArgumentException("El RUT es obligatorio.");
        }
        if (curso == null || curso.isBlank()) {
            throw new IllegalArgumentException("El curso es obligatorio.");
        }
        if (correo == null || !correo.contains("@")) {
            throw new IllegalArgumentException("Ingresa un correo válido.");
        }
    }
}
