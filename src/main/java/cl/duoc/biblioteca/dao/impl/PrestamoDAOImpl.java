package cl.duoc.biblioteca.dao.impl;

import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PrestamoDAOImpl implements PrestamoDAO {

    // Se une con estudiantes y libros para mostrar nombres en lugar de IDs
    private static final String SELECT_BASE =
            "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
            + "e.nombre AS estudiante, l.titulo AS libro "
            + "FROM prestamos p "
            + "JOIN estudiantes e ON p.id_estudiante = e.id "
            + "JOIN libros l ON p.id_libro = l.id ";

    @Override
    public void insertar(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamos(id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, prestamo.getIdEstudiante());
            ps.setInt(2, prestamo.getIdLibro());
            ps.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            asignarFechaDevolucion(ps, 4, prestamo);
            ps.setBoolean(5, prestamo.isDevuelto());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    prestamo.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Prestamo> listarTodos() throws SQLException {
        return listar(SELECT_BASE + "ORDER BY p.fecha_prestamo DESC", null);
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) throws SQLException {
        List<Prestamo> resultado = listar(SELECT_BASE + "WHERE p.id = ?", id);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public void actualizar(Prestamo prestamo) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamo.getIdEstudiante());
            ps.setInt(2, prestamo.getIdLibro());
            ps.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            asignarFechaDevolucion(ps, 4, prestamo);
            ps.setBoolean(5, prestamo.isDevuelto());
            ps.setInt(6, prestamo.getId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe un préstamo con ID " + prestamo.getId());
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM prestamos WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe un préstamo con ID " + id);
            }
        }
    }

    @Override
    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        return listar(SELECT_BASE + "WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC", idEstudiante);
    }

    @Override
    public List<Prestamo> listarNoDevueltos() throws SQLException {
        return listar(SELECT_BASE + "WHERE p.devuelto = FALSE ORDER BY p.fecha_prestamo", null);
    }

    @Override
    public Map<String, Integer> librosMasPrestados() throws SQLException {
        String sql = "SELECT l.titulo AS libro, COUNT(p.id) AS cantidad "
                + "FROM prestamos p JOIN libros l ON p.id_libro = l.id "
                + "GROUP BY l.id, l.titulo ORDER BY cantidad DESC, l.titulo";
        Map<String, Integer> ranking = new LinkedHashMap<>();
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ranking.put(rs.getString("libro"), rs.getInt("cantidad"));
            }
        }
        return ranking;
    }

    private List<Prestamo> listar(String sql, Integer parametro) throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapear(rs));
                }
            }
        }
        return prestamos;
    }

    private void asignarFechaDevolucion(PreparedStatement ps, int indice, Prestamo prestamo) throws SQLException {
        if (prestamo.getFechaDevolucion() == null) {
            ps.setNull(indice, Types.DATE);
        } else {
            ps.setDate(indice, Date.valueOf(prestamo.getFechaDevolucion()));
        }
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(rs.getInt("id"));
        prestamo.setIdEstudiante(rs.getInt("id_estudiante"));
        prestamo.setIdLibro(rs.getInt("id_libro"));
        prestamo.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());

        Date devolucion = rs.getDate("fecha_devolucion");
        prestamo.setFechaDevolucion(devolucion != null ? devolucion.toLocalDate() : null);

        prestamo.setDevuelto(rs.getBoolean("devuelto"));
        prestamo.setNombreEstudiante(rs.getString("estudiante"));
        prestamo.setTituloLibro(rs.getString("libro"));
        return prestamo;
    }
}
