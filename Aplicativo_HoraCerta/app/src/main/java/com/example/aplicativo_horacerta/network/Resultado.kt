package com.example.aplicativo_horacerta.network

// Classe espelho de Resultado.java do seu servidor
data class Resultado(
    val sucesso: Boolean,
    val mensagem: String
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 1L
    }
}