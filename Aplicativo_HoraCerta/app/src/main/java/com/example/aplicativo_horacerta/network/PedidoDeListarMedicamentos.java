package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class PedidoDeListarMedicamentos extends ComunicadoJson{

    @SerializedName("idUsuario")
    private String idUsuario;


    public PedidoDeListarMedicamentos(String idUsuario) {
        super("PedidoDeListarMedicamentos");
        this.idUsuario = idUsuario;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

}