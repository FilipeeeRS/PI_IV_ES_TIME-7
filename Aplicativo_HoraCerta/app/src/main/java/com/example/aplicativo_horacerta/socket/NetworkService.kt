package com.example.aplicativo_horacerta.socket

import android.util.Log
import com.example.aplicativo_horacerta.network.ComunicadoJson
import com.example.aplicativo_horacerta.network.Parceiro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

// NetworkService.kt
class NetworkService {

    // Função única para lidar com a comunicação via Socket
    suspend fun sendRequest(pedido: ComunicadoJson): Any? = withContext(Dispatchers.IO){
        var conexao: Socket? = null
        var servidor: Parceiro? = null
        try {
            // Conexão e Streams são centralizadas
            conexao = Socket(NetworkConfig.SERVER_IP, NetworkConfig.SERVER_PORT)

            val transmissor = BufferedWriter(
                OutputStreamWriter(
                    conexao.getOutputStream(),
                    NetworkConfig.CODIFICACAO
                )
            )
            val receptor = BufferedReader(
                InputStreamReader(
                    conexao.getInputStream(),
                    NetworkConfig.CODIFICACAO
                )
            )

            servidor = Parceiro(conexao, receptor, transmissor)

            // Envia o pedido
            servidor.receba(pedido)
            transmissor.flush()

            // Recebe a resposta bruta
            return@withContext servidor.envie()

        } catch (e: Exception) {
            Log.e("NetworkService", "Erro na comunicação via Socket:", e)
            e.printStackTrace()
            throw e
        } finally {
            conexao?.close()
        }
    }
}