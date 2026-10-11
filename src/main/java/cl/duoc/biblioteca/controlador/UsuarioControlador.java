package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.modelo.Usuario;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

public class UsuarioControlador {

    private final UsuarioDAO usuarioDAO;

    public UsuarioControlador(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Optional<Usuario> iniciarSesion(String correo, String contrasena) throws SQLException {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("Ingresa tu correo y contraseña.");
        }
        return usuarioDAO.autenticar(correo.trim(), contrasena);
    }

    public void crear(String nombre, String rut, String correo, String contrasena, String rol) throws SQLException {
        validar(nombre, rut, correo, contrasena, rol);
        try {
            usuarioDAO.insertar(new Usuario(nombre.trim(), rut.trim(), correo.trim(), contrasena, rol));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Ya existe un usuario con el RUT " + rut.trim() + ".");
        }
    }

    public List<Usuario> listar() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public void actualizar(int id, String nombre, String rut, String correo, String contrasena, String rol)
            throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un usuario de la tabla.");
        }
        validar(nombre, rut, correo, contrasena, rol);
        try {
            usuarioDAO.actualizar(new Usuario(id, nombre.trim(), rut.trim(), correo.trim(), contrasena, rol));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Ya existe otro usuario con el RUT " + rut.trim() + ".");
        }
    }

    public void eliminar(int id, Usuario sesionActual) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un usuario de la tabla.");
        }
        if (sesionActual != null && sesionActual.getId() == id) {
            throw new IllegalArgumentException("No puedes eliminar el usuario con el que iniciaste sesión.");
        }
        usuarioDAO.eliminar(id);
    }

    private void validar(String nombre, String rut, String correo, String contrasena, String rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (rut == null || rut.isBlank()) {
            throw new IllegalArgumentException("El RUT es obligatorio.");
        }
        if (correo == null || !correo.contains("@")) {
            throw new IllegalArgumentException("Ingresa un correo válido.");
        }
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (!Usuario.ROL_BIBLIOTECARIO.equals(rol) && !Usuario.ROL_ESTUDIANTE.equals(rol)) {
            throw new IllegalArgumentException("Selecciona un rol válido.");
        }
    }
}
