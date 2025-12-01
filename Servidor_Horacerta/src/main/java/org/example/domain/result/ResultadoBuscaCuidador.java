package org.example.domain.result;

import com.google.gson.annotations.SerializedName;
import org.example.protocol.ComunicadoJson;
import java.util.Objects;

public class ResultadoBuscaCuidador extends ComunicadoJson {

    @SerializedName("nomeCuidador")
    private String nomeCuidador;

    @SerializedName("uidCuidador")
    private String uidCuidador;
    @SerializedName("encontrou")
    private boolean encontrou;

    public ResultadoBuscaCuidador(boolean encontrou, String uidCuidador, String nomeCuidador) {
        super("ResultadoBuscaCuidador");
        this.encontrou = encontrou;
        this.uidCuidador = uidCuidador;
        this.nomeCuidador = nomeCuidador;
    }

    // Construtor de copia
    public ResultadoBuscaCuidador(ResultadoBuscaCuidador outro) {
        // 1. Chama o construtor do pai (ComunicadoJson)
        super(outro.getTipo());

        // 2. Copia todos os campos
        this.encontrou = outro.encontrou;
        this.uidCuidador = outro.uidCuidador;
        this.nomeCuidador = outro.nomeCuidador;
    }

    @Override
    public int hashCode() {
        // Combina o hash do pai com os hash dos três campos.
        return Objects.hash(super.hashCode(), encontrou, uidCuidador, nomeCuidador);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Compara o pai primeiro
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        ResultadoBuscaCuidador other = (ResultadoBuscaCuidador) obj;

        // Compara os três campos.
        return encontrou == other.encontrou &&
                Objects.equals(uidCuidador, other.uidCuidador) &&
                Objects.equals(nomeCuidador, other.nomeCuidador);
    }

    @Override
    public String toString() {
        if (encontrou) {
            return "ResultadoBuscaCuidador [Status: ENCONTRADO, Nome: " + nomeCuidador + ", UID: " + uidCuidador + "]";
        } else {
            return "ResultadoBuscaCuidador [Status: NÃO ENCONTRADO]";
        }
    }
}