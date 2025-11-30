package org.example.domain.result;

import java.io.Serializable;

public class Resultado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean sucesso;
    private final String mensagem;

    public Resultado(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }
    public boolean isSucesso() { return sucesso; }
    public String getMensagem(){ return mensagem; }
}
