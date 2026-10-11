package cl.duoc.biblioteca.dao.impl;

import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibroDAOImpl implements LibroDAO {

    // Se une con categorias para mostrar el nombre de la categoría en la tabla
    private static final String SELECT_BASE =
            "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria "
            + "FROM libros l LEFT JOIN categorias c ON l.id_categoria = c.id ";

    @Override
    public void insertar(Libro libro) throws SQLException {
        String sql = "INSERT INTO libros(titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setString(4, libro.getEditorial());
            ps.setInt(5, libro.getStock());
            ps.setInt(6, libro.getIdCategoria());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    libro.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Libro> listarTodos() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY l.titulo";
        List<Libro> libros = new ArrayList<>();
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                libros.add(mapear(rs));
            }
        }
        return libros;
    }

    @Override
    public Optional<Libro> buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE l.id = ?";
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
    public void actualizar(Libro libro) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setString(4, libro.getEditorial());
            ps.setInt(5, libro.getStock());
            ps.setInt(6, libro.getIdCategoria());
            ps.setInt(7, libro.getId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe un libro con ID " + libro.getId());
            }
        }
    }

    @Override
    public void actualizarStock(int idLibro, int nuevoStock) throws SQLException {
        String sql = "UPDATE libros SET stock = ? WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, idLibro);
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM libros WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe un libro con ID " + id);
            }
        }
    }

    private Libro mapear(ResultSet rs) throws SQLException {
        Libro libro = new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("isbn"),
                rs.getString("editorial"),
                rs.getInt("stock"),
                rs.getInt("id_categoria")
        );
        libro.setNombreCategoria(rs.getString("categoria"));
        return libro;
    }
}
