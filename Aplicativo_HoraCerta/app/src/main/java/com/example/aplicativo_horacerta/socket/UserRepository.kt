package com.example.aplicativo_horacerta.socket

import com.example.aplicativo_horacerta.network.ComunicadoJson
import com.example.aplicativo_horacerta.network.ComunicadoWrapper
import com.example.aplicativo_horacerta.network.PedidoDeCadastro
import com.example.aplicativo_horacerta.network.PedidoDeConexao
import com.example.aplicativo_horacerta.network.PedidoDeLogin
import com.example.aplicativo_horacerta.network.ResultadoLogin
import com.example.aplicativo_horacerta.network.ResultadoOperacao

class UserRepository(private val networkService: NetworkService = NetworkService()) {

    // Login
    suspend fun doLogin(email: String, firebaseUid: String): String {
        val pedido = PedidoDeLogin(email, firebaseUid)
        try {
            val respostaComunicado = networkService.sendRequest(pedido) as? ComunicadoJson
            val jsonBruto = (respostaComunicado?.json ?: "")

            val wrapper = NetworkConfig.GSON.fromJson(jsonBruto, ComunicadoWrapper::class.java)
            val resultadoLogin = NetworkConfig.GSON.fromJson(wrapper.operacao, ResultadoLogin::class.java)

            return if (resultadoLogin.isSuccessful) {
                "SUCESSO:${resultadoLogin.getFirebaseUid()}:${resultadoLogin.getProfileType()}"
            } else {
                "FALHA:Login rejeitado. Mensagem: ${resultadoLogin.getMensagem()}"
            }
        } catch (e: Exception) {
            return "ERRO_CONEXAO: Não foi possível conectar ao servidor. Verifique o IP ou seu Wi-Fi."
        }
    }

    // Criar Conta
    suspend fun createAccount(nome: String, email: String, firebaseUid: String, profileType: String): String {
        val pedido = PedidoDeCadastro(nome, email, firebaseUid, profileType)
        try {
            val resposta = networkService.sendRequest(pedido)
            return resposta?.toString() ?: "Resposta vazia do servidor"
        } catch (e: Exception) {
            return "Erro de conexão: ${e.message}"
        }
    }

    // CONECTAR IDOSO COM CUIDADOR
    suspend fun performConexao(emailIdoso: String, idCuidador: String): ResultadoOperacao {
        val pedido = PedidoDeConexao(emailIdoso, idCuidador)
        try {
            val respostaComunicado = networkService.sendRequest(pedido) as? ComunicadoJson

            if (respostaComunicado != null) {
                val wrapper = NetworkConfig.GSON.fromJson(respostaComunicado.json, ComunicadoWrapper::class.java)
                return NetworkConfig.GSON.fromJson(wrapper.operacao, ResultadoOperacao::class.java)
            }
            // Retorna erro se a resposta for nula
            return ResultadoOperacao("Erro", false, "Resposta vazia")
        } catch (e: Exception) {
            return ResultadoOperacao("Erro", false, e.message)
        }
    }

    // BUSCAR IDOSO
    suspend fun buscarIdoso(email: String): Pair<Boolean, String?> {
        val pedido = com.example.aplicativo_horacerta.network.PedidoBuscarIdoso(email)
        try {
            val respostaComunicado = networkService.sendRequest(pedido) as? ComunicadoJson
            if (respostaComunicado != null) {
                val jsonBruto = respostaComunicado.json

                val wrapper = NetworkConfig.GSON.fromJson(jsonBruto, ComunicadoWrapper::class.java)
                val resultado = NetworkConfig.GSON.fromJson(wrapper.operacao, com.example.aplicativo_horacerta.network.ResultadoBuscaIdoso::class.java)

                return Pair(resultado.isEncontrou, resultado.nomeIdoso)
            }
            return Pair(false, null)
        } catch (e: Exception) {
            return Pair(false, null)
        }
    }
}