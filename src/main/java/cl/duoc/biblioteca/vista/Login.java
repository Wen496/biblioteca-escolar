package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.EstudianteControlador;
import cl.duoc.biblioteca.controlador.LibroControlador;
import cl.duoc.biblioteca.controlador.PrestamoControlador;
import cl.duoc.biblioteca.controlador.UsuarioControlador;
import cl.duoc.biblioteca.modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Optional;

public class Login extends JFrame {

    private final UsuarioControlador usuarioControlador;
    private final LibroControlador libroControlador;
    private final EstudianteControlador estudianteControlador;
    private final PrestamoControlador prestamoControlador;

    private final JTextField txtCorreo = new JTextField(20);
    private final JPasswordField txtContrasena = new JPasswordField(20);

    public Login(UsuarioControlador usuarioControlador, LibroControlador libroControlador,
                 EstudianteControlador estudianteControlador, PrestamoControlador prestamoControlador) {
        this.usuarioControlador = usuarioControlador;
        this.libroControlador = libroControlador;
        this.estudianteControlador = estudianteControlador;
        this.prestamoControlador = prestamoControlador;

        setTitle("Biblioteca Escolar - Inicio de sesión");
        setSize(420, 240);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirInterfaz();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Biblioteca Escolar", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(2, 2, 8, 8));
        campos.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        campos.add(new JLabel("Correo:"));
        campos.add(txtCorreo);
        campos.add(new JLabel("Contraseña:"));
        campos.add(txtContrasena);
        add(campos, BorderLayout.CENTER);

        JButton btnIngresar = new JButton("Ingresar");
        JButton btnSalir = new JButton("Salir");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        botones.add(btnIngresar);
        botones.add(btnSalir);
        add(botones, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        try {
            Optional<Usuario> usuario = usuarioControlador.iniciarSesion(
                    txtCorreo.getText(), new String(txtContrasena.getPassword()));

            if (usuario.isEmpty()) {
                JOptionPane.showMessageDialog(this, VentanaBase.formatoMensaje("Correo o contraseña incorrectos."),
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                txtContrasena.setText("");
                return;
            }

            new VentanaPrincipal(usuario.get(), usuarioControlador, libroControlador,
                    estudianteControlador, prestamoControlador).setVisible(true);
            dispose();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, VentanaBase.formatoMensaje(e.getMessage()), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    VentanaBase.formatoMensaje("No se pudo validar el usuario:\n" + e.getMessage()),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }
}
