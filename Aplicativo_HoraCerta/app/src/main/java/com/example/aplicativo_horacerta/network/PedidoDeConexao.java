package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class PedidoDeConexao extends ComunicadoJson {

    @SerializedName("emailCuidador")
    private String emailCuidador;

    @SerializedName("emailIdoso")
    private String emailIdoso;

    public PedidoDeConexao(String emailCuidador, String emailIdoso) {
        super("ConectarIdoso"); // Essa tag deve bater com o 'case' do switch no Servidor
        this.emailCuidador = emailCuidador;
        this.emailIdoso = emailIdoso;
    }
}