package org.example.domain.result;

import com.google.gson.annotations.SerializedName;
import org.example.protocol.ComunicadoJson;

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
}