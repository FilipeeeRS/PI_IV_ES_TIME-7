package com.example.aplicativo_horacerta.network

// Classe espelho dd PedidoDeLogin.java
data class PedidoDeLogin(
    val email: String,
    val senha: String
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 1L
    }
}