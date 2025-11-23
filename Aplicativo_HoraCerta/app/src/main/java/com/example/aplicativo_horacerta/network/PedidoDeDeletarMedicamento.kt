package com.example.aplicativo_horacerta.network

import com.google.gson.annotations.SerializedName

data class PedidoDeDeletarMedicamento(
    // O campo no JSON deve ser "id"
    @SerializedName("id")
    val idMedicamento: String,

    // O campo no JSON deve ser "idUsuario"
    @SerializedName("idUsuario")
    val idUsuario: String
)