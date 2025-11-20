package com.example.aplicativo_horacerta.network;
import com.google.gson.annotations.SerializedName;

public class PedidoBuscarIdoso extends ComunicadoJson {
    @SerializedName("email")
    private String email;

    public PedidoBuscarIdoso(String email) {
        super("BuscarIdoso");
        this.email = email;
    }
}