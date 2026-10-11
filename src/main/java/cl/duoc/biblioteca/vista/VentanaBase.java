package cl.duoc.biblioteca.vista;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Comportamiento común de las ventanas de gestión: mensajes, manejo de
 * errores y carga de datos. Cada ventana define cómo carga sus propios datos.
 */
public abstract class VentanaBase extends JFrame {

    protected static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    protected VentanaBase(String titulo, int ancho, int alto) {
        setTitle(titulo);
        setSize(ancho, alto);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Carga o recarga la información que muestra la ventana.
     */
    protected abstract void cargarDatos();

    /**
     * Ejecuta una acción y muestra un mensaje claro si algo falla, para que
     * un error no cierre la aplicación.
     */
    protected void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            error(e.getMessage());
        } catch (SQLException e) {
            error("Error de base de datos:\n" + e.getMessage());
        }
    }

    protected int leerEntero(JTextField campo, String nombreCampo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " es obligatorio.");
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser un número entero.");
        }
    }

    protected String formatear(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    protected void mensaje(String texto) {
        JOptionPane.showMessageDialog(this, formatoMensaje(texto), "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void error(String texto) {
        JOptionPane.showMessageDialog(this, formatoMensaje(texto), "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected boolean confirmar(String texto, String titulo) {
        int opcion = JOptionPane.showConfirmDialog(this, formatoMensaje(texto), titulo, JOptionPane.YES_NO_OPTION);
        return opcion == JOptionPane.YES_OPTION;
    }

    /**
     * Prepara el texto de los cuadros de diálogo. Con HTML el texto se ajusta
     * en varias líneas y no aparece cortado con "..." en pantallas con escalado.
     */
    public static String formatoMensaje(String texto) {
        String limpio = texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><body style='width: 300px'>" + limpio + "</body></html>";
    }

    /**
     * Ajusta el ancho de cada columna según su contenido, para que los textos
     * de la tabla no queden cortados.
     */
    protected void ajustarColumnas(JTable tabla) {
        for (int col = 0; col < tabla.getColumnCount(); col++) {
            TableColumn columna = tabla.getColumnModel().getColumn(col);
            TableCellRenderer encabezado = tabla.getTableHeader().getDefaultRenderer();
            int ancho = encabezado.getTableCellRendererComponent(tabla, columna.getHeaderValue(), false, false, 0, col)
                    .getPreferredSize().width;

            for (int fila = 0; fila < tabla.getRowCount(); fila++) {
                TableCellRenderer renderer = tabla.getCellRenderer(fila, col);
                Component celda = tabla.prepareRenderer(renderer, fila, col);
                ancho = Math.max(ancho, celda.getPreferredSize().width);
            }
            columna.setPreferredWidth(ancho + 24);
        }
    }

    @FunctionalInterface
    protected interface Accion {
        void ejecutar() throws SQLException;
    }
}
