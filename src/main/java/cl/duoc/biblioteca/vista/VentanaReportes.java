package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.EstudianteControlador;
import cl.duoc.biblioteca.controlador.PrestamoControlador;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Prestamo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Map;

/**
 * Muestra los tres reportes. Si se abre para un estudiante, solo muestra
 * su propio historial.
 */
public class VentanaReportes extends VentanaBase {

    private final PrestamoControlador prestamoControlador;
    private final EstudianteControlador estudianteControlador;
    private final Estudiante estudianteFijo;

    private final DefaultTableModel modeloRanking = crearModelo("Libro", "Veces prestado");
    private final DefaultTableModel modeloHistorial =
            crearModelo("Libro", "Fecha préstamo", "Vence", "Fecha devolución", "Estado");
    private final DefaultTableModel modeloEnPrestamo =
            crearModelo("Estudiante", "Libro", "Fecha préstamo", "Vence", "Estado");

    private final JTable tablaRanking = new JTable(modeloRanking);
    private final JTable tablaHistorial = new JTable(modeloHistorial);
    private final JTable tablaEnPrestamo = new JTable(modeloEnPrestamo);

    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();

    public VentanaReportes(PrestamoControlador prestamoControlador, EstudianteControlador estudianteControlador,
                           Estudiante estudianteFijo) {
        super(estudianteFijo == null ? "Reportes" : "Mis préstamos", 1100, 520);
        this.prestamoControlador = prestamoControlador;
        this.estudianteControlador = estudianteControlador;
        this.estudianteFijo = estudianteFijo;
        construirInterfaz();
        cargarDatos();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelHistorial = new JPanel(new BorderLayout(6, 6));
        if (estudianteFijo == null) {
            JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filtro.add(new JLabel("Estudiante:"));
            filtro.add(cmbEstudiante);
            JButton btnVer = new JButton("Ver historial");
            filtro.add(btnVer);
            btnVer.addActionListener(e -> cargarHistorial());
            panelHistorial.add(filtro, BorderLayout.NORTH);
        } else {
            JLabel titulo = new JLabel("Historial de " + estudianteFijo.getResumen());
            titulo.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
            panelHistorial.add(titulo, BorderLayout.NORTH);
        }
        panelHistorial.add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);

        JButton btnRecargar = new JButton("Recargar");
        btnRecargar.addActionListener(e -> cargarDatos());
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        abajo.add(btnRecargar);

        if (estudianteFijo == null) {
            JTabbedPane pestanas = new JTabbedPane();
            pestanas.addTab("Libros más prestados", new JScrollPane(tablaRanking));
            pestanas.addTab("Historial por estudiante", panelHistorial);
            pestanas.addTab("Libros en préstamo", new JScrollPane(tablaEnPrestamo));
            add(pestanas, BorderLayout.CENTER);
        } else {
            add(panelHistorial, BorderLayout.CENTER);
        }
        add(abajo, BorderLayout.SOUTH);
    }

    @Override
    protected void cargarDatos() {
        if (estudianteFijo != null) {
            cargarHistorial();
            return;
        }

        ejecutar(() -> {
            modeloRanking.setRowCount(0);
            for (Map.Entry<String, Integer> fila : prestamoControlador.librosMasPrestados().entrySet()) {
                modeloRanking.addRow(new Object[]{fila.getKey(), fila.getValue()});
            }
            ajustarColumnas(tablaRanking);

            modeloEnPrestamo.setRowCount(0);
            for (Prestamo p : prestamoControlador.listarNoDevueltos()) {
                modeloEnPrestamo.addRow(new Object[]{
                        p.getNombreEstudiante(), p.getTituloLibro(), formatear(p.getFechaPrestamo()),
                        formatear(p.getFechaVencimiento()), p.getEstado()
                });
            }
            ajustarColumnas(tablaEnPrestamo);

            Estudiante seleccionado = (Estudiante) cmbEstudiante.getSelectedItem();
            cmbEstudiante.removeAllItems();
            for (Estudiante e : estudianteControlador.listar()) {
                cmbEstudiante.addItem(e);
                if (seleccionado != null && seleccionado.getId() == e.getId()) {
                    cmbEstudiante.setSelectedItem(e);
                }
            }
        });
        cargarHistorial();
    }

    private void cargarHistorial() {
        Estudiante estudiante = estudianteFijo != null ? estudianteFijo : (Estudiante) cmbEstudiante.getSelectedItem();
        modeloHistorial.setRowCount(0);
        if (estudiante == null) {
            return;
        }
        ejecutar(() -> {
            for (Prestamo p : prestamoControlador.historialDe(estudiante.getId())) {
                modeloHistorial.addRow(new Object[]{
                        p.getTituloLibro(), formatear(p.getFechaPrestamo()), formatear(p.getFechaVencimiento()),
                        formatear(p.getFechaDevolucionReal()), p.getEstado()
                });
            }
            ajustarColumnas(tablaHistorial);
        });
    }

    private static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }
}
