package org.example;

public class ComunicadoJson extends Comunicado
{
    private String json;

    public ComunicadoJson(String json)
    {
        this.json = json;
    }

    public String getJson()
    {
        return this.json;
    }
    @Override
    public String toString() { return getJson(); }
}