package cl.duoc.biblioteca.dao.impl;

import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.modelo.Usuario;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Optional<Usuario> autenticar(String correo, String contrasena) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, contraseña, rol FROM usuarios WHERE correo = ? AND contraseña = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios(nombre, rut, correo, contraseña, rol) VALUES (?, ?, ?, ?, ?)";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getRut());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getContrasena());
            ps.setString(5, usuario.getRol());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, contraseña, rol FROM usuarios ORDER BY id";
        List<Usuario> usuarios = new ArrayList<>();
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
        }
        return usuarios;
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, contraseña, rol FROM usuarios WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, contraseña = ?, rol = ? WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getRut());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getContrasena());
            ps.setString(5, usuario.getRol());
            ps.setInt(6, usuario.getId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe un usuario con ID " + usuario.getId());
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe un usuario con ID " + id);
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("rut"),
                rs.getString("correo"),
                rs.getString("contraseña"),
                rs.getString("rol")
        );
    }
}
