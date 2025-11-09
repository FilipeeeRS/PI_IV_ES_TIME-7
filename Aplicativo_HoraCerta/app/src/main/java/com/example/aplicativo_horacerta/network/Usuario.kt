package com.example.aplicativo_horacerta.network

import java.io.Serializable

// Classe espelho de Usuario.java
data class Usuario(
    val _id: String?,
    val uid: String?,
    val nome: String,
    val email: String,
    val tipo: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}