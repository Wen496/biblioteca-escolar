package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Libro;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class LibroControlador {

    private final LibroDAO libroDAO;
    private final CategoriaDAO categoriaDAO;

    public LibroControlador(LibroDAO libroDAO, CategoriaDAO categoriaDAO) {
        this.libroDAO = libroDAO;
        this.categoriaDAO = categoriaDAO;
    }

    public void crear(String titulo, String autor, String isbn, String editorial, int stock, Categoria categoria)
            throws SQLException {
        validar(titulo, autor, isbn, editorial, stock, categoria);
        try {
            libroDAO.insertar(new Libro(titulo.trim(), autor.trim(), isbn.trim(), editorial.trim(), stock,
                    categoria.getId()));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Ya existe un libro con el ISBN " + isbn.trim() + ".");
        }
    }

    public List<Libro> listar() throws SQLException {
        return libroDAO.listarTodos();
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listarTodas();
    }

    public void actualizar(int id, String titulo, String autor, String isbn, String editorial, int stock,
                           Categoria categoria) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un libro de la tabla.");
        }
        validar(titulo, autor, isbn, editorial, stock, categoria);
        try {
            libroDAO.actualizar(new Libro(id, titulo.trim(), autor.trim(), isbn.trim(), editorial.trim(), stock,
                    categoria.getId()));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Ya existe otro libro con el ISBN " + isbn.trim() + ".");
        }
    }

    public void eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Selecciona un libro de la tabla.");
        }
        try {
            libroDAO.eliminar(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("No se puede eliminar el libro porque tiene préstamos registrados.");
        }
    }

    private void validar(String titulo, String autor, String isbn, String editorial, int stock, Categoria categoria) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (autor == null || autor.isBlank()) {
            throw new IllegalArgumentException("El autor es obligatorio.");
        }
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("El ISBN es obligatorio.");
        }
        if (editorial == null || editorial.isBlank()) {
            throw new IllegalArgumentException("La editorial es obligatoria.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Selecciona una categoría.");
        }
    }
}
