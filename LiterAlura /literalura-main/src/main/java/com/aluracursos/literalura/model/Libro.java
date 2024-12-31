package com.aluracursos.literalura.model;

import jakarta.persistence.*;

import java.util.OptionalDouble;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String titulo;
    @Enumerated(EnumType.STRING)
    private Lenguaje idiomas;
    private Double numeroDescargas;

    @ManyToOne
    private Autor autores;

    public Libro() {}

    public Libro(DatosLibros datosLibros) {
        this.titulo = datosLibros.titulo();
        this.autores = new Autor(datosLibros.autores().get(0));
        this.idiomas = Lenguaje.fromString(datosLibros.lenguajes().get(0));
        this.numeroDescargas = OptionalDouble.of(datosLibros.numeroDescargas()).orElse(0);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Autor getAutor() {
        return autores;
    }

    public void setAutor(Autor autor) {
        this.autores = autor;
    }

    public Lenguaje getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(Lenguaje idiomas) {
        this.idiomas = idiomas;
    }

    public Double getNumeroDescargas() {
        return numeroDescargas;
    }

    public void setNumeroDescargas(Double numeroDescargas) {
        this.numeroDescargas = numeroDescargas;
    }

    @Override
    public String toString() {
        return "----- LIBRO -----" + '\n' +
                "Título: " + titulo + '\n' +
                "Autores: " + autores.getNombre() + '\n' +
                "Idiomas: " + idiomas + '\n' +
                "Número de Descargas: " + numeroDescargas + "\n" +
                "-----------------" + '\n';
    }

}
