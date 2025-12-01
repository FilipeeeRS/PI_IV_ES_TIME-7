package org.example.domain.result;


import org.example.protocol.ComunicadoJson;

import java.io.Serializable;

public class ResultadoOperacao extends ComunicadoJson implements Serializable  {

    // CAMPOS NECESSÁRIOS PARA LOGIN/OUTRAS
    private boolean resultado;
    private String operacao;

    // CAMPOS NECESSÁRIOS PARA DELETE/OPERAÇÕES
    private boolean sucesso;
    private String mensagem;

    public ResultadoOperacao() {
        super();
    }


    // Construtor Novo (Para o Delete e futuras operações)
    public ResultadoOperacao (boolean sucesso, String mensagem)
    {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        // Inicializa os campos antigos com os novos valores
        this.resultado = sucesso;
        this.operacao = mensagem; // Ou uma string de tipo de sucesso/falha
    }

    // Getters para todos os campos
    public boolean getResultadoOperacao(){
        return resultado;
    }

    public String getOperacao(){
        return operacao;
    }

    public boolean getSucesso(){
        return sucesso;
    }

    public String getMensagem(){
        return mensagem;
    }

    public String toString ()
    {
        return "{ Sucesso: "+this.sucesso + "Mensagem: " +this.mensagem + " | Resultado Antigo: " + this.resultado + " Operacao Antiga: " + this.operacao + " }";
    }
}
