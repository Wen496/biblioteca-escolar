package cl.duoc.biblioteca.dao.impl;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAOImpl implements CategoriaDAO {

    @Override
    public void insertar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categorias(nombre) VALUES (?)";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNombre());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    categoria.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Categoria> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM categorias ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return categorias;
    }

    @Override
    public Optional<Categoria> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categorias WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Categoria(rs.getInt("id"), rs.getString("nombre")));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe una categoría con ID " + categoria.getId());
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe una categoría con ID " + id);
            }
        }
    }
}
