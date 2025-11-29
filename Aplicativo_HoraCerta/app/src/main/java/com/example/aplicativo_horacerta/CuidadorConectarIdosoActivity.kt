package com.example.aplicativo_horacerta

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

class CuidadorConectarIdosoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.Black) {
                CuidadorConectarIdosoScreen(onBackClick = { finish() })
            }
        }
    }
}

// Busca o nome
suspend fun buscarIdosoViaSocket(email: String, onResult: (Boolean, String?) -> Unit) {

    val SERVER_IP = "10.0.116.3"
    //val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val gson = Gson()

    withContext(Dispatchers.IO) {
        var servidor: Parceiro? = null
        try {
            val conexao = Socket(SERVER_IP, SERVER_PORT)
            servidor = Parceiro(conexao,
                BufferedReader(InputStreamReader(conexao.getInputStream(), Charsets.UTF_8)),
                BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), Charsets.UTF_8))
            )

            // Manda pedido de busca
            servidor.receba(PedidoBuscarIdoso(email))

            val jsonString = servidor.envieJson()

            // Converte o texto para o objeto certo
            val resultado = gson.fromJson(jsonString, ResultadoBuscaIdoso::class.java)

            withContext(Dispatchers.Main) {
                // Se o servidor mandou true, agora vai chegar true aqui
                onResult(resultado.isEncontrou, resultado.nomeIdoso)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) { onResult(false, null) }
        } finally {
            servidor?.adeus()
        }
    }
}

suspend fun confirmarConexaoViaSocket(emailIdoso: String, onResult: (Boolean, String) -> Unit) {
    val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val gson = Gson()
    val usuarioLogado = FirebaseAuth.getInstance().currentUser
    val emailCuidador = usuarioLogado?.email ?: return

    withContext(Dispatchers.IO) {
        var servidor: Parceiro? = null
        try {
            val conexao = Socket(SERVER_IP, SERVER_PORT)
            servidor = Parceiro(conexao,
                BufferedReader(InputStreamReader(conexao.getInputStream(), Charsets.UTF_8)),
                BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), Charsets.UTF_8))
            )

            servidor.receba(PedidoDeConexao(emailCuidador, emailIdoso))

            // envieJson() pegar o texto BRUTO e não perder dados
            val jsonString = servidor.envieJson()

            val resultado = gson.fromJson(jsonString, ResultadoOperacao::class.java)

            withContext(Dispatchers.Main) {
                val msg = resultado.mensagem ?: "Vinculado com Sucesso!"
                onResult(resultado.isSucesso, msg)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(false, "Erro: ${e.message}") }
        } finally {
            servidor?.adeus()
        }
    }
}

@Composable
fun CuidadorConectarIdosoScreen(onBackClick: () -> Unit) {
    var emailIdoso by remember { mutableStateOf("") }

    var nomeEncontrado by remember { mutableStateOf<String?>(null) }
    var showConfirmacao by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background), // Ajuste se necessário
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
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE)).padding(vertical = 16.dp),
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
                .background(Color.White)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.Start) {

                    Text("ADICIONAR IDOSO", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!showConfirmacao) {
                        TextField(
                            value = emailIdoso,
                            onValueChange = { emailIdoso = it },
                            label = { Text("Email do Idoso") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (emailIdoso.isNotBlank()) {
                                    isLoading = true
                                    scope.launch {
                                        buscarIdosoViaSocket(emailIdoso) { encontrou, nome ->
                                            isLoading = false
                                            if (encontrou && nome != null) {
                                                if (nome.contains("(JÁ POSSUI CUIDADOR)")) {
                                                    Toast.makeText(context, "Este idoso já está sendo monitorado por alguém!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    nomeEncontrado = nome
                                                    showConfirmacao = true
                                                }
                                            } else {
                                                Toast.makeText(context, "Idoso não encontrado!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Preencha o e-mail", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.align(Alignment.CenterHorizontally).height(50.dp).width(150.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Buscar", fontSize = 18.sp)
                        }
                    } else {
                        // Confirmação
                        Text(
                            "Encontramos:",
                            fontSize = 14.sp, color = Color.Gray
                        )
                        Text(
                            nomeEncontrado ?: "",
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Deseja vincular-se a este idoso?",
                            fontSize = 16.sp, color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = {
                                    showConfirmacao = false
                                    nomeEncontrado = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("Cancelar")
                            }
                            Button(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        confirmarConexaoViaSocket(emailIdoso) { sucesso, msg ->
                                            isLoading = false
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            if (sucesso) {
                                                onBackClick()
                                            }
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                modifier = Modifier.width(120.dp)
                            ) {
                                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                else Text("Confirmar")
                            }
                        }
                    }
                }
            }
        }
    }
}