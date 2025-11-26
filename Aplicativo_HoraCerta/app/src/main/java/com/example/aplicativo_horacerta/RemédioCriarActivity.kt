package com.example.aplicativo_horacerta

import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import com.example.aplicativo_horacerta.network.Parceiro
import com.example.aplicativo_horacerta.network.PedidoDeCriarMedicamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.material.icons.filled.AccessTime // Ícone de relógio
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.DatePicker
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.filled.DateRange

class RemédioCriarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Obter o ID do usuário logado (pode vir de SharedPreferences, Bundle, ou um ViewModel)
        val idUsuarioLogado = intent.getStringExtra(FazerLoginActivity.KEY_USER_UID)// Mude isso para o ID real

        setContent {
            Surface(color = Color.Black) {
                RemédioCriarScreen(
                    onBackClick = {
                        finish()
                    },
                    onSaveClick = { nome, dia, horario, descricao ->

                        // 1. Verifica se os campos estão preenchidos (básico)
                        if (nome.isBlank() || dia.isBlank() || horario.isBlank()) {
                            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                            return@RemédioCriarScreen // Para a execução aqui
                        }

                        // 2. Valida se a data e hora são futuras
                        if (!isDataHorarioValido(dia, horario)) {
                            Toast.makeText(this, "A data e horário devem ser no futuro!", Toast.LENGTH_LONG).show()
                            return@RemédioCriarScreen // Para a execução aqui
                        }


                        // Lógica para salvar o novo medicamento
                        lifecycleScope.launch {
                            performCriarMedicamento(nome, dia, horario, descricao, idUsuarioLogado, { resultado ->
                                // Aqui você trata o resultado retornado pelo servidor
                                println("Resultado do Cadastro: $resultado")
                                // Exibir uma mensagem (Toast) para o usuário seria ideal
                                Toast.makeText(this@RemédioCriarActivity, resultado, Toast.LENGTH_LONG).show()
                                // Fecha a tela após a tentativa de cadastro
                            }
                            )
                        }
                    }
                )
            }
        }
    }
}

fun isDataHorarioValido(dia: String, horario: String): Boolean {
    return try {
        // Define o formato que suas Strings estão vindo (ajuste se necessário)
        // Exemplo: dia = "26/11/2025", horario = "14:30"
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        format.isLenient = false // Garante que datas como 30/02 não passem

        // Junta as strings e converte para Date
        val dataInformada = format.parse("$dia $horario")
        val dataAtual = Calendar.getInstance().time

        // Retorna true se a data informada for DEPOIS da atual
        dataInformada != null && dataInformada.after(dataAtual)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


suspend fun performCriarMedicamento(
    nome: String,
    dia: String,
    horario: String,
    descricao: String,
    idUsuario: String?,
    onResult: (String) -> Unit
) {
    val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000


    val pedido = PedidoDeCriarMedicamento(nome, dia, horario, descricao, idUsuario)

    withContext(Dispatchers.IO) {
        try {
            val conexao = Socket(SERVER_IP, SERVER_PORT)
            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream()))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream()))
            val servidor = Parceiro(conexao, receptor, transmissor)

            // 1. Envia o pedido ao servidor
            servidor.receba(pedido)

            // 2. Aguarda e recebe a resposta do servidor
            val resposta: Any? = servidor.envie()

            // Transfere o resultado para a Main Thread para atualização da UI
            withContext(Dispatchers.Main) {
                onResult(resposta?.toString() ?: "Resposta vazia do servidor")
            }

            conexao.close()

        } catch (e: Exception) {
            e.printStackTrace()
            // Transfere o erro para a Main Thread
            withContext(Dispatchers.Main) {
                onResult("Erro de comunicação: ${e.message}")
            }
        }
    }
}

@Composable
fun RemédioCriarScreen(
    onBackClick: () -> Unit,
    onSaveClick: (String, String, String, String) -> Unit
) {
    // Estados para guardar o que o usuário digita
    var nome by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Cores do design
    val headerColor = Color(0xFF0A9396)
    val titleBarColor = Color(0xFFEEEEEE)
    val fieldBackgroundColor = Color(0xFFF0F0F0)
    val contentColor = Color.White

    // Lógica do DatePicker (Dia)
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("dd/MM EEEE", Locale("pt", "BR"))
            dia = dateFormat.format(selectedCalendar.time).replaceFirstChar { it.uppercase() }
        },
        year, month, day
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

                // ADICIONAR MEDICAMENTO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(titleBarColor)
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ADICIONAR MEDICAMENTO",
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
                    .clickable { onSaveClick(nome, dia, horario, descricao) },
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewRemédioCriarScreen() {
    Surface(color = Color.White) {
        RemédioCriarScreen(onBackClick = {}, onSaveClick = {_,_,_,_ ->})
    }
}