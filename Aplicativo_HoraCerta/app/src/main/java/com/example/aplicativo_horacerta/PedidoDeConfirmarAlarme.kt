package com.example.aplicativo_horacerta.network

import com.google.gson.annotations.SerializedName

data class PedidoDeConfirmarAlarme(
    @SerializedName("idUsuario") val idUsuario: String,
    @SerializedName("nomeRemedio") val nomeRemedio: String,
    @SerializedName("dia") val dia: String,
    @SerializedName("horario") val horario: String
) {
    @SerializedName("operacao")
    val operacao: String = "ConfirmarAlarme"
}