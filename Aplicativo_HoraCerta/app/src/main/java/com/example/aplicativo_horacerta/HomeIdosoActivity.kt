package com.example.aplicativo_horacerta

import android.content.Context
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.aplicativo_horacerta.network.* import com.google.firebase.auth.FirebaseAuth
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

// --- FUNÇÃO DE REDE ---
suspend fun buscarNomeCuidador(onResult: (String) -> Unit) {
    val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val gson = Gson()
    val emailIdoso = FirebaseAuth.getInstance().currentUser?.email ?: return

    withContext(Dispatchers.IO) {
        var servidor: Parceiro? = null
        try {
            val conexao = Socket(SERVER_IP, SERVER_PORT)
            servidor = Parceiro(conexao,
                BufferedReader(InputStreamReader(conexao.getInputStream(), Charsets.UTF_8)),
                BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), Charsets.UTF_8))
            )

            // Envia pedido
            servidor.receba(PedidoBuscarCuidador(emailIdoso))

            // Recebe resposta (JSON Bruto)
            val jsonString = servidor.envieJson()
            val resultado = gson.fromJson(jsonString, ResultadoBuscaCuidador::class.java)

            withContext(Dispatchers.Main) {
                onResult(resultado.getNomeCuidador())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) { onResult("Erro conexão") }
        } finally {
            servidor?.adeus()
        }
    }
}

// DADOS EXEMPLO
data class ProximoMedicamento(
    val nome: String,
    val dia: String,
    val horario: String
)

val sampleProximoRemedio = ProximoMedicamento(
    nome = "Buscopan",
    dia = "SEGUNDA-FEIRA\n25/07",
    horario = "13:00 HORAS"
)

@Composable
fun HomeIdosoScreen(
    medicamento: ProximoMedicamento = sampleProximoRemedio,
    onLogoutClick: () -> Unit = {}
) {
    val cardColor = Color(0xFFEEEEEE)
    val headerHeight = 130.dp
    val context = LocalContext.current

    // VARIAVEL QUE GUARDA O NOME DO CUIDADOR
    var nomeCuidador by remember { mutableStateOf("Buscando...") }

    // CHAMA O SERVIDOR ASSIM QUE ABRE A TELA
    LaunchedEffect(Unit) {
        buscarNomeCuidador { nome ->
            nomeCuidador = nome
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Fundo",
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

                    Column {
                        Text(
                            text = "IDOSO",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // MOSTRA O NOME AQUI
                        Text(
                            text = "Cuidador: $nomeCuidador",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    BotaoLogoutSeguro(
                        context = context,
                        modifier = Modifier.align(Alignment.CenterVertically), // Isso alinha ele na altura correta
                        onLogoutSuccess = {
                            val intent = Intent(context, FazerLoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            // ... (O resto do seu código continua igual) ...
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(
                    onClick = {
                        val intent = Intent(context, AcessibilidadeActivity::class.java)
                        intent.putExtra("PERFIL_USUARIO", "IDOSO")
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Acessibilidade",
                        modifier = Modifier.size(100.dp),
                        tint = Color.Black
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(400.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text("PRÓXIMO REMÉDIO", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                    HorizontalDivider(modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.Gray.copy(alpha = 0.3f)))

                    // DADOS AINDA FAKE DO REMÉDIO
                    Text(medicamento.nome, fontSize = 55.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                    Text(medicamento.dia, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                    Text(medicamento.horario, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun BotaoLogoutSeguro(
    context: Context,
    modifier: Modifier = Modifier,
    onLogoutSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    // Estados
    var mostrarAlerta by remember { mutableStateOf(false) }
    var emailDigitado by remember { mutableStateOf("") }
    var erroValidacao by remember { mutableStateOf(false) }

    // O Ícone na Barra
    IconButton(
        onClick = { mostrarAlerta = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.ExitToApp,
            contentDescription = "Sair com Segurança",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }

    if (mostrarAlerta) {
        AlertDialog(
            onDismissRequest = { mostrarAlerta = false },
            title = { Text(text = "Sair da Conta") },
            text = {
                Column {
                    Text("Para segurança, confirme o e-mail:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailDigitado,
                        onValueChange = {
                            emailDigitado = it
                            erroValidacao = false
                        },
                        label = { Text("E-mail") },
                        isError = erroValidacao,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (erroValidacao) {
                        Text("E-mail incorreto!", color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val emailReal = auth.currentUser?.email
                        if (emailDigitado.trim().equals(emailReal, ignoreCase = true)) {
                            mostrarAlerta = false
                            val prefs = context.getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                            prefs.edit().clear().apply()
                            auth.signOut()
                            onLogoutSuccess()
                        } else {
                            erroValidacao = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarAlerta = false }) { Text("Cancelar") }
            }
        )
    }
}