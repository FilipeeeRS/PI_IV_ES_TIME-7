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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
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

class HomeIdosoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.Black) {
                HomeIdosoScreen(
                    onLogoutClick = {
                        FirebaseAuth.getInstance().signOut()
                        val prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()
                        val intent = Intent(this, InicioTelaActivity::class.java) // Ou FazerLoginActivity::class.java
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

// --- FUNÇÃO DE BUSCAR CUIDADOR ---
suspend fun buscarDadosCuidador(): Pair<String, String>? {
    val SERVER_IP = "10.0.2.2"
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

// --- FUNÇÃO DE TESTE SEM DEPENDER DO AMIGO ---
// --- FUNÇÃO PARA SINCRONIZAR ALARMES ---
suspend fun sincronizarRemediosEAgendar(context: Context, uidCuidador: String) {
    // 1. Reutiliza a função de listar que vocês já criaram para a HomeCuidador
    // (Nota: Certifique-se que performListarMedicamentos está acessível aqui ou copie ela para cá)
    val resultadoLista = performListarMedicamentos(uidCuidador)

    if (resultadoLista?.medicamentos != null) {
        Log.d("SYNC", "Encontrados ${resultadoLista.medicamentos.size} remédios. Agendando...")

        // 2. Para cada remédio, chama o código do seu AMIGO
        for (remedio in resultadoLista.medicamentos) {
            AlarmeActivity.agendar(
                context = context,
                nome = remedio.nome,
                dia = remedio.data,       // Ajuste se o nome do campo for diferente
                horario = remedio.horario, // Formato esperado "14:30"
                descricao = remedio.descricao,
                idUsuario = uidCuidador
            )
        }
    }
}

@Composable
fun HomeIdosoScreen(onLogoutClick: () -> Unit = {}) {
    val context = LocalContext.current
    var nomeCuidador by remember { mutableStateOf("Buscando...") }

    // Lista de remédios para mostrar na tela
    val listaRemedios = remember { mutableStateListOf<Medicamento>() }

    LaunchedEffect(Unit) {
        // 1. Descobre quem cuida de mim
        val dadosCuidador = buscarDadosCuidador()

        if (dadosCuidador != null) {
            nomeCuidador = dadosCuidador.first // Nome
            val uidCuidador = dadosCuidador.second // ID

            // 2. Simula o agendamento (Logs no console)
            sincronizarRemediosEAgendar(context, uidCuidador)

            // 3. Atualiza a lista visual na tela
            val resultado = performListarMedicamentos(uidCuidador)
            if (resultado?.medicamentos != null) {
                listaRemedios.clear()
                listaRemedios.addAll(resultado.medicamentos)
            }
        } else {
            nomeCuidador = "Nenhum vínculo"
        }
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // LAYOUT CORRIGIDO PARA O BOTÃO NÃO SUMIR
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Texto com peso (weight) para ocupar espaço mas deixar sobrar pro botão
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "IDOSO",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Cuidador: $nomeCuidador",
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis // Põe "..." se for muito grande
                        )
                    }

                    // Botão Logout fixo na direita
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Filled.ExitToApp, "Sair", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        },
        floatingActionButton = {
            // ... (Seu FAB)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            if (listaRemedios.isNotEmpty()) {
                // Pega o primeiro remédio da lista para exibir no card grande
                val proximo = listaRemedios[0]
                CardProximoRemedio(nome = proximo.nome, horario = proximo.horario, dia = proximo.data)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Total agendados: ${listaRemedios.size}", color = Color.Gray)
            } else {
                Text("Nenhum remédio agendado", fontSize = 20.sp, color = Color.Gray)
            }
        }
    }
}

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
            Text("PRÓXIMO REMÉDIO", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(nome, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text(dia, fontSize = 20.sp)
            Text(horario, fontSize = 20.sp, color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}