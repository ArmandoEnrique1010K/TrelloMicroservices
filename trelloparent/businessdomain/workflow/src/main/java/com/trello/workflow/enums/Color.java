package com.trello.workflow.enums;

// Colores tomados del proyecto de Marcadores:
// https://github.com/ArmandoEnrique1010K/BookmarkApp/blob/main/server/src/configs/seedColors.ts
public enum Color {
    // No se puede introducir el caracter "#" al inicio si se va a utilizar el
    // codigo hexadecimal

    // Rojo
    // FFC9C9,

    // En este caso se utiliza 2 valores para representar un color
    // Nombre y valor en codigo hexadecimal
    RED("Rojo", "#FFC9C9"),
    ORANGE("Naranja", "#FFD6A7"),
    YELLOW("Amarillo", "#FFF085"),
    GREEN("Verde", "#D8F999"),
    EMERALD("Esmeralda", "#A4F4CF"),
    AQUAMARINE("Aguamarina", "#96F7E4"),
    SKY("Celeste", "#A2F4FD"),
    BLUE("Azul", "#B8E6FE"),
    VIOLET("Violeta", "#C6D2FF"),
    LILAC("Lila", "#DDD6FF"),
    PINK("Rosa", "#FCCEE8"),
    GRAY("Gris", "#E2E8F0");

    private final String name;
    private final String hex;

    Color(String name, String hex) {
        this.name = name;
        this.hex = hex;
    }

    public String getName() {
        return name;
    }

    public String getHex() {
        return hex;
    }

}
