package com.iudigital.emergencias.model;

public enum NivelGravedad {
    CRITICO(4), GRAVE(3), MODERADO(2), LEVE(1);

    private final int peso;
    NivelGravedad(int peso) { this.peso = peso; }
    public int getPeso() { return peso; }
}