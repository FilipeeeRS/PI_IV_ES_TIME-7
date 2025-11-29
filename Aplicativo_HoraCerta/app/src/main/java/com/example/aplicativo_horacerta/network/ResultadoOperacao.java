package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class ResultadoOperacao extends ComunicadoJson {

    @SerializedName("sucesso")
    private boolean sucesso;

    @SerializedName("mensagem")
    private String mensagem;

    public ResultadoOperacao(boolean sucesso, String mensagem) {
        super("ResultadoOperacao");
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    // Getters para o Kotlin
    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }
}