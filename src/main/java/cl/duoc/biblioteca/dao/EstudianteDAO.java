package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Estudiante;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EstudianteDAO {
    void insertar(Estudiante estudiante) throws SQLException;
    List<Estudiante> listarTodos() throws SQLException;
    Optional<Estudiante> buscarPorId(int id) throws SQLException;
    Optional<Estudiante> buscarPorRut(String rut) throws SQLException;
    void actualizar(Estudiante estudiante) throws SQLException;
    void eliminar(int id) throws SQLException;
}
