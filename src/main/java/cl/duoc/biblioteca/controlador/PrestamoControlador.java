package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Coordina préstamos, devoluciones y reportes.
 * Se crea una sola instancia en Main, así todos los hilos comparten el mismo
 * objeto y los métodos synchronized protegen el stock.
 */
public class PrestamoControlador {

    /**
     * La vista implementa esta interfaz para saber cómo terminó un préstamo
     * que se procesó en segundo plano.
     */
    public interface ResultadoPrestamo {
        void exito(Prestamo prestamo);
        void error(String mensaje);
    }

    private final PrestamoDAO prestamoDAO;
    private final LibroDAO libroDAO;
    private final EstudianteDAO estudianteDAO;
    private final ExecutorService ejecutor = Executors.newFixedThreadPool(3);

    public PrestamoControlador(PrestamoDAO prestamoDAO, LibroDAO libroDAO, EstudianteDAO estudianteDAO) {
        this.prestamoDAO = prestamoDAO;
        this.libroDAO = libroDAO;
        this.estudianteDAO = estudianteDAO;
    }

    /**
     * Registra el préstamo en un hilo aparte para que la ventana no se congele
     * mientras se guarda en la base de datos.
     */
    public void prestarEnSegundoPlano(int idEstudiante, int idLibro, ResultadoPrestamo resultado) {
        ejecutor.submit(() -> {
            try {
                Prestamo prestamo = registrarPrestamo(idEstudiante, idLibro);
                resultado.exito(prestamo);
            } catch (IllegalArgumentException | IllegalStateException e) {
                resultado.error(e.getMessage());
            } catch (SQLException e) {
                resultado.error("Error de base de datos: " + e.getMessage());
            }
        });
    }

    /**
     * Revisa el stock, guarda el préstamo y descuenta un ejemplar.
     * Es synchronized porque si dos préstamos del mismo libro llegan al mismo
     * tiempo, ambos podrían leer stock 1 y dejarlo en -1.
     */
    public synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro) throws SQLException {
        if (idEstudiante <= 0) {
            throw new IllegalArgumentException("Selecciona un estudiante.");
        }
        if (idLibro <= 0) {
            throw new IllegalArgumentException("Selecciona un libro.");
        }
        if (estudianteDAO.buscarPorId(idEstudiante).isEmpty()) {
            throw new IllegalArgumentException("El estudiante seleccionado no existe.");
        }

        Libro libro = libroDAO.buscarPorId(idLibro)
                .orElseThrow(() -> new IllegalArgumentException("El libro seleccionado no existe."));

        if (!libro.estaDisponible()) {
            throw new IllegalStateException("No quedan ejemplares disponibles de \"" + libro.getTitulo() + "\".");
        }

        Prestamo prestamo = new Prestamo(idEstudiante, idLibro, LocalDate.now());
        prestamoDAO.insertar(prestamo);
        libroDAO.actualizarStock(idLibro, libro.getStock() - 1);

        prestamo.setTituloLibro(libro.getTitulo());
        return prestamo;
    }

    /**
     * Marca el préstamo como devuelto con la fecha de hoy y devuelve el
     * ejemplar al stock. También modifica el stock, por eso es synchronized.
     */
    public synchronized Prestamo registrarDevolucion(int idPrestamo) throws SQLException {
        Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo)
                .orElseThrow(() -> new IllegalArgumentException("El préstamo seleccionado no existe."));

        if (prestamo.isDevuelto()) {
            throw new IllegalStateException("Este préstamo ya fue devuelto.");
        }

        prestamo.setDevuelto(true);
        prestamo.setFechaDevolucion(LocalDate.now());
        prestamoDAO.actualizar(prestamo);

        Libro libro = libroDAO.buscarPorId(prestamo.getIdLibro())
                .orElseThrow(() -> new IllegalStateException("El libro del préstamo ya no existe."));
        libroDAO.actualizarStock(libro.getId(), libro.getStock() + 1);

        return prestamo;
    }

    public List<Prestamo> listarNoDevueltos() throws SQLException {
        return prestamoDAO.listarNoDevueltos();
    }

    public List<Prestamo> historialDe(int idEstudiante) throws SQLException {
        return prestamoDAO.listarPorEstudiante(idEstudiante);
    }

    public Map<String, Integer> librosMasPrestados() throws SQLException {
        return prestamoDAO.librosMasPrestados();
    }

    public void cerrar() {
        ejecutor.shutdown();
    }
}
