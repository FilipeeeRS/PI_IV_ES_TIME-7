package com.example.aplicativo_horacerta.network

import com.google.gson.annotations.SerializedName

data class ComunicadoWrapper(
    @SerializedName("operacao") val operacao: String
)