package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Libro;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LibroDAO {
    void insertar(Libro libro) throws SQLException;
    List<Libro> listarTodos() throws SQLException;
    Optional<Libro> buscarPorId(int id) throws SQLException;
    void actualizar(Libro libro) throws SQLException;
    void actualizarStock(int idLibro, int nuevoStock) throws SQLException;
    void eliminar(int id) throws SQLException;
}
