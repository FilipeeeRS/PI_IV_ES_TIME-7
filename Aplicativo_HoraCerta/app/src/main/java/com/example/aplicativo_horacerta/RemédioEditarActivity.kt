package com.example.aplicativo_horacerta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.aplicativo_horacerta.network.ComunicadoJson
import com.example.aplicativo_horacerta.network.Parceiro
import com.example.aplicativo_horacerta.network.PedidoDeEditarMedicamento
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Serializable
import java.net.Socket
import java.util.Locale


class RemédioEditarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recebe os dados da Intent
        val idUsuarioLogado = intent.getStringExtra(FazerLoginActivity.KEY_USER_UID)
        val dadosDoRemedio = intent.getSerializableExtra("DADOS_MEDICAMENTO") as? Medicamento

        // Validação básica se os dados foram passados
        if (dadosDoRemedio == null || idUsuarioLogado.isNullOrBlank()) {
            Toast.makeText(this, "Erro: Dados do medicamento ou usuário ausentes para edição.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            Surface(color = Color.Black) {
                // O Composable é chamado com os campos separados do objeto Medicamento
                RemédioEditarScreen(
                    idMedicamento = dadosDoRemedio.id, // Passa o ID
                    initialNome = dadosDoRemedio.nome,
                    initialDia = dadosDoRemedio.data, // <--- Referência atualizada para .data
                    initialHorario = dadosDoRemedio.horario,
                    initialDescricao = dadosDoRemedio.descricao,
                    onBackClick = {
                        finish()
                    },
                    // Implementação da Lógica de Salvar
                    onSaveClick = { idMedicamento, nome, dia, horario, descricao ->

                        // Validação de segurança dos IDs
                        if (idMedicamento.isBlank() || idUsuarioLogado.isBlank()) {
                            Toast.makeText(this, "Erro: ID do medicamento ou usuário ausente.", Toast.LENGTH_LONG).show()
                            return@RemédioEditarScreen
                        }

                        // Validação de Negócio Flexível: Compara novos valores com valores iniciais.
                        val isNomeChanged = nome != dadosDoRemedio.nome
                        val isDiaChanged = dia != dadosDoRemedio.data // <--- Comparação atualizada para .data
                        val isHorarioChanged = horario != dadosDoRemedio.horario
                        val isDescricaoChanged = descricao != dadosDoRemedio.descricao

                        if (!isNomeChanged && !isDiaChanged && !isHorarioChanged && !isDescricaoChanged) {
                            Toast.makeText(this, "Nenhuma alteração detectada para salvar.", Toast.LENGTH_LONG).show()
                            return@RemédioEditarScreen
                        }


                        // Se houver alteração, inicia a operação de rede
                        lifecycleScope.launch {
                            val resultado = performEditMedicamento(
                                idMedicamento = idMedicamento,
                                // Envia a string vazia se o valor não tiver mudado, para o servidor ignorar o campo
                                nome = nome.takeIf { isNomeChanged } ?: "",
                                dia = dia.takeIf { isDiaChanged } ?: "", // Usa 'dia' para o pedido de rede
                                horario = horario.takeIf { isHorarioChanged } ?: "",
                                descricao = descricao.takeIf { isDescricaoChanged } ?: "",
                                idUsuario = idUsuarioLogado
                            )

                            // Processa o resultado na thread principal
                            if (resultado?.isSucesso == true) {
                                Toast.makeText(this@RemédioEditarActivity, "Medicamento editado com sucesso!", Toast.LENGTH_SHORT).show()
                                finish() // Retorna para a tela anterior
                            } else {
                                val mensagem = resultado?.mensagem ?: "Erro de edição desconhecido."
                                Toast.makeText(this@RemédioEditarActivity, "Falha: $mensagem", Toast.LENGTH_LONG).show()
                                Log.e("EDIT", "Falha na edição: $mensagem")
                            }
                        }
                    }
                )
            }
        }
    }
}

// =================================================================================
// FUNÇÃO DE REDE (Networking)
// =================================================================================

suspend fun performEditMedicamento(
    idMedicamento: String,
    nome: String,
    dia: String,
    horario: String,
    descricao: String,
    idUsuario: String?
): ResultadoOperacao? { // Retorna um objeto ResultadoOperacao

    // Configurações de rede - Manter estes valores
    val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val CODIFICACAO = Charsets.UTF_8

    if (idUsuario.isNullOrBlank()) {
        Log.e("EditNetwork", "ID do Usuário é nulo ou vazio.")
        return ResultadoOperacao(tipo = "ErroCliente", isSucesso = false, mensagem = "Sessão inválida.")
    }

    // Cria o objeto de pedido
    val pedido = PedidoDeEditarMedicamento(idMedicamento, nome, dia, horario, descricao, idUsuario)
    val gson = Gson()

    return withContext(Dispatchers.IO) {
        var conexao: Socket? = null
        try {
            // 1. Inicializa a conexão
            conexao = Socket(SERVER_IP, SERVER_PORT)

            val transmissor =
                BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), CODIFICACAO))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream(), CODIFICACAO))
            val servidor = Parceiro(conexao, receptor, transmissor) // Inicializa o Parceiro

            // 2. Envia o pedido
            servidor.receba(pedido)
            transmissor.flush()

            // 3. Aguarda e recebe a resposta do servidor
            val respostaComunicado: Any? = servidor.envie()

            if (respostaComunicado is ComunicadoJson) {
                // Desempacota e desserializa o resultado
                val wrapperJson = respostaComunicado.json
                val wrapper = gson.fromJson(wrapperJson, WrapperResposta::class.java)
                val jsonStringAninhada = wrapper.operacaoJsonString

                val resultadoFinal = gson.fromJson(jsonStringAninhada, ResultadoOperacao::class.java)

                Log.d("EditNetwork", "Resultado Final (isSucesso): ${resultadoFinal.isSucesso}")
                return@withContext resultadoFinal

            } else {
                Log.e("EditNetwork", "Erro: Resposta recebida não é um ComunicadoJson.")
                return@withContext ResultadoOperacao(tipo = "ErroCliente", isSucesso = false, mensagem = "Formato de resposta inesperado do servidor.")
            }

        } catch (e: Exception) {
            Log.e("EditNetwork", "Erro GERAL de rede ou desserialização:", e)
            e.printStackTrace()
            return@withContext ResultadoOperacao(tipo = "ErroComunicação", isSucesso = false, mensagem = "Erro de comunicação: ${e.message}")
        } finally {
            conexao?.close()
        }
    }
}

// =================================================================================
// COMPOSABLE (UI) - RemédioEditarScreen
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemédioEditarScreen(
    idMedicamento: String, // ID do medicamento que será editado
    initialNome: String,
    initialDia: String,
    initialHorario: String,
    initialDescricao: String,
    onBackClick: () -> Unit,
    // A função onSaveClick agora recebe o ID do medicamento como primeiro parâmetro
    onSaveClick: (id: String, nome: String, dia: String, horario: String, descricao: String) -> Unit
) {
    // Estados iniciais preenchidos com os dados existentes
    var nome by remember { mutableStateOf(initialNome) }
    var dia by remember { mutableStateOf(initialDia) }
    var horario by remember { mutableStateOf(initialHorario) }
    var descricao by remember { mutableStateOf(initialDescricao) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Cores do design
    val titleBarColor = Color(0xFFEEEEEE)
    val fieldBackgroundColor = Color(0xFFF0F0F0)
    val contentColor = Color.White

    // Lógica do DatePicker (Dia)
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("dd/MM EEEE", Locale("pt", "BR"))
            dia = dateFormat.format(selectedCalendar.time).replaceFirstChar { it.uppercase() }
        },
        year, month, dayOfMonth
    )

    // Lógica do TimePicker (Horário)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            horario = String.format("%02d:%02d", selectedHour, selectedMinute)
        },
        hour, minute, true
    )

    Scaffold(
        topBar = {
            Column {
                // Barra "CUIDADOR" (igual HomeCuidador)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    // Assumindo que R.drawable.ic_launcher_background é o recurso correto para o fundo
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
                        // Assumindo que R.drawable.ic_launcher_foreground é o recurso correto para o logo
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

                // TÍTULO: EDITAR MEDICAMENTO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(titleBarColor)
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "EDITAR MEDICAMENTO", // Título ajustado
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
                    modifier = Modifier.size(64.dp),
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
                    focusedContainerColor = fieldBackgroundColor,
                    unfocusedContainerColor = fieldBackgroundColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dia
            TextField(
                value = dia,
                onValueChange = { },
                label = { Text("DIA:") },
                singleLine = true,
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor,
                    unfocusedContainerColor = fieldBackgroundColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent

                ),
                // Ícone de Calendário
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Selecionar Data",
                            tint = Color.Black
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Horário
            TextField(
                value = horario,
                onValueChange = { },
                label = { Text("HORÁRIO:") },
                singleLine = true,
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBackgroundColor,
                    unfocusedContainerColor = fieldBackgroundColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                // Ícone clicável no final
                trailingIcon = {
                    IconButton(onClick = { timePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Selecionar Horário",
                            tint = Color.Black
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() }
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
                    focusedContainerColor = fieldBackgroundColor,
                    unfocusedContainerColor = fieldBackgroundColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botão Confirmar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp)
                    .clip(RoundedCornerShape(100.dp))
                    // CHAMADA DE SALVAR: Agora passa o ID do medicamento
                    .clickable { onSaveClick(idMedicamento, nome, dia, horario, descricao) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Fundo do Botão",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "CONFIRMAR",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}