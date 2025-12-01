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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicativo_horacerta.socket.NetworkService
import com.example.aplicativo_horacerta.socket.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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

@Composable
fun CuidadorConectarIdosoScreen(onBackClick: () -> Unit) {
    // Estados da tela
    var emailIdoso by remember { mutableStateOf("") }
    var nomeEncontrado by remember { mutableStateOf<String?>(null) }
    var showConfirmacao by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                        Text(
                            text = "CUIDADOR",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE))
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CONECTAR IDOSO",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = "ADICIONAR IDOSO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!showConfirmacao) {
                        // --- ESTADO DE BUSCA ---
                        TextField(
                            value = emailIdoso,
                            onValueChange = { emailIdoso = it },
                            label = { Text("Email do Idoso") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (emailIdoso.isNotBlank()) {
                                    isLoading = true
                                    scope.launch {
                                        // Busca idoso na API
                                        val userRepository = UserRepository(NetworkService())
                                        val (encontrou, nome) = userRepository.buscarIdoso(emailIdoso)

                                        isLoading = false
                                        if (encontrou && nome != null) {
                                            // Validação: Impede duplicidade de cuidadores
                                            if (nome.contains("(JÁ POSSUI CUIDADOR)")) {
                                                Toast.makeText(
                                                    context,
                                                    "Este idoso já está sendo monitorado por alguém!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                nomeEncontrado = nome
                                                showConfirmacao = true
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Idoso não encontrado!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Preencha o e-mail",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .height(50.dp)
                                .width(150.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text("Buscar", fontSize = 18.sp)
                            }
                        }
                    } else {
                        // --- ESTADO DE CONFIRMAÇÃO ---
                        Text("Encontramos:", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            text = nomeEncontrado ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Deseja vincular-se a este idoso?",
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
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
                                        val emailCuidador = FirebaseAuth.getInstance().currentUser?.email ?: return@launch
                                        val userRepository = UserRepository(NetworkService())

                                        // Realiza o vínculo no banco de dados
                                        val resultado = userRepository.performConexao(emailCuidador, emailIdoso)

                                        isLoading = false
                                        val msg = resultado.mensagem ?: "Operação finalizada."
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

                                        if (resultado.isSucesso) {
                                            onBackClick()
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                modifier = Modifier.width(120.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text("Confirmar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}