# Sistema de Gestión de Biblioteca Escolar

Aplicación de escritorio en Java para administrar la biblioteca de un colegio: libros, estudiantes, usuarios, préstamos, devoluciones y reportes. Es la Evaluación Final Transversal de Desarrollo Orientado a Objetos II (Duoc UC).

## Tecnologías

- Java 17
- Swing (JFrame) para la interfaz gráfica
- MySQL con JDBC (MySQL Connector/J)
- Maven
- IntelliJ IDEA

## Estructura del proyecto

El código está en `src/main/java/cl/duoc/biblioteca` y se divide en paquetes según el patrón MVC y DAO:

- `modelo`: clases Persona (abstracta), Usuario, Estudiante, Libro, Categoria y Prestamo.
- `dao`: interfaces con las operaciones de cada entidad.
- `dao.impl`: implementaciones de los DAO con JDBC y PreparedStatement.
- `controlador`: validan los datos y coordinan la vista con los DAO.
- `vista`: ventanas de la aplicación.
- `util`: clase DatabaseConnection.

Los scripts de la base de datos están en la carpeta `sql`.

## Cómo ejecutarlo

1. En MySQL Workbench, ejecutar primero `PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql` y después `PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql`. Ambos están en la carpeta `sql` y crean la base `biblioteca`.
2. Revisar el usuario y la contraseña de MySQL en `util/DatabaseConnection.java` y cambiarlos si es necesario.
3. Abrir la carpeta del proyecto en IntelliJ como proyecto Maven y esperar que descargue el conector.
4. Ejecutar la clase `Main`.

También se puede ejecutar el archivo `BibliotecaEscolar.jar` con doble clic o desde la terminal:

```
java -jar BibliotecaEscolar.jar
```

Para generar el .jar de nuevo se usa `mvn clean package`. El archivo queda en la carpeta `target`.

## Usuarios de prueba

Vienen en el script de poblado. Todos tienen la contraseña `clave123`.

- Bibliotecario: antonia@correo.cl
- Estudiante: carlos@correo.cl (también maria@correo.cl, ignacio@correo.cl y otros)

## Funcionalidades

**Inicio de sesión.** Se ingresa con correo y contraseña. El sistema habilita las opciones según el rol:

- Bibliotecario: acceso a todas las funciones.
- Estudiante: puede ver el catálogo de libros y su propio historial de préstamos.

**Gestión de libros, estudiantes y usuarios.** Permite agregar, modificar, eliminar y listar registros. No se puede eliminar un libro o un estudiante que tenga préstamos registrados.

**Préstamos y devoluciones.** Al registrar un préstamo se revisa que haya stock, se guarda el préstamo y se descuenta un ejemplar. El plazo es de 7 días, igual que en los datos de ejemplo, y la fecha de vencimiento se calcula automáticamente y se guarda en `fecha_devolucion`. Al registrar la devolución se reemplaza por la fecha real; si es posterior al vencimiento, el sistema informa los días de atraso y estos quedan visibles en el historial del estudiante.

El botón "Probar préstamos simultáneos" envía al mismo tiempo más préstamos de un libro que ejemplares disponibles. Al final muestra cuántos se registraron y cuántos se rechazaron, y así se comprueba que el stock nunca queda negativo.

**Reportes.** Libros más prestados, historial por estudiante y libros que están actualmente en préstamo.

## Decisiones de diseño

- **MVC:** las vistas solo muestran datos y capturan eventos, los controladores validan y los modelos representan la información. Ninguna vista ni controlador contiene SQL.
- **DAO:** todo el acceso a la base de datos está en las clases `dao.impl`. Si cambiara el motor de base de datos, solo habría que modificar esa capa.
- **Singleton:** `DatabaseConnection` tiene una sola instancia, que se obtiene con `getInstance()`. Si la conexión se pierde, se vuelve a abrir.
- **Herencia y polimorfismo:** Usuario y Estudiante heredan de la clase abstracta Persona y cada una implementa `getTipo()` a su manera. Las ventanas de gestión heredan de la clase abstracta VentanaBase.
- **Hilos:** el préstamo se procesa en segundo plano con un ExecutorService, así la ventana no se congela mientras se guarda. Al terminar, la interfaz se actualiza con `SwingUtilities.invokeLater`.
- **Sincronización:** los métodos que modifican el stock (`registrarPrestamo` y `registrarDevolucion`) son `synchronized`. Si dos préstamos del mismo libro llegan al mismo tiempo, el segundo espera a que termine el primero, y el stock nunca queda negativo.

## Autora

Macarena Álvarez Sáez
