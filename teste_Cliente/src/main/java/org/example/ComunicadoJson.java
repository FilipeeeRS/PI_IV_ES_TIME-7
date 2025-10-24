package org.example;

import com.google.gson.Gson;

public class ComunicadoJson extends Comunicado {
    private String operacao;

    public ComunicadoJson(String operacao) {
        this.operacao = operacao;
    }

    public String getOperacao() {
        return operacao;
    }

    public String getJson() {
        return new Gson().toJson(this);
    }
    @Override
    public String toString() { return getJson(); }

}
