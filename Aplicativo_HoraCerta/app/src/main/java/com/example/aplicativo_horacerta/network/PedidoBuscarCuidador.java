package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class PedidoBuscarCuidador extends ComunicadoJson {

    @SerializedName("emailIdoso")
    private String emailIdoso;

    public PedidoBuscarCuidador(String emailIdoso) {
        super("BuscarCuidador");
        this.emailIdoso = emailIdoso;
    }
}