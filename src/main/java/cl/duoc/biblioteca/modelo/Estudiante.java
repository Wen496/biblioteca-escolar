package cl.duoc.biblioteca.modelo;

/**
 * Estudiante que puede pedir libros en préstamo.
 */
public class Estudiante extends Persona {

    private String curso;

    public Estudiante() {
    }

    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    public Estudiante(String nombre, String rut, String curso, String correo) {
        this(0, nombre, rut, curso, correo);
    }

    @Override
    public String getTipo() {
        return "Estudiante de " + curso;
    }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
}
