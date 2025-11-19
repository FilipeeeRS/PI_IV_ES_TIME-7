package com.example.aplicativo_horacerta
import com.example.aplicativo_horacerta.network.ComunicadoJson
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicativo_horacerta.network.Parceiro
import com.example.aplicativo_horacerta.network.PedidoDeConexao
import com.example.aplicativo_horacerta.network.ResultadoOperacao
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

class CuidadorConectarIdosoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.Black) {
                CuidadorConectarIdosoScreen(
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

// Função que faz o trabalho sujo de Rede (Socket)
// Baseada na lógica do doLogin do seu grupo
suspend fun conectarComIdosoViaSocket(emailIdoso: String, onResult: (Boolean, String) -> Unit) {
    val SERVER_IP = "10.0.2.2" // IP padrão do emulador Android para o PC
    val SERVER_PORT = 3000
    val gson = Gson()

    // 1. Pega o email do Cuidador logado no Firebase
    val usuarioLogado = FirebaseAuth.getInstance().currentUser
    val emailCuidador = usuarioLogado?.email

    if (emailCuidador == null) {
        withContext(Dispatchers.Main) {
            onResult(false, "Erro: Cuidador não está logado no Firebase.")
        }
        return
    }

    val pedido = PedidoDeConexao(emailCuidador, emailIdoso)

    withContext(Dispatchers.IO) {
        var servidor: Parceiro? = null
        try {
            // 2. Abre conexão
            val conexao = Socket(SERVER_IP, SERVER_PORT)
            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), Charsets.UTF_8))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream(), Charsets.UTF_8))
            servidor = Parceiro(conexao, receptor, transmissor)

            // 3. Envia o pedido
            servidor.receba(pedido)

// 4. Recebe resposta
            // O servidor retorna 'Comunicado', mas nós sabemos que é um 'ComunicadoJson'
            // Então fazemos o CAST (as ComunicadoJson)
            val respostaComunicado = servidor.envie() as ComunicadoJson

            // AGORA VAI FUNCIONAR: Como convertemos acima, o Kotlin acha o getJson()
            val jsonResposta = respostaComunicado.getJson()

            // Converte JSON para Objeto
            val resultado = gson.fromJson(jsonResposta, ResultadoOperacao::class.java)

            withContext(Dispatchers.Main) {
                val msgSegura = resultado.mensagem ?: "Operação finalizada (sem mensagem do servidor)"
                onResult(resultado.isSucesso, msgSegura)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(false, "Erro de conexão: ${e.message}")
            }
        } finally {
            // 5. Fecha conexão (Igual ao Login)
            servidor?.adeus()
        }
    }
}

@Composable
fun CuidadorConectarIdosoScreen(
    onBackClick: () -> Unit
) {
    var emailIdoso by remember { mutableStateOf("") }

    // Utilitários para exibir mensagens e rodar coroutines
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val titleBarColor = Color(0xFFEEEEEE)
    val contentColor = Color.White
    val cardBackgroundColor = Color(0xFFF0F0F0)
    val buttonColor = Color.Black

    Scaffold(
        topBar = {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Fundo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("CUIDADOR", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(titleBarColor).padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("CONECTAR IDOSO", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(contentColor)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Filled.ArrowBack, "Voltar", modifier = Modifier.size(32.dp), tint = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.Start) {

                    Text("ADICIONAR IDOSO (POR EMAIL)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = emailIdoso,
                        onValueChange = { emailIdoso = it },
                        label = { Text("Email do Idoso") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading, // Trava enquanto carrega
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Ao convidar, verificaremos se o email existe.",
                        fontSize = 14.sp, color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally), textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (emailIdoso.isNotBlank()) {
                                isLoading = true
                                scope.launch {
                                    conectarComIdosoViaSocket(emailIdoso) { sucesso, msg ->
                                        isLoading = false
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        if (sucesso) {
                                            // Se deu certo, pode voltar ou limpar o campo
                                            emailIdoso = ""
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Preencha o e-mail", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.align(Alignment.CenterHorizontally).height(50.dp).width(150.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Convidar", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}