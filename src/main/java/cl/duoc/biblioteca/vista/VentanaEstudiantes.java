package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.EstudianteControlador;
import cl.duoc.biblioteca.modelo.Estudiante;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class VentanaEstudiantes extends VentanaBase {

    private final EstudianteControlador controlador;
    private List<Estudiante> estudiantes;

    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtRut = new JTextField();
    private final JTextField txtCurso = new JTextField();
    private final JTextField txtCorreo = new JTextField();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "RUT", "Curso", "Correo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public VentanaEstudiantes(EstudianteControlador controlador) {
        super("Gestión de estudiantes", 1100, 560);
        this.controlador = controlador;
        construirInterfaz();
        cargarDatos();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridLayout(5, 2, 6, 6));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del estudiante"));
        txtId.setEditable(false);
        formulario.add(new JLabel("ID:"));
        formulario.add(txtId);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("RUT:"));
        formulario.add(txtRut);
        formulario.add(new JLabel("Curso:"));
        formulario.add(txtCurso);
        formulario.add(new JLabel("Correo:"));
        formulario.add(txtCorreo);

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
        scroll.setBorder(BorderFactory.createTitledBorder("Estudiantes registrados"));

        add(izquierda, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);

        btnLimpiar.addActionListener(e -> limpiar());
        btnAgregar.addActionListener(e -> agregar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRecargar.addActionListener(e -> cargarDatos());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                Estudiante est = estudiantes.get(tabla.getSelectedRow());
                txtId.setText(String.valueOf(est.getId()));
                txtNombre.setText(est.getNombre());
                txtRut.setText(est.getRut());
                txtCurso.setText(est.getCurso());
                txtCorreo.setText(est.getCorreo());
            }
        });
    }

    @Override
    protected void cargarDatos() {
        ejecutar(() -> {
            estudiantes = controlador.listar();
            modeloTabla.setRowCount(0);
            for (Estudiante e : estudiantes) {
                modeloTabla.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
            }
            ajustarColumnas(tabla);
        });
    }

    private void agregar() {
        ejecutar(() -> {
            controlador.crear(txtNombre.getText(), txtRut.getText(), txtCurso.getText(), txtCorreo.getText());
            mensaje("Estudiante registrado correctamente.");
            cargarDatos();
            limpiar();
        });
    }

    private void actualizar() {
        ejecutar(() -> {
            controlador.actualizar(idSeleccionado(), txtNombre.getText(), txtRut.getText(),
                    txtCurso.getText(), txtCorreo.getText());
            mensaje("Estudiante actualizado correctamente.");
            cargarDatos();
            limpiar();
        });
    }

    private void eliminar() {
        ejecutar(() -> {
            int id = idSeleccionado();
            if (confirmar("¿Eliminar el estudiante seleccionado?", "Confirmar eliminación")) {
                controlador.eliminar(id);
                mensaje("Estudiante eliminado correctamente.");
                cargarDatos();
                limpiar();
            }
        });
    }

    private int idSeleccionado() {
        if (txtId.getText().isBlank()) {
            throw new IllegalArgumentException("Selecciona primero un estudiante de la tabla.");
        }
        return Integer.parseInt(txtId.getText());
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        tabla.clearSelection();
    }
}
