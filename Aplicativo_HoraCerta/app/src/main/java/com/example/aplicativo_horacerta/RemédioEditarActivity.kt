package com.example.aplicativo_horacerta

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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

class RemédioEditarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idUsuarioLogado = intent.getStringExtra(FazerLoginActivity.KEY_USER_UID)
        val dadosDoRemedio = intent.getSerializableExtra("DADOS_MEDICAMENTO") as? Medicamento

        if (dadosDoRemedio == null || idUsuarioLogado.isNullOrBlank()) {
            Toast.makeText(this, "Erro: Dados ausentes.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            Surface(color = Color.Black) {
                RemédioEditarScreen(
                    idMedicamento = dadosDoRemedio.id,
                    initialNome = dadosDoRemedio.nome,
                    initialDia = dadosDoRemedio.data,
                    initialHorario = dadosDoRemedio.horario,
                    initialDescricao = dadosDoRemedio.descricao,
                    onBackClick = { finish() },
                    onSaveClick = { idMedicamento, nome, dia, horario, descricao ->

                        if (idMedicamento.isBlank() || idUsuarioLogado.isBlank()) return@RemédioEditarScreen

                        val isNomeChanged = nome != dadosDoRemedio.nome
                        val isDiaChanged = dia != dadosDoRemedio.data
                        val isHorarioChanged = horario != dadosDoRemedio.horario
                        val isDescricaoChanged = descricao != dadosDoRemedio.descricao

                        if (!isNomeChanged && !isDiaChanged && !isHorarioChanged && !isDescricaoChanged) {
                            Toast.makeText(this, "Nenhuma alteração.", Toast.LENGTH_LONG).show()
                            return@RemédioEditarScreen
                        }

                        lifecycleScope.launch {
                            val resultado = performEditMedicamento(
                                idMedicamento = idMedicamento,
                                nome = nome.takeIf { isNomeChanged } ?: "",
                                dia = dia.takeIf { isDiaChanged } ?: "",
                                horario = horario.takeIf { isHorarioChanged } ?: "",
                                descricao = descricao.takeIf { isDescricaoChanged } ?: "",
                                idUsuario = idUsuarioLogado
                            )

                            if (resultado?.isSucesso == true) {
                                Toast.makeText(this@RemédioEditarActivity, "Medicamento editado!", Toast.LENGTH_SHORT).show()
                                AlarmeActivity.agendar(
                                    context = this@RemédioEditarActivity,
                                    nome = nome,
                                    dia = dia,
                                    horario = horario,
                                    descricao = descricao,
                                    idUsuario = idUsuarioLogado
                                )
                                finish()

                            } else {
                                Toast.makeText(this@RemédioEditarActivity, "Falha: ${resultado?.mensagem}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

suspend fun performEditMedicamento(idMedicamento: String, nome: String, dia: String, horario: String, descricao: String, idUsuario: String?): ResultadoOperacao? {
    val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val CODIFICACAO = Charsets.UTF_8
    val pedido = PedidoDeEditarMedicamento(idMedicamento, nome, dia, horario, descricao, idUsuario)
    val gson = Gson()

    return withContext(Dispatchers.IO) {
        var conexao: Socket? = null
        try {
            conexao = Socket(SERVER_IP, SERVER_PORT)
            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), CODIFICACAO))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream(), CODIFICACAO))
            val servidor = Parceiro(conexao, receptor, transmissor)

            servidor.receba(pedido)
            transmissor.flush()
            val respostaComunicado: Any? = servidor.envie()

            if (respostaComunicado is ComunicadoJson) {
                val wrapperJson = respostaComunicado.json
                val wrapper = gson.fromJson(wrapperJson, WrapperResposta::class.java)
                val resultadoFinal = gson.fromJson(wrapper.operacaoJsonString, ResultadoOperacao::class.java)
                return@withContext resultadoFinal
            } else {
                return@withContext ResultadoOperacao(tipo="Erro", isSucesso=false, mensagem="Resposta inválida")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ResultadoOperacao(tipo="Erro", isSucesso=false, mensagem="Erro: ${e.message}")
        } finally {
            conexao?.close()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemédioEditarScreen(
    idMedicamento: String, initialNome: String, initialDia: String, initialHorario: String, initialDescricao: String,
    onBackClick: () -> Unit, onSaveClick: (String, String, String, String, String) -> Unit
) {
    var nome by remember { mutableStateOf(initialNome) }
    var dia by remember { mutableStateOf(initialDia) }
    var horario by remember { mutableStateOf(initialHorario) }
    var descricao by remember { mutableStateOf(initialDescricao) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val titleBarColor = Color(0xFFEEEEEE)
    val fieldBackgroundColor = Color(0xFFF0F0F0)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, y: Int, m: Int, d: Int ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            dia = dateFormat.format(cal.time)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, h, m -> horario = String.format("%02d:%02d", h, m) },
        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
    )

    Scaffold(
        topBar = {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                    Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(100.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("CUIDADOR", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().background(titleBarColor).padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
                    Text("EDITAR MEDICAMENTO", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White).verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.Start)) { Icon(Icons.Filled.ArrowBack, "Voltar", modifier = Modifier.size(64.dp), tint = Color.Black) }
            Spacer(modifier = Modifier.height(16.dp))

            TextField(value = nome, onValueChange = { nome = it }, label = { Text("NOME:") }, singleLine = true, shape = RoundedCornerShape(8.dp), colors = TextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            TextField(value = dia, onValueChange = { }, label = { Text("DIA:") }, singleLine = true, readOnly = true, shape = RoundedCornerShape(8.dp), colors = TextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black), trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Filled.DateRange, "Data", tint = Color.Black) } }, modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() })
            Spacer(modifier = Modifier.height(16.dp))

            TextField(value = horario, onValueChange = { }, label = { Text("HORÁRIO:") }, singleLine = true, readOnly = true, shape = RoundedCornerShape(8.dp), colors = TextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black), trailingIcon = { IconButton(onClick = { timePickerDialog.show() }) { Icon(Icons.Filled.AccessTime, "Horário", tint = Color.Black) } }, modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() })
            Spacer(modifier = Modifier.height(16.dp))

            TextField(value = descricao, onValueChange = { descricao = it }, label = { Text("DESCRIÇÃO:") }, singleLine = false, shape = RoundedCornerShape(8.dp), colors = TextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black), modifier = Modifier.fillMaxWidth().height(150.dp))
            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth(0.8f).height(70.dp).clip(RoundedCornerShape(100.dp)).clickable { onSaveClick(idMedicamento, nome, dia, horario, descricao) }, contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Text("CONFIRMAR", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}