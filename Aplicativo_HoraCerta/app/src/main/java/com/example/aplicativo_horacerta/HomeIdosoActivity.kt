package com.example.aplicativo_horacerta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// IMPORTANTE: Assumimos que 'Parceiro', 'Comunicado', 'PedidoBuscarCuidador', etc.
// estão definidos em Java na pasta 'network'
import com.example.aplicativo_horacerta.network.* import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class HomeIdosoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.Black) {
                HomeIdosoScreen(
                    onLogoutClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, InicioTelaActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

// --- ESTRUTURAS DE DADOS DE MEDICAMENTO ---
data class Medicamentos(
    val id: String,
    val nome: String,
    val tomou: Boolean,
    @SerializedName("dia") val data: String,
    val horario: String,
    val descricao: String,
    val idUsuario: String
)

data class ResultadoListaMedica(
    @SerializedName("tipo") val tipo: String = "ResultadoListaMedicamentos",
    @SerializedName("medicamentos") val medicamentos: List<Medicamentos>?
) : Comunicado()

// CLASSE DE WRAPPER ATUALIZADA PARA WrapperRespostas
data class WrapperRespostas(
    @SerializedName("operacao") val operacaoJsonString: String
)

// --- FUNÇÃO DE REDE: Buscar Nome do Cuidador ---
suspend fun buscarNomeCuidador(onResult: (String) -> Unit) {
    val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val CODIFICACAO = Charsets.UTF_8
    val gson = Gson()
    val emailIdoso = FirebaseAuth.getInstance().currentUser?.email ?: return

    val pedido = PedidoBuscarCuidador(emailIdoso)

    withContext(Dispatchers.IO) {
        var conexao: Socket? = null
        try {
            conexao = Socket(SERVER_IP, SERVER_PORT)

            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), CODIFICACAO))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream(), CODIFICACAO))

            val servidor = Parceiro(conexao, receptor, transmissor)

            servidor.receba(pedido)
            transmissor.flush()

            val jsonString = servidor.envieJson()
            if (jsonString.isNullOrBlank()) {
                withContext(Dispatchers.Main) { onResult("Erro: Resposta vazia") }
                return@withContext
            }

            val resultado = gson.fromJson(jsonString, ResultadoBuscaCuidador::class.java)

            withContext(Dispatchers.Main) {
                onResult(resultado.getNomeCuidador())
            }
        } catch (e: Exception) {
            Log.e("NetworkIdoso", "Erro ao buscar cuidador:", e)
            withContext(Dispatchers.Main) { onResult("Erro conexão") }
        } finally {
            conexao?.close()
        }
    }
}


// --- FUNÇÃO DE REDE: Listar Medicamentos para o Idoso ---
suspend fun performListarMedicamentosIdoso(userId: String): ResultadoListaMedica? {

    val SERVER_IP = "10.0.2.2"
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

            // 1. Envia o pedido
            servidor.receba(pedido)
            transmissor.flush()

            // 2. Aguarda e recebe a resposta
            val respostaComunicado: Any? = servidor.envie()

            if (respostaComunicado is ComunicadoJson) {
                val wrapperJson = respostaComunicado.json

                // Parsing: ComunicadoJson -> WrapperRespostas (CLASSE ATUALIZADA) -> ResultadoListaMedica
                val wrapper = gson.fromJson(wrapperJson, WrapperRespostas::class.java) // ATUALIZADO AQUI
                val jsonStringAninhada = wrapper.operacaoJsonString
                val resultadoFinal = gson.fromJson(jsonStringAninhada, ResultadoListaMedica::class.java)

                Log.d("NetworkIdoso", "JSON Interno Final: $jsonStringAninhada")
                return@withContext resultadoFinal

            } else {
                Log.e("NetworkIdoso", "Resposta inesperada: Não é um ComunicadoJson.")
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e("NetworkIdoso", "Erro fatal ao listar medicamentos:", e)
            return@withContext null
        } finally {
            conexao?.close()
        }
    }
}


// --- COMPOSABLES ---

@Composable
fun HomeIdosoScreen(
    onLogoutClick: () -> Unit = {}
) {
    val cardColor = Color(0xFFEEEEEE)
    val headerHeight = 130.dp
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // VARIÁVEIS DE ESTADO
    var nomeCuidador by remember { mutableStateOf("Buscando...") }
    val medicamentosList = remember { mutableStateListOf<Medicamentos>() }
    var isLoading by remember { mutableStateOf(true) }
    var listaCarregada by remember { mutableStateOf(false) }

    // Função para carregar os medicamentos
    val carregarMedicamentos = {
        if (userId != null) {
            scope.launch {
                isLoading = true
                val resultado = performListarMedicamentosIdoso(userId)
                if (resultado?.medicamentos != null) {
                    medicamentosList.clear()
                    val sorted = resultado.medicamentos.sortedWith(
                        compareBy<Medicamentos> { it.data }.thenBy { it.horario }
                    )
                    medicamentosList.addAll(sorted)
                    listaCarregada = true
                } else {
                    medicamentosList.clear()
                    listaCarregada = false
                }
                isLoading = false
            }
        } else {
            isLoading = false
            listaCarregada = false
        }
    }

    // Carregamento inicial de dados
    LaunchedEffect(Unit) {
        buscarNomeCuidador { nome -> nomeCuidador = nome }
        carregarMedicamentos()
    }

    Scaffold(
        topBar = {
            // Header/TopBar
            Box(
                modifier = Modifier.fillMaxWidth().height(headerHeight)
            ) {
                //
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Fundo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Conteúdo do Header
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "IDOSO",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cuidador: $nomeCuidador",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Sair",
                            tint = Color.White,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Botão Acessibilidade
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, AcessibilidadeActivity::class.java)
                        intent.putExtra("PERFIL_USUARIO", "IDOSO")
                        context.startActivity(intent)
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Acessibilidade",
                        modifier = Modifier.size(30.dp),
                    )
                }

                // Botão Recarregar (Refresh)
                FloatingActionButton(
                    onClick = carregarMedicamentos as () -> Unit,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Recarregar Lista",
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // --- Exibir o conteúdo principal: Próximo Remédio ---
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.padding(top = 100.dp))
                medicamentosList.isEmpty() && listaCarregada -> Text(
                    "Nenhum medicamento registrado.",
                    modifier = Modifier.padding(top = 100.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = Color.Gray
                )
                medicamentosList.isNotEmpty() -> ProximoRemedioCard(
                    proximo = medicamentosList.first(),
                    cardColor = cardColor
                )
                else -> Text(
                    "Falha ao carregar dados.",
                    modifier = Modifier.padding(top = 100.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = Color.Red
                )
            }
        }
    }
}


// --- Card do Próximo Remédio ---
@Composable
fun ProximoRemedioCard(proximo: Medicamentos, cardColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f).heightIn(min = 300.dp, max = 400.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                "PRÓXIMO REMÉDIO",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.Gray.copy(alpha = 0.5f))
            )

            Text(
                proximo.nome,
                fontSize = 45.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                proximo.data,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
            Text(
                "${proximo.horario} HORAS",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}