package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {
    Optional<Usuario> autenticar(String correo, String contrasena) throws SQLException;
    void insertar(Usuario usuario) throws SQLException;
    List<Usuario> listarTodos() throws SQLException;
    Optional<Usuario> buscarPorId(int id) throws SQLException;
    void actualizar(Usuario usuario) throws SQLException;
    void eliminar(int id) throws SQLException;
}
