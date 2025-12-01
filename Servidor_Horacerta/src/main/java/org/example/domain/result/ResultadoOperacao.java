package org.example.domain.result;


import org.example.protocol.ComunicadoJson;
import java.util.Objects;
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

    //Construtor de copia
    public ResultadoOperacao(ResultadoOperacao outro) {
        // Chama o construtor do pai (ComunicadoJson)
        super(outro.getTipo());

        // Copia todos os campos
        this.resultado = outro.resultado;
        this.operacao = outro.operacao;
        this.sucesso = outro.sucesso;
        this.mensagem = outro.mensagem;
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

    @Override
    public String toString ()
    {
        // toString claro para logs, mostrando a migração de campos.
        return "ResultadoOperacao {" +
                "Sucesso: "+ this.sucesso +
                ", Mensagem: '" + this.mensagem + '\'' +
                " | Resultado Antigo: " + this.resultado +
                ", Operacao Antiga: '" + this.operacao + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        // Usa o hash do pai (ComunicadoJson) e os campos chaves desta classe.
        return Objects.hash(super.hashCode(), sucesso, mensagem);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        ResultadoOperacao other = (ResultadoOperacao) obj;

        return sucesso == other.sucesso &&
                Objects.equals(mensagem, other.mensagem);
    }


}
