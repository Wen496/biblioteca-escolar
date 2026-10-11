package cl.duoc.biblioteca;

import cl.duoc.biblioteca.controlador.EstudianteControlador;
import cl.duoc.biblioteca.controlador.LibroControlador;
import cl.duoc.biblioteca.controlador.PrestamoControlador;
import cl.duoc.biblioteca.controlador.UsuarioControlador;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.impl.CategoriaDAOImpl;
import cl.duoc.biblioteca.dao.impl.EstudianteDAOImpl;
import cl.duoc.biblioteca.dao.impl.LibroDAOImpl;
import cl.duoc.biblioteca.dao.impl.PrestamoDAOImpl;
import cl.duoc.biblioteca.dao.impl.UsuarioDAOImpl;
import cl.duoc.biblioteca.util.DatabaseConnection;
import cl.duoc.biblioteca.vista.Login;
import cl.duoc.biblioteca.vista.VentanaBase;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        // Textos de los botones de los cuadros de diálogo en español
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "Aceptar");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");

        // Antes de mostrar la interfaz se comprueba que exista conexión
        try {
            DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, VentanaBase.formatoMensaje(
                    "No fue posible conectarse a la base de datos.\n\n"
                            + "Verifica que MySQL esté en ejecución y que la base biblioteca exista.\n\n"
                            + "Detalle: " + e.getMessage()),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LibroDAO libroDAO = new LibroDAOImpl();
        EstudianteDAO estudianteDAO = new EstudianteDAOImpl();

        UsuarioControlador usuarioControlador = new UsuarioControlador(new UsuarioDAOImpl());
        LibroControlador libroControlador = new LibroControlador(libroDAO, new CategoriaDAOImpl());
        EstudianteControlador estudianteControlador = new EstudianteControlador(estudianteDAO);
        PrestamoControlador prestamoControlador =
                new PrestamoControlador(new PrestamoDAOImpl(), libroDAO, estudianteDAO);

        // Al cerrar la aplicación se detienen los hilos y se cierra la conexión
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            prestamoControlador.cerrar();
            try {
                DatabaseConnection.getInstance().cerrar();
            } catch (SQLException ignorada) {
                // La aplicación ya se está cerrando
            }
        }));

        SwingUtilities.invokeLater(() -> new Login(usuarioControlador, libroControlador,
                estudianteControlador, prestamoControlador).setVisible(true));
    }
}
