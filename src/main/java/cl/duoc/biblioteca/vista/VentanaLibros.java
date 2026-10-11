package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.LibroControlador;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Libro;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class VentanaLibros extends VentanaBase {

    private final LibroControlador controlador;
    private final boolean soloLectura;
    private List<Libro> libros;

    private final JTextField txtId = new JTextField();
    private final JTextField txtTitulo = new JTextField();
    private final JTextField txtAutor = new JTextField();
    private final JTextField txtIsbn = new JTextField();
    private final JTextField txtEditorial = new JTextField();
    private final JTextField txtStock = new JTextField();
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public VentanaLibros(LibroControlador controlador, boolean soloLectura) {
        super(soloLectura ? "Catálogo de libros" : "Gestión de libros", 1150, 580);
        this.controlador = controlador;
        this.soloLectura = soloLectura;
        construirInterfaz();
        cargarCategorias();
        cargarDatos();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridLayout(7, 2, 6, 6));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del libro"));
        txtId.setEditable(false);
        formulario.add(new JLabel("ID:"));
        formulario.add(txtId);
        formulario.add(new JLabel("Título:"));
        formulario.add(txtTitulo);
        formulario.add(new JLabel("Autor:"));
        formulario.add(txtAutor);
        formulario.add(new JLabel("ISBN:"));
        formulario.add(txtIsbn);
        formulario.add(new JLabel("Editorial:"));
        formulario.add(txtEditorial);
        formulario.add(new JLabel("Stock:"));
        formulario.add(txtStock);
        formulario.add(new JLabel("Categoría:"));
        formulario.add(cmbCategoria);

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
        scroll.setBorder(BorderFactory.createTitledBorder("Libros registrados"));

        if (soloLectura) {
            // El estudiante solo puede consultar el catálogo
            add(scroll, BorderLayout.CENTER);
            JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            abajo.add(btnRecargar);
            add(abajo, BorderLayout.SOUTH);
        } else {
            add(izquierda, BorderLayout.WEST);
            add(scroll, BorderLayout.CENTER);
        }

        btnLimpiar.addActionListener(e -> limpiar());
        btnAgregar.addActionListener(e -> agregar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnRecargar.addActionListener(e -> cargarDatos());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                mostrarSeleccionado();
            }
        });
    }

    private void cargarCategorias() {
        ejecutar(() -> {
            cmbCategoria.removeAllItems();
            for (Categoria c : controlador.listarCategorias()) {
                cmbCategoria.addItem(c);
            }
        });
    }

    @Override
    protected void cargarDatos() {
        ejecutar(() -> {
            libros = controlador.listar();
            modeloTabla.setRowCount(0);
            for (Libro l : libros) {
                modeloTabla.addRow(new Object[]{
                        l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                        l.getEditorial(), l.getNombreCategoria(), l.getStock()
                });
            }
            ajustarColumnas(tabla);
        });
    }

    private void mostrarSeleccionado() {
        Libro libro = libros.get(tabla.getSelectedRow());
        txtId.setText(String.valueOf(libro.getId()));
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtIsbn.setText(libro.getIsbn());
        txtEditorial.setText(libro.getEditorial());
        txtStock.setText(String.valueOf(libro.getStock()));
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getId() == libro.getIdCategoria()) {
                cmbCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    private void agregar() {
        ejecutar(() -> {
            controlador.crear(txtTitulo.getText(), txtAutor.getText(), txtIsbn.getText(),
                    txtEditorial.getText(), leerEntero(txtStock, "stock"),
                    (Categoria) cmbCategoria.getSelectedItem());
            mensaje("Libro agregado correctamente.");
            cargarDatos();
            limpiar();
        });
    }

    private void actualizar() {
        ejecutar(() -> {
            controlador.actualizar(idSeleccionado(), txtTitulo.getText(), txtAutor.getText(), txtIsbn.getText(),
                    txtEditorial.getText(), leerEntero(txtStock, "stock"),
                    (Categoria) cmbCategoria.getSelectedItem());
            mensaje("Libro actualizado correctamente.");
            cargarDatos();
            limpiar();
        });
    }

    private void eliminar() {
        ejecutar(() -> {
            int id = idSeleccionado();
            if (confirmar("¿Eliminar el libro seleccionado?", "Confirmar eliminación")) {
                controlador.eliminar(id);
                mensaje("Libro eliminado correctamente.");
                cargarDatos();
                limpiar();
            }
        });
    }

    private int idSeleccionado() {
        if (txtId.getText().isBlank()) {
            throw new IllegalArgumentException("Selecciona primero un libro de la tabla.");
        }
        return Integer.parseInt(txtId.getText());
    }

    private void limpiar() {
        txtId.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        txtStock.setText("");
        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        tabla.clearSelection();
    }
}
