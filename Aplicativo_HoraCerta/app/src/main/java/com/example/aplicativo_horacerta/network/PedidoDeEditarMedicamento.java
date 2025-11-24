package com.example.aplicativo_horacerta.network

import com.google.gson.annotations.SerializedName

data class PedidoDeEditarMedicamento(
        // CAMPO OBRIGATÓRIO
        @SerializedName("operacao")
        val operacao: String = "PedidoDeEditarMedicamento",

        // Identificação
        @SerializedName("id")
        val idMedicamento: String,

        @SerializedName("idUsuario")
        val idUsuario: String,

        // Dados para Atualizar
        @SerializedName("nome")
        val nome: String,

        @SerializedName("dia")
        val dia: String, // << AGORA INCLUÍDO

        @SerializedName("horario")
        val horario: String, // << AGORA INCLUÍDO

        @SerializedName("descricao")
        val descricao: String,

        @SerializedName("tomou")
        val tomou: Boolean
)