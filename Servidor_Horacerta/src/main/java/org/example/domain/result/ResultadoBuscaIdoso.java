package org.example.domain.result;

import com.google.gson.annotations.SerializedName;
import org.example.protocol.ComunicadoJson;

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
}