package com.example.aplicativo_horacerta;

public class Resultado extends Comunicado {
    private static final long serialVersionUID = 1L;

    private final boolean sucesso;
    private final String mensagem;

    public Resultado(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }

    @Override
    public String toString() {
        return "Resultado{" + "sucesso=" + sucesso + ", mensagem='" + mensagem + "'}";
    }
}
