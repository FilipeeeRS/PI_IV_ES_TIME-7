package org.example.protocol;

import org.example.protocol.Comunicado;

public class ComunicadoJson extends Comunicado {


    private String tipo;


    private String json;


    public ComunicadoJson() {
        tipo = "ComunicadoJson";
    }


    public ComunicadoJson(String json) {
        this();
        this.json = json;
    }

    // Getters...
    public String getTipo() {
        return this.tipo;
    }

    public String getJson() {
        return this.json;
    }

    @Override
    public String toString() { return getJson(); }
}