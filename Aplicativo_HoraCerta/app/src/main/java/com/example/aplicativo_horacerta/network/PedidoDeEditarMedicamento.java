package com.example.aplicativo_horacerta.network;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PedidoDeEditarMedicamento extends ComunicadoJson {
    private static final long serialVersionUID = 1L;

    @SerializedName("idMedicamento") // ID único do medicamento a ser editado
    private String idMedicamento;
    @SerializedName("nome")
    private String nome;
    @SerializedName("dia")
    private String dia;
    @SerializedName("horario")
    private String horario;
    @SerializedName("descricao")
    private String descricao;
    @SerializedName("idUsuario")
    private String idUsuario; // UID do Cuidador (mantido, pois é importante para contexto/autorização)

    // Construtor usado para criar o objeto antes de enviar
    public PedidoDeEditarMedicamento(String idMedicamento, String nome, String dia, String horario, String descricao, String idUsuario) {
        super("PedidoDeEditarMedicamento");

        // ** VALIDAÇÃO ESSENCIAL (Manter): ID e Usuário são necessários para identificar o que editar e quem está editando **
        if (idMedicamento == null || idMedicamento.isBlank()) throw new IllegalArgumentException("ID do Medicamento inválido");
        if (idUsuario == null || idUsuario.isBlank()) throw new IllegalArgumentException("ID do Usuário inválido");

        // ** VALIDAÇÃO FLEXIBILIZADA (Remover/Comentar): Permite que campos de edição sejam nulos/vazios **
        // if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome inválido");
        // if (dia == null || dia.isBlank()) throw new IllegalArgumentException("Dia inválido");
        // if (horario == null || horario.isBlank()) throw new IllegalArgumentException("Horario inválido");
        // if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("Descricao inválida");

        this.idMedicamento = idMedicamento;
        this.nome = nome;
        this.dia = dia;
        this.horario = horario;
        this.descricao = descricao;
        this.idUsuario = idUsuario;
    }

    // Getters
    public String getIdMedicamento() { return idMedicamento; }
    public String getNome() { return nome; }
    public String getDia() { return dia; }
    public String getHorario() { return horario; }
    public String getDescricao() { return descricao; }
    public String getIdUsuario() { return idUsuario; }
}