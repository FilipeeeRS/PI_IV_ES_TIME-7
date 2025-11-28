package com.example.aplicativo_horacerta

import android.content.Context
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.aplicativo_horacerta.network.*
import com.google.firebase.auth.FirebaseAuth
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

class HomeIdosoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Fundo geral preto para destacar o conteúdo (igual ao padrão do app)
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

// --- FUNÇÕES DE LÓGICA (MANTIDAS IGUAIS) ---
suspend fun buscarDadosCuidador(): Pair<String, String>? {
    val SERVER_IP = "10.0.116.3" // Mantenha seu IP atualizado
    val SERVER_PORT = 3000
    val gson = Gson()
    val emailIdoso = FirebaseAuth.getInstance().currentUser?.email ?: return null

    return withContext(Dispatchers.IO) {
        var servidor: Parceiro? = null
        try {
            val conexao = Socket(SERVER_IP, SERVER_PORT)
            servidor = Parceiro(conexao,
                BufferedReader(InputStreamReader(conexao.getInputStream(), Charsets.UTF_8)),
                BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), Charsets.UTF_8))
            )
            servidor.receba(PedidoBuscarCuidador(emailIdoso))
            val jsonString = servidor.envieJson()
            val resultado = gson.fromJson(jsonString, ResultadoBuscaCuidador::class.java)

            if (resultado.isEncontrou()) {
                Pair(resultado.getNomeCuidador(), resultado.getUidCuidador())
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            servidor?.adeus()
        }
    }
}

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

// --- COMPOSABLE ATUALIZADO (VISUAL NOVO) ---
@Composable
fun HomeIdosoScreen(onLogoutClick: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var nomeCuidador by remember { mutableStateOf("Buscando...") }
    val listaRemediosFuturos = remember { mutableStateListOf<Medicamento>() }

    // Função que carrega e filtra (mantida da versão anterior para funcionar a fila)
    fun carregarDados() {
        scope.launch {
            val dadosCuidador = buscarDadosCuidador()

            if (dadosCuidador != null) {
                nomeCuidador = dadosCuidador.first
                val uidCuidador = dadosCuidador.second

                sincronizarRemediosEAgendar(context, uidCuidador)
                val resultado = performListarMedicamentos(uidCuidador)

                if (resultado?.medicamentos != null) {
                    listaRemediosFuturos.clear()
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                    val agora = Calendar.getInstance().time

                    val filtrados = resultado.medicamentos.filter { remedio ->
                        try {
                            val dataRemedio = sdf.parse("${remedio.data} ${remedio.horario}")
                            dataRemedio != null && dataRemedio.after(agora)
                        } catch (e: Exception) { false }
                    }.sortedBy { remedio ->
                        sdf.parse("${remedio.data} ${remedio.horario}")
                    }
                    listaRemediosFuturos.addAll(filtrados)
                }
            } else {
                nomeCuidador = "Nenhum vínculo"
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                carregarDados()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            // --- AQUI ESTÁ A MUDANÇA VISUAL (Baseada na HomeCuidador) ---
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    // Imagem de Fundo
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Fundo do Cabeçalho",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Conteúdo do Cabeçalho
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ícone Grande (Igual HomeCuidador)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(100.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Coluna de Texto (Título e Nome do Cuidador)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "IDOSO",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cuidador: $nomeCuidador",
                                color = Color.White.copy(alpha = 0.9f), // Leve transparência
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Botão Sair
                        IconButton(
                            onClick = onLogoutClick,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Sair",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                // (Não coloquei TabRow aqui pois idoso só tem uma função principal)
            }
        }
    ) { paddingValues ->
        // CORPO DA TELA
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (listaRemediosFuturos.isNotEmpty()) {
                val proximo = listaRemediosFuturos[0]
                CardProximoRemedio(nome = proximo.nome, horario = proximo.horario, dia = proximo.data)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Próximos na fila: ${listaRemediosFuturos.size - 1}", color = Color.Gray)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top=50.dp)) {
                    Text("Tudo em dia!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Você não tem remédios pendentes.", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// Mantive o card igual, pois combina com o estilo clean
@Composable
fun CardProximoRemedio(nome: String, horario: String, dia: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text("PRÓXIMO REMÉDIO", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(nome, fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Data: $dia", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(horario, fontSize = 35.sp, color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}