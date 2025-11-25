package com.example.aplicativo_horacerta.network

import com.google.gson.annotations.SerializedName

data class PedidoDeEditarMedicamento(
    @SerializedName("operacao")
    val operacao: String = "PedidoDeEditarMedicamento",

    // Identificação
    @SerializedName("id")
    val idMedicamento: String,

    @SerializedName("idUsuario")
    val idUsuario: String,

    // Dados que você tem no banco
    @SerializedName("nome")
    val nome: String,

    @SerializedName("descricao") // Usando 'descricao' no lugar de 'dosagem'
    val descricao: String,

    @SerializedName("tomou")
    val tomou: Boolean // Campo para histórico/status
)