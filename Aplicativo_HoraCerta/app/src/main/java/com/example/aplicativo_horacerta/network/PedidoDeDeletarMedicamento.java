package com.example.aplicativo_horacerta.network

import com.google.gson.annotations.SerializedName

data class PedidoDeDeletarMedicamento(
        // Campo fixo para o Switch do servidor
        @SerializedName("operacao")
        val operacao: String = "PedidoDeDeletarMedicamento",

        // ID do Remédio (o _id do Mongo)
        @SerializedName("id")
        val idMedicamento: String,

        // ID do Usuário (filtro de segurança no servidor)
        @SerializedName("idUsuario")
        val idUsuario: String
)