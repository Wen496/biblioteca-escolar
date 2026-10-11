package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.EstudianteControlador;
import cl.duoc.biblioteca.controlador.LibroControlador;
import cl.duoc.biblioteca.controlador.PrestamoControlador;
import cl.duoc.biblioteca.controlador.UsuarioControlador;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Menú principal. Los botones se habilitan según el rol del usuario.
 */
public class VentanaPrincipal extends JFrame {

    private final Usuario usuario;
    private final UsuarioControlador usuarioControlador;
    private final LibroControlador libroControlador;
    private final EstudianteControlador estudianteControlador;
    private final PrestamoControlador prestamoControlador;

    public VentanaPrincipal(Usuario usuario, UsuarioControlador usuarioControlador, LibroControlador libroControlador,
                            EstudianteControlador estudianteControlador, PrestamoControlador prestamoControlador) {
        this.usuario = usuario;
        this.usuarioControlador = usuarioControlador;
        this.libroControlador = libroControlador;
        this.estudianteControlador = estudianteControlador;
        this.prestamoControlador = prestamoControlador;

        setTitle("Biblioteca Escolar");
        setSize(460, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirInterfaz();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JLabel bienvenida = new JLabel("Bienvenido/a, " + usuario.getResumen(), SwingConstants.CENTER);
        bienvenida.setFont(new Font("SansSerif", Font.BOLD, 16));
        bienvenida.setBorder(BorderFactory.createEmptyBorder(14, 10, 0, 10));
        add(bienvenida, BorderLayout.NORTH);

        boolean esBibliotecario = usuario.esBibliotecario();

        JButton btnLibros = new JButton(esBibliotecario ? "Gestión de libros" : "Catálogo de libros");
        JButton btnEstudiantes = new JButton("Gestión de estudiantes");
        JButton btnUsuarios = new JButton("Gestión de usuarios");
        JButton btnPrestamos = new JButton("Préstamos y devoluciones");
        JButton btnReportes = new JButton(esBibliotecario ? "Reportes" : "Mis préstamos");
        JButton btnCerrarSesion = new JButton("Cerrar sesión");

        // El estudiante solo puede consultar
        btnEstudiantes.setEnabled(esBibliotecario);
        btnUsuarios.setEnabled(esBibliotecario);
        btnPrestamos.setEnabled(esBibliotecario);

        JPanel menu = new JPanel(new GridLayout(6, 1, 8, 8));
        menu.setBorder(BorderFactory.createEmptyBorder(10, 60, 20, 60));
        menu.add(btnLibros);
        menu.add(btnEstudiantes);
        menu.add(btnUsuarios);
        menu.add(btnPrestamos);
        menu.add(btnReportes);
        menu.add(btnCerrarSesion);
        add(menu, BorderLayout.CENTER);

        btnLibros.addActionListener(e -> new VentanaLibros(libroControlador, !esBibliotecario).setVisible(true));
        btnEstudiantes.addActionListener(e -> new VentanaEstudiantes(estudianteControlador).setVisible(true));
        btnUsuarios.addActionListener(e -> new VentanaUsuarios(usuarioControlador, usuario).setVisible(true));
        btnPrestamos.addActionListener(e ->
                new VentanaPrestamos(prestamoControlador, estudianteControlador, libroControlador).setVisible(true));
        btnReportes.addActionListener(e -> abrirReportes());
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
    }

    private void abrirReportes() {
        if (usuario.esBibliotecario()) {
            new VentanaReportes(prestamoControlador, estudianteControlador, null).setVisible(true);
            return;
        }

        // El usuario estudiante se vincula con su ficha de estudiante por el RUT
        try {
            Optional<Estudiante> estudiante = estudianteControlador.buscarPorRut(usuario.getRut());
            if (estudiante.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        VentanaBase.formatoMensaje(
                                "Tu usuario no está vinculado a un estudiante registrado. Consulta en la biblioteca."),
                        "Sin registro", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new VentanaReportes(prestamoControlador, estudianteControlador, estudiante.get()).setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    VentanaBase.formatoMensaje("No se pudo cargar el historial:\n" + e.getMessage()),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarSesion() {
        // Se cierran también las ventanas abiertas desde este menú
        for (Window ventana : Window.getWindows()) {
            if (ventana != this) {
                ventana.dispose();
            }
        }
        new Login(usuarioControlador, libroControlador, estudianteControlador, prestamoControlador).setVisible(true);
        dispose();
    }
}
