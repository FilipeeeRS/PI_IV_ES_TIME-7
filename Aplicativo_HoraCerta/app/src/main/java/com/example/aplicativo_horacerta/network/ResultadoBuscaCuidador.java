package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class ResultadoBuscaCuidador extends ComunicadoJson {

    @SerializedName("nomeCuidador")
    private String nomeCuidador;

    @SerializedName("encontrou")
    private boolean encontrou;

    public ResultadoBuscaCuidador(boolean encontrou, String nomeCuidador) {
        super("ResultadoBuscaCuidador");
        this.encontrou = encontrou;
        this.nomeCuidador = nomeCuidador;
    }

    public String getNomeCuidador() { return nomeCuidador; }
    public boolean isEncontrou() { return encontrou; }
}