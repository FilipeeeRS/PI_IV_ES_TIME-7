package com.example.aplicativo_horacerta.socket

import android.content.Context
import android.util.Log
import com.example.aplicativo_horacerta.AlarmeActivity
import com.example.aplicativo_horacerta.ResultadoListaMedicamentos
import com.example.aplicativo_horacerta.ResultadoOperacao
import com.example.aplicativo_horacerta.WrapperResposta
import com.example.aplicativo_horacerta.network.ComunicadoJson
import com.example.aplicativo_horacerta.network.PedidoBuscarCuidador
import com.example.aplicativo_horacerta.network.PedidoDeConfirmarAlarme
import com.example.aplicativo_horacerta.network.PedidoDeCriarMedicamento
import com.example.aplicativo_horacerta.network.PedidoDeDeletarMedicamento
import com.example.aplicativo_horacerta.network.PedidoDeEditarMedicamento
import com.example.aplicativo_horacerta.network.PedidoDeListarMedicamentos
import com.example.aplicativo_horacerta.network.ResultadoBuscaCuidador
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MedicamentoRepository {

    private val networkService = NetworkService()
    private val GSON = NetworkConfig.GSON

    // LISTAR
    suspend fun performListarMedicamentos(userId: String): ResultadoListaMedicamentos? {
        val pedido = PedidoDeListarMedicamentos(userId)
        return try {
            val respostaComunicado = withContext(Dispatchers.IO) {
                networkService.sendRequest(pedido)
            } as? ComunicadoJson

            if (respostaComunicado != null) {
                val wrapperJson = respostaComunicado.json
                val wrapper = GSON.fromJson(wrapperJson, WrapperResposta::class.java)
                val tipo = object : TypeToken<ResultadoListaMedicamentos>() {}.type
                GSON.fromJson<ResultadoListaMedicamentos>(wrapper.operacaoJsonString, tipo)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("MedicamentoRepo", "Erro ao listar:", e)
            null
        }
    }

    // CRIAR
    suspend fun performCriarMedicamento(
        nome: String, dia: String, horario: String, descricao: String, idUsuario: String?
    ): String {
        val pedido = PedidoDeCriarMedicamento(nome, dia, horario, descricao, idUsuario)
        return try {
            val resposta = withContext(Dispatchers.IO) {
                networkService.sendRequest(pedido)
            }
            resposta?.toString() ?: "Resposta vazia do servidor"
        } catch (e: Exception) {
            "Erro: Falha na conexão. O servidor pode estar desligado."
        }
    }

    // EDITAR
    suspend fun performEditMedicamento(idMedicamento: String, nome: String, dia: String, horario: String, descricao: String, idUsuario: String?): ResultadoOperacao {
        val pedido = PedidoDeEditarMedicamento(idMedicamento, nome, dia, horario, descricao, idUsuario)
        return try {
            val respostaComunicado = withContext(Dispatchers.IO) {
                networkService.sendRequest(pedido)
            } as? ComunicadoJson

            if (respostaComunicado != null) {
                val wrapperJson = respostaComunicado.json
                val wrapper = GSON.fromJson(wrapperJson, WrapperResposta::class.java)
                GSON.fromJson(wrapper.operacaoJsonString, ResultadoOperacao::class.java)
            } else {
                ResultadoOperacao("ErroCliente", false, "Resposta inválida")
            }
        } catch (e: Exception) {
            ResultadoOperacao("Erro", false, "Falha ao salvar. Tente novamente.")
        }
    }

    // DELETAR
    suspend fun performDeleteMedicamento(idMedicamento: String, idUsuario: String?): ResultadoOperacao {
        val pedido = PedidoDeDeletarMedicamento(idMedicamento, idUsuario)
        return try {
            val respostaComunicado = withContext(Dispatchers.IO) {
                networkService.sendRequest(pedido)
            } as? ComunicadoJson

            if (respostaComunicado != null) {
                val wrapperJson = respostaComunicado.json
                val wrapper = GSON.fromJson(wrapperJson, WrapperResposta::class.java)
                GSON.fromJson(wrapper.operacaoJsonString, ResultadoOperacao::class.java)
            } else {
                ResultadoOperacao("ErroCliente", false, "Formato inesperado")
            }
        } catch (e: Exception) {
            ResultadoOperacao("ErroComunicação", false, "Erro: ${e.message}")
        }
    }

    // BUSCAR CUIDADOR
    suspend fun buscarDadosCuidador(): Pair<String, String>? {
        val emailIdoso = FirebaseAuth.getInstance().currentUser?.email ?: return null
        val pedido = PedidoBuscarCuidador(emailIdoso)

        return try {
            val respostaComunicado = withContext(Dispatchers.IO) {
                networkService.sendRequest(pedido)
            } as? ComunicadoJson

            if (respostaComunicado != null) {
                val wrapperJson = respostaComunicado.json
                val wrapper = GSON.fromJson(wrapperJson, WrapperResposta::class.java)
                val tipo = object : TypeToken<ResultadoBuscaCuidador>() {}.type
                val resultado = GSON.fromJson<ResultadoBuscaCuidador>(wrapper.operacaoJsonString, tipo)

                if (resultado.isEncontrou()) {
                    Pair(resultado.getNomeCuidador(), resultado.getUidCuidador())
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // SINCRONIZAR ALARMES
    suspend fun sincronizarRemediosEAgendar(context: Context, uidCuidador: String) {
        val resultadoLista = performListarMedicamentos(uidCuidador)
        if (resultadoLista?.medicamentos != null) {
            for (remedio in resultadoLista.medicamentos) {
                AlarmeActivity.agendar(
                    context = context,
                    nome = remedio.nome,
                    dia = remedio.data,
                    horario = remedio.horario,
                    descricao = remedio.descricao,
                    idUsuario = uidCuidador
                )
            }
        }
    }

    // CONFIRMAR ALARME
    suspend fun performConfirmarAlarme(idUsuario: String, nomeRemedio: String, dia: String, horario: String): Boolean {
        val pedido = PedidoDeConfirmarAlarme(idUsuario, nomeRemedio, dia, horario)
        return try {
            val resposta = withContext(Dispatchers.IO) {
                networkService.sendRequest(pedido)
            }
            resposta != null
        } catch (e: Exception) {
            Log.e("MedicamentoRepo", "Erro ao confirmar alarme:", e)
            false
        }
    }
}