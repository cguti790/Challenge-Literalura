package com.aluracursos.literalura.model;

public enum Lenguaje {
    //inglés
    en("en"),
    //español
    es("es"),
    //francés
    fr("fr"),
    //húngaro
    hu("hu"),
    //finés
    fi("fi"),
    //portugués
    pt("pt");

    private String lenguajesGutendex;

    Lenguaje(String idiomasGutendex) {
        this.lenguajesGutendex = idiomasGutendex;
    }

    public static Lenguaje fromString (String text) {
        for (Lenguaje lenguaje : Lenguaje.values()) {
            if (lenguaje.lenguajesGutendex.equalsIgnoreCase(text)) {
                return lenguaje;
            }
        }
        throw new IllegalArgumentException("No se encontró ningún lenguaje: " + text);
    }
}
