package org.example.protocol;

import org.example.protocol.Comunicado;

import java.util.Objects;

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
    public String toString() {
        return "ComunicadoJson [Tipo: " + tipo + ", Conteúdo JSON: " + json + "]";
    }

    @Override
    public int hashCode() {
        // Combina o hash do pai (Comunicado) com os campos específicos desta classe.
        return Objects.hash(super.hashCode(), tipo, json);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        ComunicadoJson other = (ComunicadoJson) obj;

        return Objects.equals(tipo, other.tipo) &&
                Objects.equals(json, other.json);
    }
}