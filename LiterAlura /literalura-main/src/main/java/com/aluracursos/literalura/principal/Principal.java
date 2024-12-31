package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.service.AutorService;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;
import com.aluracursos.literalura.service.LibroService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    public static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private LibroService libroServicio;
    private AutorService autorServicio;

    public Principal(LibroService libroService, AutorService autorService) {
        this.libroServicio = libroService;
        this.autorServicio = autorService;
    }

    public void muestraMenu() {
        var opcion = -1;
        while (opcion != 0) {

            try {
                String menu = """
                --------------
                **Elija la opción a través del número: **
                1.- Buscar libro por título
                2.- Listar libros registrados
                3.- Listar autores registrados
                4.- Listar autores vivos en un determinado año
                5.- Listar libros por idioma
                6.- Estadísticas de libros por número de descargas
                7.- Buscar los 10 libros más descargados
                8.- Buscar autores por nombre
                0.- Finalizar
                -------------""";

                System.out.println(menu);
                opcion = teclado.nextInt();
                teclado.nextLine();

                switch (opcion) {

                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        buscarAutoresVivosPorYear();
                        break;
                    case 5:
                        listarLibrosPorIdioma();
                        break;
                    case 6:
                        estadisticasLibrosPorNumDescargas();
                        break;
                    case 7:
                        buscarTop10LibrosMasDescargados();
                        break;
                    case 8:
                        buscarAutorPorNombre();
                        break;
                    case 0:
                        System.out.println("Cerrando la aplicación...");
                        break;
                    default:
                        System.out.println("Opción inválida. Favor de introducir un número del menú.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Opción inválida. Favor de introducir un número del menú.");
                teclado.nextLine();
            }
        }
    }


    private DatosResultados obtenerDatosResultados(String tituloLibro) {
        var json = consumoAPI.obtenerDatos(URL_BASE+"?search="+tituloLibro.replace(" ", "%20"));
        var datos = conversor.obtenerDatos(json, DatosResultados.class);
        return datos;
    }

    private void buscarLibroPorTitulo() {

        System.out.print("Escribe el título del libro que quieres buscar: ");
        var tituloLibro = teclado.nextLine().toUpperCase();

        Optional<Libro> libroRegistrado = libroServicio.buscarLibroPorTitulo(tituloLibro);

        if (libroRegistrado.isPresent()) {
            System.out.println("El libro ya ha sido buscado.");
        } else {
            var datos = obtenerDatosResultados(tituloLibro);

            if (datos.listaLibros().isEmpty()){
                System.out.println("No se encontró el libro buscado en Gutendex API.");
            } else {
                DatosLibros datosLibros = datos.listaLibros().get(0);
                DatosAutores datosAutores = datosLibros.autores().get(0);
                String lenguaje = datosLibros.lenguajes().get(0);
                //Lenguaje lenguaje = Lenguaje.fromString(lenguaje);

                Libro libro = new Libro(datosLibros);
                libro.setIdiomas(Lenguaje.valueOf(lenguaje));

                Optional<Autor> autorRegistrado = autorServicio.buscarAutorRegistrado(datosAutores.nombre());

                if (autorRegistrado.isPresent()) {
                    System.out.println("El autor ya está registrado.");
                    Autor autorExiste = autorRegistrado.get();
                    libro.setAutor(autorExiste);
                } else {
                    Autor autor = new Autor(datosAutores);
                    autor = autorServicio.guardarAutor(autor);
                    libro.setAutor(autor);
                    autor.getLibros().add(libro);
                }

                try {
                    libroServicio.guardarLibro(libro);
                    System.out.println("\nLibro encontrado.\n");
                    System.out.println(libro+"\n");
                    System.out.println("Libro guardado.\n");
                } catch (DataIntegrityViolationException e){
                    System.out.println("El libro ya está registrado.");
                }
            }
        }

    }


    private void listarLibrosRegistrados() {


        List<Libro> libros = libroServicio.listarLibrosRegistrados();

        if (libros.isEmpty()) {
            System.out.println("No se encontraron libros registrados.");
        } else {
            System.out.println("Los libros registrados son los siguientes:\n");
            libros.stream()
                    .sorted(Comparator.comparing(Libro::getTitulo))
                    .forEach(System.out::println);
        }

    }


    private void listarAutoresRegistrados() {

        List<Autor> autores = autorServicio.listarAutoresRegistrados();

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores.");
        } else {
            System.out.println("Los autores son los siguientes:\n");
            for (Autor autor : autores) {
                List<Libro> librosPorAutorId = libroServicio.buscarPorIdAutor(autor.getId());

                System.out.println("INFORMACION DEL AUTOR");
                System.out.println("Autor: "+autor.getNombre());
                System.out.println("Fecha de Nacimiento: "+autor.getFechaNacimiento());
                System.out.println("Fecha de Fallecido: "+autor.getFechaFallecido());

                if (librosPorAutorId.isEmpty()) {
                    System.out.println("No se encontraron libros para este autor.");
                } else {
                    String librosRegistrados = librosPorAutorId.stream()
                            .map(Libro::getTitulo)
                                    .collect(Collectors.joining(", "));
                    System.out.println("Libros: ["+librosRegistrados+"]");
                    System.out.println("-----------------\n");
                }
            }
        }

    }


    private void buscarAutoresVivosPorYear() {

        System.out.print("Escribe el año del autor: ");
        var yearDelAutor = teclado.nextInt();


        List<Autor> buscarAutoresPorYear = autorServicio.buscarAutoresVivosPorYear(yearDelAutor);


        if (buscarAutoresPorYear.isEmpty()) {
            System.out.println("No se encontraron autores por el año buscado.");
        } else {
            System.out.printf("El autor o los autores vivos del año %d son los siguientes:\n", yearDelAutor);
            for (Autor autoresVivos : buscarAutoresPorYear) {
                List<Libro> librosAutoresVivosPorId = libroServicio.buscarPorIdAutor(autoresVivos.getId());

                System.out.println("INFORMACION DEL AUTOR");
                System.out.println("Autor: "+autoresVivos.getNombre());
                System.out.println("Fecha de Nacimiento: "+autoresVivos.getFechaNacimiento());
                System.out.println("Fecha de Fallecido: "+autoresVivos.getFechaFallecido());

                if (librosAutoresVivosPorId.isEmpty()) {
                    System.out.println("No se encontraron libros para este autor.");
                } else {
                    String librosRegistrados = librosAutoresVivosPorId.stream()
                            .map(Libro::getTitulo)
                            .collect(Collectors.joining(", "));
                    System.out.println("Libros: ["+librosRegistrados+"]");
                    System.out.println("-----------------\n");
                }
            }
        }

    }
    private void listarLibrosPorIdioma() {

        System.out.println("""
                Estos son los idiomas disponibles:
                - es -> Español
                - en -> Inglés
                - fr -> Francés
                - pt -> Portugués
                """);

        System.out.print("Escribe el idioma abreviado para buscar los libros: ");
        var nombreLenguaje = teclado.nextLine();

        try {
            List<Libro> buscarLibrosPorLenguaje = libroServicio.buscarPorLenguajes(Lenguaje.fromString(nombreLenguaje));
            if (buscarLibrosPorLenguaje.isEmpty()) {
                System.out.println("No se encontraron los libros en ese idioma.");
            } else {
                System.out.printf("Los libros en '%s' son:\n", nombreLenguaje);
                buscarLibrosPorLenguaje.forEach(l -> System.out.print(l.toString()));
            }
        } catch (Exception e) {
            System.out.println("La opción no es válida. Escribe un idioma del menú.");
        }

    }

    private void estadisticasLibrosPorNumDescargas() {

        List<Libro> todosLosLibros = libroServicio.listarLibrosRegistrados();

        if (todosLosLibros.isEmpty()){
            System.out.println("No se encontraron libros registrados.");
        } else {
            System.out.println("Estadísticas de los libros:\n");
            DoubleSummaryStatistics est = todosLosLibros.stream()
                    .filter(libro -> libro.getNumeroDescargas() > 0)
                    .collect(Collectors.summarizingDouble(Libro::getNumeroDescargas));
            System.out.println("Cantidad promedio de descargas: " + est.getAverage());
            System.out.println("Cantidad máxima de descargas: "+ est.getMax());
            System.out.println("Cantidad mínima de descargas: " + est.getMin());
        }

    }
    private void buscarTop10LibrosMasDescargados() {
        List<Libro> top10LibrosMasDescargados = libroServicio.buscarTop10MasDescargados();

        if (top10LibrosMasDescargados.isEmpty()) {
            System.out.println("No se encontraron los suficientes libros.");
        } else {
            System.out.println("Los Top 10 libros más descargados:\n");
            top10LibrosMasDescargados.forEach(libro -> System.out.println(libro.toString()));
        }

    }
    private void buscarAutorPorNombre() {

        System.out.print("Escribe el nombre del autor: ");
        var nombreAutor = teclado.nextLine().toUpperCase();
        List<Autor> autorBuscado = autorServicio.buscarAutorPorNombre(nombreAutor);

        if (autorBuscado.isEmpty()) {
            System.out.println("No se encontraron autores.");
        } else {
            System.out.printf("El autor ha sido encontrado: '%s':\n", nombreAutor);
            for (Autor autor : autorBuscado) {
                List<Libro> librosPorAutorId = libroServicio.buscarPorIdAutor(autor.getId());
                System.out.println("INFORMACION DEL AUTOR");
                System.out.println("Autor: " + autor.getNombre());
                System.out.println("Fecha de Nacimiento: " + autor.getFechaNacimiento());
                System.out.println("Fecha de Fallecido: " + autor.getFechaFallecido());

                if (librosPorAutorId.isEmpty()) {
                    System.out.println("No se han encontrado libros para este autor.");
                } else {
                    String librosRegistrados = librosPorAutorId.stream()
                            .map(Libro::getTitulo)
                            .collect(Collectors.joining(", "));
                    System.out.println("Libros: ["+librosRegistrados+"]");
                    System.out.println("-----------------\n");
                }
            }
        }

    }

}