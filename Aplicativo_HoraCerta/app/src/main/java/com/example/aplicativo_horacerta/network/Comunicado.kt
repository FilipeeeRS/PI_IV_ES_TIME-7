package com.example.aplicativo_horacerta.network

import java.io.Serializable

// Classe mãe, envia os objetos para o server
open class Comunicado : Serializable {
    companion object {
        // Valor igual ao Comunicado.java
        private const val serialVersionUID = 1L
    }
}