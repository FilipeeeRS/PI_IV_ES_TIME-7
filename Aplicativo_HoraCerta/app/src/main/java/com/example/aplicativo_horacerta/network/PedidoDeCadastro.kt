package com.example.aplicativo_horacerta.network

// Classe espelho dd PedidoDeCadastro.java
data class PedidoDeCadastro(
    val nome: String,
    val email: String,
    val senha: String,
    val tipo: String  // cuidador/idoso
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 1L
    }
}