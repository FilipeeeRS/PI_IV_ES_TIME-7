package com.example.aplicativo_horacerta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.aplicativo_horacerta.network.Parceiro
import com.example.aplicativo_horacerta.network.PedidoDeCriarMedicamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RemédioCriarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idUsuarioLogado = intent.getStringExtra(FazerLoginActivity.KEY_USER_UID)

        setContent {
            Surface(color = Color.Black) {
                RemédioCriarScreen(
                    onBackClick = { finish() },
                    onSaveClick = { nome, dia, horario, descricao ->
                        if (nome.isBlank() || dia.isBlank() || horario.isBlank()) {
                            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                            return@RemédioCriarScreen
                        }
                        if (!isDataHorarioValido(dia, horario)) {
                            Toast.makeText(this, "A data e horário devem ser no futuro!", Toast.LENGTH_LONG).show()
                            return@RemédioCriarScreen
                        }

                        lifecycleScope.launch {
                            performCriarMedicamento(nome, dia, horario, descricao, idUsuarioLogado) { resultado ->
                                Toast.makeText(this@RemédioCriarActivity, resultado, Toast.LENGTH_LONG).show()

                                if (!resultado.contains("Erro", ignoreCase = true) && idUsuarioLogado != null) {
                                    AlarmeActivity.agendar(
                                        context = this@RemédioCriarActivity,
                                        nome = nome,
                                        dia = dia,
                                        horario = horario,
                                        descricao = descricao,
                                        idUsuario = idUsuarioLogado
                                    )
                                    finish()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

fun isDataHorarioValido(dia: String, horario: String): Boolean {
    return try {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        format.isLenient = false
        val dataInformada = format.parse("$dia $horario")
        val dataAtual = Calendar.getInstance().time
        dataInformada != null && dataInformada.after(dataAtual)
    } catch (e: Exception) {
        false
    }
}

suspend fun performCriarMedicamento(
    nome: String, dia: String, horario: String, descricao: String, idUsuario: String?, onResult: (String) -> Unit
) {

    val SERVER_IP = "10.0.116.3"
    //val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val pedido = PedidoDeCriarMedicamento(nome, dia, horario, descricao, idUsuario)

    withContext(Dispatchers.IO) {
        try {
            val conexao = Socket(SERVER_IP, SERVER_PORT)
            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream()))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream()))
            val servidor = Parceiro(conexao, receptor, transmissor)

            servidor.receba(pedido)
            val resposta: Any? = servidor.envie()

            withContext(Dispatchers.Main) { onResult(resposta?.toString() ?: "Resposta vazia do servidor") }
            conexao.close()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult("Erro: ${e.message}") }
        }
    }
}

@Composable
fun RemédioCriarScreen(onBackClick: () -> Unit, onSaveClick: (String, String, String, String) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Configura DatePicker para dd/MM/yyyy
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            dia = format.format(cal.time)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m -> horario = String.format("%02d:%02d", h, m) },
        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
    )
    val fieldBackgroundColor = Color(0xFFF0F0F0)

    Scaffold(
        topBar = {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                    Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(100.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("CUIDADOR", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE)).padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
                    Text("ADICIONAR MEDICAMENTO", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Filled.ArrowBack, "Voltar", tint = Color.Black, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(16.dp))

            TextField(value = nome, onValueChange = { nome = it }, label = { Text("NOME:") }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))

            TextField(value = dia, onValueChange = {}, label = { Text("DIA:") }, readOnly = true, trailingIcon = { IconButton({ datePickerDialog.show() }) { Icon(Icons.Default.DateRange, null) } }, colors = TextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black), modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() })
            Spacer(Modifier.height(16.dp))

            TextField(value = horario, onValueChange = {}, label = { Text("HORÁRIO:") }, readOnly = true, trailingIcon = { IconButton({ timePickerDialog.show() }) { Icon(Icons.Default.AccessTime, null) } }, colors = TextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black), modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() })
            Spacer(Modifier.height(16.dp))

            TextField(value = descricao, onValueChange = { descricao = it }, label = { Text("DESCRIÇÃO:") }, singleLine = false, shape = RoundedCornerShape(8.dp), colors = TextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black), modifier = Modifier.fillMaxWidth().height(150.dp))
            Spacer(Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth(0.8f).height(70.dp).clip(RoundedCornerShape(100.dp)).clickable { onSaveClick(nome, dia, horario, descricao) }, contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Text("CONFIRMAR", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewRemédioCriarScreen() {
    Surface(color = Color.White) { RemédioCriarScreen(onBackClick = {}, onSaveClick = {_,_,_,_ ->}) }
}