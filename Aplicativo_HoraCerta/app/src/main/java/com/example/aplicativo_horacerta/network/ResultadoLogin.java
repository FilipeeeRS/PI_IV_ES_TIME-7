package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class ResultadoLogin extends ComunicadoJson {
    private static final long serialVersionUID = 1L;


    @SerializedName("mensagem")
    private final String mensagem;


    @SerializedName("resultado")
    private final boolean isSuccessful;


    @SerializedName("usuario")
    private final Usuario usuario;


    public ResultadoLogin(boolean isSuccessful, Usuario usuario, String mensagem) {
        super("ResultadoLogin");
        this.isSuccessful = isSuccessful;
        this.usuario = usuario;
        // Inicializa o novo campo
        this.mensagem = mensagem;
    }


    public boolean isSuccessful() { return isSuccessful; }


    public String getFirebaseUid() { return usuario.getUid(); }
    public String getProfileType() { return usuario.getTipo(); }


    public String getMensagem() {
        return mensagem;
    }
}