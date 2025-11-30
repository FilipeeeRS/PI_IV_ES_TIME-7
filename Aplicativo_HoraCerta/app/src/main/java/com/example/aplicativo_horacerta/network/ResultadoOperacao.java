package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class ResultadoOperacao extends ComunicadoJson {

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("sucesso")
    private boolean sucesso;

    @SerializedName("mensagem")
    private String mensagem;

    public ResultadoOperacao(String tipo, boolean sucesso, String mensagem) {
        super(tipo);
        this.tipo = tipo;
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public String getTipo() { return tipo; }
}