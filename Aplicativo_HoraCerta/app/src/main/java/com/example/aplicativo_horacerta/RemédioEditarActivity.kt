package com.example.aplicativo_horacerta

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// --- IMPORTAÇÕES NECESSÁRIAS PARA COMUNICAÇÃO (JSON/IO) ---
import com.example.aplicativo_horacerta.network.PedidoDeEditarMedicamento
import com.example.aplicativo_horacerta.network.PedidoDeDeletarMedicamento
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket
import java.io.InputStreamReader
// -----------------------------------------------------------


// =========================================================================
// Simulação das Classes de Dados
// =========================================================================
data class Medicamento(
    val id: Int,
    val nome: String,
    val tomou: Boolean,
    val dia: String,
    val horario: String,
    val descricao: String = ""
)
val sampleMedicamentos = listOf(
    Medicamento(1, "Buscopan (Preview)", true, "Quarta", "10:00", "Para dores abdominais.")
)
const val DUMMY_USER_ID = "usuario_logado_12345"
const val SERVER_IP = "SEU_IP_DO_SERVIDOR"
const val SERVER_PORT = 12345
// =========================================================================

class RemédioEditarActivity : ComponentActivity() {
    private var medicamentoId: String? = null

    // NOTA: Em um app real, você buscaria os dados do remédio do servidor aqui.
    private val dadosDoRemedio = sampleMedicamentos.find { it.id.toString() == medicamentoId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A linha abaixo usa um ID fixo para o Preview, mas o correto é pegar do Intent:
        medicamentoId = intent.getStringExtra("MEDICAMENTO_ID")

        setContent {
            Surface(color = Color.Black) {
                RemédioEditarScreen(
                    medicamentoId = medicamentoId,
                    dadosIniciais = dadosDoRemedio,
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}


@Composable
fun RemédioEditarScreen(
    medicamentoId: String?,
    dadosIniciais: Medicamento?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gson = remember { Gson() }

    // Estados para guardar o que o usuário digita
    var nome by remember { mutableStateOf(dadosIniciais?.nome ?: "") }
    var dia by remember { mutableStateOf(dadosIniciais?.dia ?: "") }
    var horario by remember { mutableStateOf(dadosIniciais?.horario ?: "") }
    var descricao by remember { mutableStateOf(dadosIniciais?.descricao ?: "") }

    val tomouOriginal = dadosIniciais?.tomou ?: false

    // Estilos
    val titleBarColor = Color(0xFFEEEEEE)
    val fieldBackgroundColor = Color(0xFFF0F0F0)
    val contentColor = Color.White
    val deleteButtonColor = Color(0xFFD32F2F)

    /**
     * Função auxiliar para enviar dados via Socket.
     */
    suspend fun enviarPedido(pedido: Any, sucessoMensagem: String, falhaMensagem: String) {
        val jsonDoPedido = gson.toJson(pedido)
        var sucesso = false

        withContext(Dispatchers.IO) {
            try {
                // 1. Configurar Conexão
                val socket = Socket(SERVER_IP, SERVER_PORT)
                val output = PrintWriter(socket.getOutputStream(), true)
                // CORREÇÃO DE TIPO: Usa InputStreamReader para garantir a leitura correta de caracteres
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                // 2. Enviar JSON
                output.println(jsonDoPedido)

                // 3. Receber Resposta (True/False)
                val resposta = input.readLine()
                socket.close()

                sucesso = resposta.trim().equals("true", ignoreCase = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        withContext(Dispatchers.Main) {
            if (sucesso) {
                Toast.makeText(context, sucessoMensagem, Toast.LENGTH_SHORT).show()
                onBackClick()
            } else {
                Toast.makeText(context, falhaMensagem, Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- FUNÇÃO PRIVADA PARA EDITAR (SALVAR) ---
    fun handleSaveClick() {
        if (medicamentoId == null) {
            Toast.makeText(context, "Erro: ID do medicamento não encontrado.", Toast.LENGTH_LONG).show()
            onBackClick()
            // Usa o 'return' simples, pois está em uma função comum
            return
        }

        // 1. Monta o Pedido de Edição
        val pedido = PedidoDeEditarMedicamento(
            id = medicamentoId,
            nome = nome,
            dia = dia,
            horario = horario,
            descricao = descricao,
            idUsuario = DUMMY_USER_ID,
            tomou = tomouOriginal
        )

        // 2. Envia a requisição em background
        coroutineScope.launch {
            enviarPedido(
                pedido,
                "Medicamento atualizado com sucesso!",
                "Falha na atualização ou comunicação."
            )
        }
    }

    // --- FUNÇÃO PRIVADA PARA DELETAR (EXCLUIR) ---
    fun handleDeleteClick() {
        if (medicamentoId == null) {
            Toast.makeText(context, "Erro: ID do medicamento não encontrado.", Toast.LENGTH_LONG).show()
            onBackClick()
            // Usa o 'return' simples, pois está em uma função comum
            return
        }

        // 1. Monta o Pedido de Deleção
        val pedido = PedidoDeDeletarMedicamento(
            idMedicamento = medicamentoId,
            idUsuario = DUMMY_USER_ID
        )

        // 2. Envia a requisição em background
        coroutineScope.launch {
            enviarPedido(
                pedido,
                "Medicamento excluído com sucesso!",
                "Falha na exclusão ou comunicação."
            )
        }
    }
    // --- FIM DA LÓGICA DE EDITAR E DELETAR ---


    Scaffold(
        topBar = {
            Column {
                // Barra "CUIDADOR" (Cabeçalho)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Fundo do Cabeçalho",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "CUIDADOR",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Título EDITAR MEDICAMENTO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(titleBarColor)
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "EDITAR MEDICAMENTO",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        // Formulário
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(contentColor)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Botão Voltar
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nome
            TextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("NOME:") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dia
            TextField(
                value = dia,
                onValueChange = { dia = it },
                label = { Text("DIA:") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Horário
            TextField(
                value = horario,
                onValueChange = { horario = it },
                label = { Text("HORÁRIO:") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descrição
            TextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("DESCRIÇÃO:") },
                singleLine = false,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botão 1: SALVAR (UPDATE)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp)
                    .clip(RoundedCornerShape(100.dp))
                    // Chamada correta: a lambda do clickable chama a função handleSaveClick()
                    .clickable(onClick = { handleSaveClick() }),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Fundo do Botão",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "SALVAR ALTERAÇÕES",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão 2: EXCLUIR (DELETE)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(deleteButtonColor)
                    // Chamada correta: a lambda do clickable chama a função handleDeleteClick()
                    .clickable(onClick = { handleDeleteClick() }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EXCLUIR MEDICAMENTO",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewRemédioEditarScreen() {
    Surface(color = Color.White) {
        val previewData = Medicamento(1, "Buscopan (Preview)", true, "Quarta", "10:00")
        RemédioEditarScreen(medicamentoId = "1", dadosIniciais = previewData, onBackClick = {})
    }
}