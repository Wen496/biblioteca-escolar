package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.UsuarioControlador;
import cl.duoc.biblioteca.modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class VentanaUsuarios extends VentanaBase {

    private final UsuarioControlador controlador;
    private final Usuario sesionActual;
    private List<Usuario> usuarios;

    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtRut = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JComboBox<String> cmbRol =
            new JComboBox<>(new String[]{Usuario.ROL_BIBLIOTECARIO, Usuario.ROL_ESTUDIANTE});

    // La contraseña no se muestra en la tabla
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "RUT", "Correo", "Rol"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public VentanaUsuarios(UsuarioControlador controlador, Usuario sesionActual) {
        super("Gestión de usuarios", 1100, 560);
        this.controlador = controlador;
        this.sesionActual = sesionActual;
        construirInterfaz();
        cargarDatos();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridLayout(6, 2, 6, 6));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));
        txtId.setEditable(false);
        formulario.add(new JLabel("ID:"));
        formulario.add(txtId);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("RUT:"));
        formulario.add(txtRut);
        formulario.add(new JLabel("Correo:"));
        formulario.add(txtCorreo);
        formulario.add(new JLabel("Contraseña:"));
        formulario.add(txtContrasena);
        formulario.add(new JLabel("Rol:"));
        formulario.add(cmbRol);

        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRecargar = new JButton("Recargar");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        acciones.add(btnLimpiar);
        acciones.add(btnAgregar);
        acciones.add(btnActualizar);
        acciones.add(btnEliminar);
        acciones.add(btnRecargar);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        izquierda.add(formulario, BorderLayout.NORTH);
        izquierda.add(acciones, BorderLayout.CENTER);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Usuarios del sistema"));

        add(izquierda, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);

        btnLimpiar.addActionListener(e -> limpiar());
        btnAgregar.addActionListener(e -> agregar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRecargar.addActionListener(e -> cargarDatos());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                Usuario u = usuarios.get(tabla.getSelectedRow());
                txtId.setText(String.valueOf(u.getId()));
                txtNombre.setText(u.getNombre());
                txtRut.setText(u.getRut());
                txtCorreo.setText(u.getCorreo());
                txtContrasena.setText(u.getContrasena());
                cmbRol.setSelectedItem(u.getRol());
            }
        });
    }

    @Override
    protected void cargarDatos() {
        ejecutar(() -> {
            usuarios = controlador.listar();
            modeloTabla.setRowCount(0);
            for (Usuario u : usuarios) {
                modeloTabla.addRow(new Object[]{u.getId(), u.getNombre(), u.getRut(), u.getCorreo(), u.getRol()});
            }
            ajustarColumnas(tabla);
        });
    }

    private void agregar() {
        ejecutar(() -> {
            controlador.crear(txtNombre.getText(), txtRut.getText(), txtCorreo.getText(),
                    new String(txtContrasena.getPassword()), (String) cmbRol.getSelectedItem());
            mensaje("Usuario creado correctamente.");
            cargarDatos();
            limpiar();
        });
    }

    private void actualizar() {
        ejecutar(() -> {
            controlador.actualizar(idSeleccionado(), txtNombre.getText(), txtRut.getText(), txtCorreo.getText(),
                    new String(txtContrasena.getPassword()), (String) cmbRol.getSelectedItem());
            mensaje("Usuario actualizado correctamente.");
            cargarDatos();
            limpiar();
        });
    }

    private void eliminar() {
        ejecutar(() -> {
            int id = idSeleccionado();
            if (confirmar("¿Eliminar el usuario seleccionado?", "Confirmar eliminación")) {
                controlador.eliminar(id, sesionActual);
                mensaje("Usuario eliminado correctamente.");
                cargarDatos();
                limpiar();
            }
        });
    }

    private int idSeleccionado() {
        if (txtId.getText().isBlank()) {
            throw new IllegalArgumentException("Selecciona primero un usuario de la tabla.");
        }
        return Integer.parseInt(txtId.getText());
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtRut.setText("");
        txtCorreo.setText("");
        txtContrasena.setText("");
        cmbRol.setSelectedIndex(0);
        tabla.clearSelection();
    }
}
