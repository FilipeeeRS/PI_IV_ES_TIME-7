package com.example.aplicativo_horacerta.network

// Classe espelho de PedidoDeDeletarMedicamento.java
data class PedidoDeDeletarMedicamento(
    val idMedicamento: String
) : Comunicado() {
    companion object {
        private const val serialVersionUID = 1L
    }
}