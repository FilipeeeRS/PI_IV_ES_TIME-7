package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

public class PedidoDeConfirmarAlarme extends ComunicadoJson {

    @SerializedName("idUsuario")
    private String idUsuario;

    @SerializedName("nomeRemedio")
    private String nomeRemedio;

    @SerializedName("dia")
    private String dia;

    @SerializedName("horario")
    private String horario;

    public PedidoDeConfirmarAlarme(String idUsuario, String nomeRemedio, String dia, String horario) {
        super("ConfirmarAlarme");
        this.idUsuario = idUsuario;
        this.nomeRemedio = nomeRemedio;
        this.dia = dia;
        this.horario = horario;
    }

    // Getters e Setters
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getNomeRemedio() { return nomeRemedio; }
    public void setNomeRemedio(String nomeRemedio) { this.nomeRemedio = nomeRemedio; }
    public String getDia() { return dia; }
    public void setDia(String dia) { this.dia = dia; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
}