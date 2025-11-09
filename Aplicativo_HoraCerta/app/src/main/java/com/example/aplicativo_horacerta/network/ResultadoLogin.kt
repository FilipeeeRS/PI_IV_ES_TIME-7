package com.example.aplicativo_horacerta.network

// Classe espelho de ResultadoLogin.java
data class ResultadoLogin(
    val status: String,
    val usuario: Usuario?
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 1L
    }
}