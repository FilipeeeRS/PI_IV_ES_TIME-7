package com.example.aplicativo_horacerta.network;
import com.google.gson.annotations.SerializedName;

public class ResultadoBuscaIdoso extends ComunicadoJson {
    @SerializedName("encontrou")
    private boolean encontrou;

    @SerializedName("nomeIdoso")
    private String nomeIdoso;

    public ResultadoBuscaIdoso(boolean encontrou, String nomeIdoso) {
        super("ResultadoBuscaIdoso");
        this.encontrou = encontrou;
        this.nomeIdoso = nomeIdoso;
    }

    public boolean isEncontrou() { return encontrou; }
    public String getNomeIdoso() { return nomeIdoso; }
}