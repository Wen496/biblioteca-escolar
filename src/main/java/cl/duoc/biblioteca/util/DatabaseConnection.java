package cl.duoc.biblioteca.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Maneja la conexión con MySQL usando el patrón Singleton.
 * Toda la aplicación comparte una sola instancia y una sola conexión.
 */
public final class DatabaseConnection {

    // Ajustar según la configuración local de MySQL
    private static final String URL =
            "jdbc:mysql://localhost:3306/biblioteca?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=America/Santiago";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "password";

    private static DatabaseConnection instancia;

    private Connection conexion;

    private DatabaseConnection() throws SQLException {
        conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

    /**
     * Devuelve la única instancia de la clase. Es synchronized para que dos
     * hilos no creen dos instancias al mismo tiempo.
     */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instancia == null) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    /**
     * Entrega la conexión. Si se cerró o se perdió (por ejemplo, porque MySQL
     * se reinició), se vuelve a abrir.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (conexion == null || !conexion.isValid(2)) {
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        }
        return conexion;
    }

    public synchronized void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("No se pudo cerrar la conexión: " + e.getMessage());
        }
    }
}
