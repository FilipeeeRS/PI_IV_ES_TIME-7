package com.example.aplicativo_horacerta

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aplicativo_horacerta.socket.MedicamentoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

@Composable
fun HomeIdosoScreen(onLogoutClick: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var nomeCuidador by remember { mutableStateOf("Buscando...") }
    val listaRemediosFuturos = remember { mutableStateListOf<Medicamento>() }

    // Função que carrega e filtra
    fun carregarDados() {
        scope.launch {
            // Busca cuidador usando o repositório
            val dadosCuidador = MedicamentoRepository.buscarDadosCuidador()

            if (dadosCuidador != null) {
                nomeCuidador = dadosCuidador.first
                val uidCuidador = dadosCuidador.second

                // Sincroniza alarme
                MedicamentoRepository.sincronizarRemediosEAgendar(context, uidCuidador)

                // Busca lista para mostrar na tela
                val resultado = MedicamentoRepository.performListarMedicamentos(uidCuidador)

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

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "IDOSO",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cuidador: $nomeCuidador",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

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
            }
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