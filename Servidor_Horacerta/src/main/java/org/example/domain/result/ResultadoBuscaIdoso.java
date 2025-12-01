package org.example.domain.result;

import com.google.gson.annotations.SerializedName;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;


public class ResultadoBuscaIdoso extends ComunicadoJson {

    @SerializedName("encontrou")
    private boolean encontrou;

    @SerializedName("nomeIdoso")
    private String nomeIdoso;

    public ResultadoBuscaIdoso(boolean encontrou, String nomeIdoso) {
        super("ResultadoBuscaIdoso");
        this.encontrou = encontrou;
        this.nomeIdoso = nomeIdoso;
    }

    // Construtor de copia
    public ResultadoBuscaIdoso(ResultadoBuscaIdoso outro) {
        // 1. Chama o construtor do pai (ComunicadoJson)
        super(outro.getTipo());

        // 2. Copia todos os campos
        this.encontrou = outro.encontrou;
        this.nomeIdoso = outro.nomeIdoso;
    }

    @Override
    public int hashCode() {
        // Combina o hash do pai com os hash dos dois campos.
        return Objects.hash(super.hashCode(), encontrou, nomeIdoso);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Compara o pai primeiro
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        ResultadoBuscaIdoso other = (ResultadoBuscaIdoso) obj;

        // Compara os dois campos.
        return encontrou == other.encontrou &&
                Objects.equals(nomeIdoso, other.nomeIdoso);
    }
    @Override
    public String toString() {
        if (encontrou) {
            return "ResultadoBuscaIdoso [Status: ENCONTRADO, Nome: " + nomeIdoso + "]";
        } else {
            return "ResultadoBuscaIdoso [Status: NÃO ENCONTRADO]";
        }
    }
}