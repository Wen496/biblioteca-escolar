package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Categoria;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CategoriaDAO {
    void insertar(Categoria categoria) throws SQLException;
    List<Categoria> listarTodas() throws SQLException;
    Optional<Categoria> buscarPorId(int id) throws SQLException;
    void actualizar(Categoria categoria) throws SQLException;
    void eliminar(int id) throws SQLException;
}
