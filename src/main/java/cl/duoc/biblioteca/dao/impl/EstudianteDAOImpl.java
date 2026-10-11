package cl.duoc.biblioteca.dao.impl;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EstudianteDAOImpl implements EstudianteDAO {

    @Override
    public void insertar(Estudiante estudiante) throws SQLException {
        String sql = "INSERT INTO estudiantes(nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getRut());
            ps.setString(3, estudiante.getCurso());
            ps.setString(4, estudiante.getCorreo());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    estudiante.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Estudiante> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes ORDER BY nombre";
        List<Estudiante> estudiantes = new ArrayList<>();
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                estudiantes.add(mapear(rs));
            }
        }
        return estudiantes;
    }

    @Override
    public Optional<Estudiante> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes WHERE id = ?";
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
    public Optional<Estudiante> buscarPorRut(String rut) throws SQLException {
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes WHERE rut = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void actualizar(Estudiante estudiante) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getRut());
            ps.setString(3, estudiante.getCurso());
            ps.setString(4, estudiante.getCorreo());
            ps.setInt(5, estudiante.getId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe un estudiante con ID " + estudiante.getId());
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No existe un estudiante con ID " + id);
            }
        }
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("rut"),
                rs.getString("curso"),
                rs.getString("correo")
        );
    }
}
