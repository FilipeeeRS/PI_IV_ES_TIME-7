package org.example;

import com.google.gson.Gson;

public class ResultadoLogin extends ResultadoOperacao{
    private Usuario usuario;

    public ResultadoLogin(boolean resultado, String operacao, Usuario usuario) {
        super(resultado, operacao); // inicializa os campos do pai
        this.usuario = usuario;
    }

    public String getResultado() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
