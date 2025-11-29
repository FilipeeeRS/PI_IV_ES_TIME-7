package com.example.aplicativo_horacerta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.aplicativo_horacerta.network.Resultado
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date


import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.core.content.ContextCompat.startActivity
import com.example.aplicativo_horacerta.network.Parceiro
import com.example.aplicativo_horacerta.network.PedidoDeCriarMedicamento
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Serializable // Necessário para passar o objeto Medicamento

import com.example.aplicativo_horacerta.FazerLoginActivity
import com.example.aplicativo_horacerta.FazerLoginActivity.Companion.KEY_USER_UID
import com.example.aplicativo_horacerta.network.Comunicado
import com.example.aplicativo_horacerta.network.ComunicadoJson
import com.example.aplicativo_horacerta.network.PedidoDeListarMedicamentos
import com.google.gson.annotations.SerializedName

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aplicativo_horacerta.network.PedidoDeDeletarMedicamento

class HomeCuidadorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userUid = intent.getStringExtra(FazerLoginActivity.KEY_USER_UID)
        val profileType = intent.getStringExtra(FazerLoginActivity.KEY_PROFILE_TYPE)

        if (userUid == null || profileType == null) {
            // Caso de falha: Redirecionar para o login
            Toast.makeText(this, "Erro: Falha na passagem de dados de sessão.", Toast.LENGTH_LONG).show()
            // Opcional: Redirecionar para FazerLoginActivity ou fechar
            finish()
            return
        }
        Toast.makeText(this, "Home do $profileType. UID: $userUid", Toast.LENGTH_SHORT).show()
        setContent {
            Surface(color = Color.Black) {
                HomeCuidador(
                    userId = userUid,
                    onAccessibilityClick = {
                        val intent = Intent(this, AcessibilidadeActivity::class.java)
                        startActivity(intent)
                    },
                    onLogoutClick = { performLogout() }
                )
            }
        }
    }

    // fun logout
    fun performLogout() {
        // Remove a sessão do Firebase
        FirebaseAuth.getInstance().signOut()

        // Limpa os dados do SharedPreferences
        val prefs = getSharedPreferences(FazerLoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Redireciona para a tela de Login
        val intent = Intent(this, FazerLoginActivity::class.java)
        // Adiciona flags para limpar o histórico de atividades
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}



data class Medicamento(
    val id: String,
    val nome: String,
    val tomou: Boolean,
    @SerializedName("dia") val data: String,
    val horario: String,
    val descricao: String,
    val idUsuario: String
) : Serializable

data class ResultadoListaMedicamentos(
    @SerializedName("tipo") val tipo: String = "ResultadoListaMedicamentos",
    @SerializedName("medicamentos") val medicamentos: List<Medicamento>?
) : Comunicado()

data class WrapperResposta(
    @SerializedName("operacao") val operacaoJsonString: String
)

suspend fun performListarMedicamentos(userId: String): ResultadoListaMedicamentos? {

    val SERVER_IP = "10.0.116.3"
    //val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val CODIFICACAO = Charsets.UTF_8

    val pedido = PedidoDeListarMedicamentos(userId)
    val gson = Gson()

    return withContext(Dispatchers.IO) {
        var conexao: Socket? = null
        try {
            conexao = Socket(SERVER_IP, SERVER_PORT)
            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), CODIFICACAO))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream(), CODIFICACAO))
            val servidor = Parceiro(conexao, receptor, transmissor)
            servidor.receba(pedido)
            val respostaComunicado: Any? = servidor.envie()

            conexao.close()
            conexao = null

            if (respostaComunicado is ComunicadoJson) {
                val wrapperJson = respostaComunicado.json
                val wrapper = gson.fromJson(wrapperJson, WrapperResposta::class.java)
                val jsonStringAninhada = wrapper.operacaoJsonString
                val resultadoFinal = gson.fromJson(jsonStringAninhada, ResultadoListaMedicamentos::class.java)
                Log.d("Network", "JSON Interno Final: $jsonStringAninhada")
                return@withContext resultadoFinal

            } else {
                Log.e("Network", "Resposta inesperada: Não é um ComunicadoJson.")
                null
            }

        } catch (e: Exception) {
            Log.e("Network", "Erro fatal ao listar medicamentos (chegou ao parsing):", e)
            e.printStackTrace()
            null
        } finally {
            conexao?.close()
        }
    }
}

data class HistoricoMedicamento(
    val id: String,
    val nome: String,
    val tomou: Boolean,
    val data: String,
    val horario: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeCuidador(
    userId: String?,
    initialTabIndex: Int = 0,
    onAccessibilityClick: () -> Unit = {},
    onLogoutClick: () -> Unit
) {

    val pagerState = rememberPagerState(initialPage = initialTabIndex) { 2 }
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val scope = rememberCoroutineScope()

    val tabBarColor = Color(0xFFEEEEEE)
    val contentColor = Color.White

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    val context = LocalContext.current
    val medicamentosList = remember { mutableStateListOf<Medicamento>() }
    val historicoList = remember { mutableStateListOf<HistoricoMedicamento>() }
    var carregamentoFalhou by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (userId != null) {
                    scope.launch {
                        val resultado = performListarMedicamentos(userId)
                        if (resultado?.medicamentos != null) {

                            // formatar data
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("pt", "BR"))
                            val agora = java.util.Calendar.getInstance().time

                            // Limpar lista
                            medicamentosList.clear()
                            historicoList.clear()

                            // Separar em (futuros e passados)
                            val (futuros, passados) = resultado.medicamentos.partition { remedio ->
                                try {
                                    val dataHoraRemedio = sdf.parse("${remedio.data} ${remedio.horario}")
                                    dataHoraRemedio != null && dataHoraRemedio.after(agora)
                                } catch (e: Exception) {
                                    false
                                }
                            }

                            // Preencher lista de medicamentos, ordenar por data
                            medicamentosList.addAll(futuros.sortedBy {
                                try { sdf.parse("${it.data} ${it.horario}") } catch(e:Exception) { null }
                            })

                            // Preencher lista do Histórico
                            passados.forEach { remedioPassado ->
                                historicoList.add(
                                    HistoricoMedicamento(
                                        id = remedioPassado.id,
                                        nome = remedioPassado.nome,
                                        tomou = remedioPassado.tomou,
                                        data = remedioPassado.data,
                                        horario = remedioPassado.horario
                                    )
                                )
                            }

                        } else {
                            // Opcional: Toast de erro
                        }
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Scaffold(
        topBar = {
            Column {
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
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onLogoutClick,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Sair e Fazer Login",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = tabBarColor,
                    contentColor = Color.Black,
                    indicator = { tabPositions ->
                        val currentTabPosition = tabPositions[selectedTabIndex]

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentSize(Alignment.BottomStart)
                                .offset(x = currentTabPosition.left)
                                .width(currentTabPosition.width)
                                .height(3.dp)
                                .background(Color.Black)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("MEDICAMENTOS", fontSize = 18.sp) },
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("HISTÓRICO", fontSize = 18.sp) },
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Gray
                    )
                }
            }
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(
                    onClick = onAccessibilityClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Acessibilidade",
                        modifier = Modifier.size(100.dp),
                        tint = Color.Black
                    )
                }
                IconButton(
                    onClick = {
                        val intent = Intent(context, CuidadorConectarIdosoActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Conectar a Idoso",
                        modifier = Modifier.size(100.dp),
                        tint = Color.Black
                    )
                }
            }
        },

        floatingActionButtonPosition = FabPosition.Center

    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(contentColor)
        ) { page ->
            when (page) {
                0 -> MedicamentosTab(
                    userId = userId ?: "",
                    medicamentosList = medicamentosList,
                    onRemoveMedicamento = { medicamento ->
                        scope.launch {
                            // userId deve ser passado para a função de deleção!
                            val usuarioId = userId ?: return@launch // Garante que o ID não é nulo antes de prosseguir

                            val resultado = performDeleteMedicamento(medicamento.id.toString(), usuarioId)

                            // Se o servidor retornar ResultadoOperacao(isSucesso = true), esta condição passa
                            if (resultado?.isSucesso == true) {
                                // **Isto remove o item da UI e resolve o seu problema de sincronização**
                                medicamentosList.remove(medicamento)
                                Toast.makeText(context, "Medicamento deletado!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Usa a mensagem de erro da resposta (ou uma genérica se for nula)
                                val mensagemErro = resultado?.mensagem ?: "Erro desconhecido ao deletar."
                                Toast.makeText(context, mensagemErro, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
                1 -> HistoricoTab(historicoList =  historicoList)
            }
        }
    }
}

data class ResultadoOperacao(
    @SerializedName("tipo")
    val tipo: String,

    @SerializedName("sucesso")
    val isSucesso: Boolean,

    @SerializedName("mensagem")
    val mensagem: String? = null
) : Comunicado()

// Função de rede para deletar medicamento
suspend fun performDeleteMedicamento(
    idMedicamento: String,
    idUsuario: String?
): ResultadoOperacao? {

    val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val CODIFICACAO = Charsets.UTF_8

    val pedido = PedidoDeDeletarMedicamento(idMedicamento, idUsuario)
    val gson = Gson()

    return withContext(Dispatchers.IO) {
        var conexao: Socket? = null
        try {
            // Inicializa a conexão
            conexao = Socket(SERVER_IP, SERVER_PORT)

            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), CODIFICACAO))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream(), CODIFICACAO))
            val servidor = Parceiro(conexao, receptor, transmissor)

            // Envia pedido
            servidor.receba(pedido)
            // FORÇA O ENVIO DO PEDIDO ANTES DE TENTAR LER A RESPOSTA
            transmissor.flush()

            // Aguarda e recebe a resposta do servidor (ComunicadoJson)
            val respostaComunicado: Any? = servidor.envie() // Lê resposta

            if (respostaComunicado == null) {
                Log.e("DeleteNetwork", "Erro: Resposta do servidor é NULL.")
                return@withContext ResultadoOperacao(tipo = "ErroCliente", isSucesso = false, mensagem = "Resposta vazia do servidor.")
            }

            if (respostaComunicado is ComunicadoJson) {
                val wrapperJson = respostaComunicado.json
                val wrapper = gson.fromJson(wrapperJson, WrapperResposta::class.java)
                val jsonStringAninhada = wrapper.operacaoJsonString

                // Desserializa o resultado final
                val resultadoFinal = gson.fromJson(jsonStringAninhada, ResultadoOperacao::class.java)

                Log.d("DeleteNetwork", "Resultado Final (isSucesso): ${resultadoFinal.isSucesso}")
                return@withContext resultadoFinal

            } else {
                Log.e("DeleteNetwork", "Erro: Resposta recebida não é um ComunicadoJson.")
                return@withContext ResultadoOperacao(tipo = "ErroCliente", isSucesso = false, mensagem = "Formato de resposta inesperado.")
            }

        } catch (e: Exception) {
            Log.e("DeleteNetwork", "Erro GERAL de rede ou desserialização:", e)
            return@withContext ResultadoOperacao(tipo = "ErroComunicação", isSucesso = false, mensagem = "Erro de comunicação: ${e.message}")
        } finally {
            conexao?.close()
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
@Composable
fun MedicamentosTab(
    userId: String,
    medicamentosList: List<Medicamento>,
    onRemoveMedicamento: (Medicamento) -> Unit
) {
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var medicamentoParaDeletar by remember { mutableStateOf<Medicamento?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = {
                val intent = Intent(context, RemédioCriarActivity::class.java)
                intent.putExtra(KEY_USER_UID, userId)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = "adicionar novo medicamento +",
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
                fontSize = 18.sp
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(medicamentosList) { medicamento ->
                MedicamentoItem(
                    medicamento = medicamento,
                    onDeleteClick = {
                        medicamentoParaDeletar = medicamento
                        showDeleteDialog = true
                    },
                    onEditClick = {
                        val intent = Intent(context, RemédioEditarActivity::class.java)
                        intent.putExtra("DADOS_MEDICAMENTO", medicamento as Serializable)
                        intent.putExtra(KEY_USER_UID, userId)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    if (showDeleteDialog && medicamentoParaDeletar != null) {
        DeleteConfirmationDialog(
            medicamentoNome = medicamentoParaDeletar!!.nome,
            onConfirmDelete = {
                onRemoveMedicamento(medicamentoParaDeletar!!)
                showDeleteDialog = false
                medicamentoParaDeletar = null
            },
            onDismiss = {
                showDeleteDialog = false
                medicamentoParaDeletar = null
            }
        )
    }
}

////////////////////////////////////////////////////////////////////////////////
@Composable
fun DeleteConfirmationDialog(
    medicamentoNome: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Confirmar Exclusão")
        },
        text = {
            Text(text = "Você tem certeza que deseja deletar o medicamento \"$medicamentoNome\"?")
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Deletar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun MedicamentoItem(
    medicamento: Medicamento,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F0F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medicamento.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Dia: ${medicamento.data}", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Horário: ${medicamento.horario}", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(28.dp))
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Deletar", tint = Color.Gray, modifier = Modifier.size(28.dp))
            }
        }
    }
}

////////////////////////////////////////////////////////////////////////////////
@Composable
fun HistoricoTab(historicoList: List<HistoricoMedicamento>) {

    // Se a lista estiver vazia, você pode mostrar uma mensagem
    if (historicoList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum remédio no histórico.", color = Color.Gray)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(historicoList) { historicoItem ->
            HistoricoItem(historico = historicoItem)
        }
    }
}

@Composable
fun HistoricoItem(historico: HistoricoMedicamento) {
    val statusText = if (historico.tomou) "Status: Tomou" else "Status: Não Tomou"
    val icon = if (historico.tomou) Icons.Filled.Check else Icons.Filled.Close
    val iconColor = if (historico.tomou) Color.Gray else Color.Red
    val textColor = if (historico.tomou) Color.Gray else Color.Unspecified

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = historico.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Dia: ${historico.data}", fontSize = 14.sp, color = textColor)
                Text(text = "Horário: ${historico.horario}", fontSize = 14.sp, color = textColor)
                Text(text = statusText, fontSize = 14.sp, color = textColor)
            }
            Icon(
                imageVector = icon,
                contentDescription = statusText,
                tint = iconColor,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

/*
@Preview(showSystemUi = true, showBackground = true, name = "Aba Medicamentos")
@Composable
fun PreviewHomeCuidador() {
    Surface(color = Color.White) {
        // Passa uma função vazia para o Logout no Preview
        HomeCuidador(initialTabIndex = 0, onLogoutClick = {})
    }
}
@Preview(showSystemUi = true, showBackground = true, name = "Aba Histórico")
@Composable
fun PreviewHomeCuidadorHistorico() {
    Surface(color = Color.White) {
        // Passa uma função vazia para o Logout no Preview
        HomeCuidador(initialTabIndex = 1, onLogoutClick = {})
    }
}
*/