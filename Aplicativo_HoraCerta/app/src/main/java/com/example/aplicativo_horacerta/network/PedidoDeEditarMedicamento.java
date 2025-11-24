package com.example.aplicativo_horacerta.network

import com.google.gson.annotations.SerializedName

data class PedidoDeEditarMedicamento(
        // CAMPO OBRIGATÓRIO para o Switch do servidor
        @SerializedName("operacao")
        val operacao: String = "PedidoDeEditarMedicamento",

        // Identificação do Remédio (usamos "id" no JSON para bater com o Java)
        @SerializedName("id")
        val idMedicamento: String, // <<--- Nome mais claro no Kotlin!

        @SerializedName("idUsuario")
        val idUsuario: String,

        // Dados para atualizar
        @SerializedName("nome")
        val nome: String,

        @SerializedName("dosagem")
        val dosagem: String,

        @SerializedName("tomou")
        val tomou: Boolean
)