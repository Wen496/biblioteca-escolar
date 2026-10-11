package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Prestamo;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PrestamoDAO {
    void insertar(Prestamo prestamo) throws SQLException;
    List<Prestamo> listarTodos() throws SQLException;
    Optional<Prestamo> buscarPorId(int id) throws SQLException;
    void actualizar(Prestamo prestamo) throws SQLException;
    void eliminar(int id) throws SQLException;

    // Consultas usadas por los reportes
    List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException;
    List<Prestamo> listarNoDevueltos() throws SQLException;
    Map<String, Integer> librosMasPrestados() throws SQLException;
}
