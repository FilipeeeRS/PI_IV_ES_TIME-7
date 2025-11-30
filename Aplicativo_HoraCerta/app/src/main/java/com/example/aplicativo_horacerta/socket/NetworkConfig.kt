package com.example.aplicativo_horacerta.socket

import com.google.gson.Gson

// NetworkConfig.kt
object NetworkConfig {

    // Escolhar qual IP usar
    //const val SERVER_IP = "10.0.116.3"
    const val SERVER_IP = "10.0.2.2"

    const val SERVER_PORT = 3000
    val GSON = Gson()
    val CODIFICACAO = Charsets.UTF_8
}