package com.example.aplicativo_horacerta.network;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PedidoDeCriarMedicamento extends ComunicadoJson {
    private static final long serialVersionUID = 1L;

    @SerializedName("nome")
    private String nome;
    @SerializedName("dia")
    private String dia;
    @SerializedName("horario")
    private String horario;
    @SerializedName("descricao")
    private String descricao;
    @SerializedName("idUsuario")
    private String idUsuario; // UID do Cuidador

    // Construtor usado para criar o objeto antes de enviar
    public PedidoDeCriarMedicamento(String nome, String dia, String horario, String descricao, String idUsuario) {
        super("PedidoDeCriarMedicamento");
        if (nome == null || nome.isBlank())  throw new IllegalArgumentException("Nome inválido");
        if (dia == null || dia.isBlank())throw new IllegalArgumentException("Dia inválido");
        if ( horario == null || horario.isBlank())throw new IllegalArgumentException("Horario inválida");
        if (descricao == null || descricao.isBlank())throw new IllegalArgumentException("Descricao inválido");
        if (idUsuario == null || idUsuario.isBlank())throw new IllegalArgumentException("ide inválido");

        this.nome = nome;
        this.dia = dia;
        this.horario = horario;
        this.descricao = descricao;
        this.idUsuario = idUsuario;

    }
    public String getNome()   { return nome; }
    public String getDia()  { return dia; }
    public String getHorario()  { return horario; }
    public String getDescricao()  { return descricao; }
    public String getIdUsuario() {return idUsuario; }
}