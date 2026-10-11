package cl.duoc.biblioteca.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Préstamo de un libro a un estudiante.
 * Al prestar, fechaDevolucion guarda la fecha en que se debe devolver (7 días
 * después, igual que en los datos de ejemplo). Al devolver se reemplaza por la
 * fecha real, y así el atraso queda registrado en la base de datos.
 */
public class Prestamo {

    public static final int DIAS_PRESTAMO = 7;

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean devuelto;

    // Datos para mostrar en las tablas
    private String nombreEstudiante;
    private String tituloLibro;

    public Prestamo() {
    }

    public Prestamo(int idEstudiante, int idLibro, LocalDate fechaPrestamo) {
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = getFechaVencimiento();
        this.devuelto = false;
    }

    /**
     * Fecha en que realmente se devolvió el libro, o null si sigue prestado.
     */
    public LocalDate getFechaDevolucionReal() {
        return devuelto ? fechaDevolucion : null;
    }

    public LocalDate getFechaVencimiento() {
        return fechaPrestamo.plusDays(DIAS_PRESTAMO);
    }

    /**
     * Días de atraso. Si el libro ya se devolvió se compara la fecha de
     * devolución con el vencimiento; si no, se compara con la fecha de hoy.
     */
    public long getDiasAtraso() {
        LocalDate referencia = devuelto && fechaDevolucion != null ? fechaDevolucion : LocalDate.now();
        long dias = ChronoUnit.DAYS.between(getFechaVencimiento(), referencia);
        return Math.max(dias, 0);
    }

    public String getEstado() {
        long atraso = getDiasAtraso();
        if (devuelto) {
            return atraso > 0 ? "Devuelto con " + atraso + " día(s) de atraso" : "Devuelto a tiempo";
        }
        return atraso > 0 ? "Atrasado " + atraso + " día(s)" : "En préstamo";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }
    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }
    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }
    public String getTituloLibro() { return tituloLibro; }
    public void setTituloLibro(String tituloLibro) { this.tituloLibro = tituloLibro; }
}
