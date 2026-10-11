package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.EstudianteControlador;
import cl.duoc.biblioteca.controlador.LibroControlador;
import cl.duoc.biblioteca.controlador.PrestamoControlador;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VentanaPrestamos extends VentanaBase {

    private final PrestamoControlador prestamoControlador;
    private final EstudianteControlador estudianteControlador;
    private final LibroControlador libroControlador;
    private List<Prestamo> prestamos;

    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();
    private final JComboBox<Libro> cmbLibro = new JComboBox<>();
    private final JButton btnPrestar = new JButton("Registrar préstamo");
    private final JButton btnPruebaSimultanea = new JButton("Probar préstamos simultáneos");
    private final JLabel lblEstado = new JLabel(" ");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Estudiante", "Libro", "Fecha préstamo", "Vence", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public VentanaPrestamos(PrestamoControlador prestamoControlador, EstudianteControlador estudianteControlador,
                            LibroControlador libroControlador) {
        super("Préstamos y devoluciones", 1250, 560);
        this.prestamoControlador = prestamoControlador;
        this.estudianteControlador = estudianteControlador;
        this.libroControlador = libroControlador;
        construirInterfaz();
        cargarDatos();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridLayout(4, 1, 6, 6));
        formulario.setBorder(BorderFactory.createTitledBorder("Nuevo préstamo (" + Prestamo.DIAS_PRESTAMO + " días)"));
        formulario.add(new JLabel("Estudiante:"));
        formulario.add(cmbEstudiante);
        formulario.add(new JLabel("Libro:"));
        formulario.add(cmbLibro);

        JPanel acciones = new JPanel(new GridLayout(2, 1, 6, 6));
        acciones.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        acciones.add(btnPrestar);
        acciones.add(btnPruebaSimultanea);

        // Se agrupa todo arriba para que los botones mantengan su tamaño normal
        JPanel superior = new JPanel(new BorderLayout(6, 10));
        superior.add(formulario, BorderLayout.NORTH);
        superior.add(acciones, BorderLayout.CENTER);
        superior.add(lblEstado, BorderLayout.SOUTH);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        izquierda.add(superior, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Libros actualmente en préstamo"));

        JButton btnDevolver = new JButton("Registrar devolución");
        JButton btnRecargar = new JButton("Recargar");
        JPanel accionesTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        accionesTabla.add(btnDevolver);
        accionesTabla.add(btnRecargar);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(scroll, BorderLayout.CENTER);
        derecha.add(accionesTabla, BorderLayout.SOUTH);

        add(izquierda, BorderLayout.WEST);
        add(derecha, BorderLayout.CENTER);

        btnPrestar.addActionListener(e -> prestar());
        btnPruebaSimultanea.addActionListener(e -> probarPrestamosSimultaneos());
        btnDevolver.addActionListener(e -> devolver());
        btnRecargar.addActionListener(e -> cargarDatos());
    }

    @Override
    protected void cargarDatos() {
        ejecutar(() -> {
            cmbEstudiante.removeAllItems();
            for (Estudiante est : estudianteControlador.listar()) {
                cmbEstudiante.addItem(est);
            }

            cmbLibro.removeAllItems();
            for (Libro libro : libroControlador.listar()) {
                cmbLibro.addItem(libro);
            }

            prestamos = prestamoControlador.listarNoDevueltos();
            modeloTabla.setRowCount(0);
            for (Prestamo p : prestamos) {
                modeloTabla.addRow(new Object[]{
                        p.getId(), p.getNombreEstudiante(), p.getTituloLibro(),
                        formatear(p.getFechaPrestamo()), formatear(p.getFechaVencimiento()), p.getEstado()
                });
            }
            ajustarColumnas(tabla);
        });
    }

    private void prestar() {
        Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        if (estudiante == null || libro == null) {
            error("Selecciona un estudiante y un libro.");
            return;
        }

        btnPrestar.setEnabled(false);
        lblEstado.setText("Registrando préstamo...");

        // El préstamo se procesa en otro hilo. Cuando termina, la ventana se
        // actualiza con invokeLater porque Swing solo se modifica desde su propio hilo.
        prestamoControlador.prestarEnSegundoPlano(estudiante.getId(), libro.getId(),
                new PrestamoControlador.ResultadoPrestamo() {
                    @Override
                    public void exito(Prestamo prestamo) {
                        SwingUtilities.invokeLater(() -> {
                            btnPrestar.setEnabled(true);
                            lblEstado.setText("Último préstamo: " + prestamo.getTituloLibro());
                            mensaje("Préstamo registrado.\nFecha de devolución: "
                                    + formatear(prestamo.getFechaVencimiento()));
                            cargarDatos();
                        });
                    }

                    @Override
                    public void error(String mensaje) {
                        SwingUtilities.invokeLater(() -> {
                            btnPrestar.setEnabled(true);
                            lblEstado.setText(" ");
                            VentanaPrestamos.this.error(mensaje);
                        });
                    }
                });
    }

    /**
     * Envía al mismo tiempo más préstamos del libro seleccionado que ejemplares
     * disponibles. Sirve para comprobar que la sincronización evita que el
     * stock quede negativo.
     */
    private void probarPrestamosSimultaneos() {
        Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        if (estudiante == null || libro == null) {
            error("Selecciona un estudiante y un libro.");
            return;
        }

        int stockInicial = libro.getStock();
        int cantidad = stockInicial + 2;

        boolean continuar = confirmar("Se enviarán " + cantidad + " préstamos simultáneos de \"" + libro.getTitulo()
                        + "\" (stock actual: " + stockInicial + ").\n"
                        + "Los préstamos registrados quedarán en la base de datos. ¿Continuar?",
                "Prueba de préstamos simultáneos");
        if (!continuar) {
            return;
        }

        btnPrestar.setEnabled(false);
        btnPruebaSimultanea.setEnabled(false);
        lblEstado.setText("Procesando " + cantidad + " préstamos...");

        AtomicInteger registrados = new AtomicInteger();
        AtomicInteger rechazados = new AtomicInteger();
        AtomicInteger pendientes = new AtomicInteger(cantidad);

        for (int i = 0; i < cantidad; i++) {
            prestamoControlador.prestarEnSegundoPlano(estudiante.getId(), libro.getId(),
                    new PrestamoControlador.ResultadoPrestamo() {
                        @Override
                        public void exito(Prestamo prestamo) {
                            registrados.incrementAndGet();
                            terminarPrueba();
                        }

                        @Override
                        public void error(String mensaje) {
                            rechazados.incrementAndGet();
                            terminarPrueba();
                        }

                        // Cuando responde el último hilo se muestra el resumen
                        private void terminarPrueba() {
                            if (pendientes.decrementAndGet() == 0) {
                                SwingUtilities.invokeLater(() -> {
                                    btnPrestar.setEnabled(true);
                                    btnPruebaSimultanea.setEnabled(true);
                                    lblEstado.setText(" ");
                                    cargarDatos();
                                    mensaje("Préstamos enviados: " + cantidad
                                            + "\nStock inicial: " + stockInicial
                                            + "\nRegistrados: " + registrados.get()
                                            + "\nRechazados por falta de stock: " + rechazados.get()
                                            + "\nStock final: " + (stockInicial - registrados.get()));
                                });
                            }
                        }
                    });
        }
    }

    private void devolver() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            error("Selecciona un préstamo de la tabla.");
            return;
        }
        Prestamo seleccionado = prestamos.get(fila);

        if (!confirmar("¿Registrar la devolución de \"" + seleccionado.getTituloLibro() + "\"?",
                "Confirmar devolución")) {
            return;
        }

        ejecutar(() -> {
            Prestamo devuelto = prestamoControlador.registrarDevolucion(seleccionado.getId());
            long atraso = devuelto.getDiasAtraso();
            if (atraso > 0) {
                mensaje("Devolución registrada con " + atraso + " día(s) de atraso.\n"
                        + "El atraso queda registrado en el historial del estudiante.");
            } else {
                mensaje("Devolución registrada dentro del plazo.");
            }
            cargarDatos();
        });
    }
}
