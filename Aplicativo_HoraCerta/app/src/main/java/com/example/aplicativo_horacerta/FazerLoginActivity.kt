package com.example.aplicativo_horacerta

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicativo_horacerta.network.PedidoDeLogin
import com.example.aplicativo_horacerta.network.ResultadoLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class FazerLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.Black) {

                val scope = rememberCoroutineScope()
                var loginMessage by remember { mutableStateOf<String?>(null) }

                FazerLogin(
                    loginMessage = loginMessage,
                    onConfirmClick = { email, password ->
                        loginMessage = null

                        scope.launch {
                            val resultado = performLogin(email, password)

                            when (resultado?.status) {
                                "SUCESSO_CUIDADOR" -> {
                                    val intent = Intent(this@FazerLoginActivity, HomeCuidadorActivity::class.java)
                                    // TODO: Passar os dados do 'resultado.usuario' para a Home
                                    // intent.putExtra("USUARIO_NOME", resultado.usuario?.nome)
                                    startActivity(intent)
                                    finish()
                                    loginMessage = "Login de Cuidador bem-sucedido!"

                                }
                                "SUCESSO_IDOSO" -> {
                                    // TODO: Criar a HomeIdosoActivity
                                    // val intent = Intent(this@FazerLoginActivity, HomeIdosoActivity::class.java)
                                    // startActivity(intent)
                                    // finish()
                                    loginMessage = "Login de Idoso bem-sucedido!"
                                }
                                "ERRO_SENHA" -> {
                                    loginMessage = "Email ou senha incorretos."
                                }
                                "ERRO_USUARIO" -> {
                                    loginMessage = "Usuário não encontrado."
                                }
                                else -> {
                                    loginMessage = "Erro de conexão. Tente novamente."
                                }
                            }
                        }
                    },
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

// Função de login com o servidor
suspend fun performLogin(email: String, senha: String): ResultadoLogin? {

    val SERVER_IP = "10.0.116.3"
    val SERVER_PORT = 3000

    val pedido = PedidoDeLogin(email, senha)

    // Roda a rede em background
    return withContext(Dispatchers.IO) {
        try {
            val socket = Socket(SERVER_IP, SERVER_PORT)

            val oos = ObjectOutputStream(socket.outputStream)
            oos.writeObject(pedido) // Envia o pedido
            oos.flush()

            val ois = ObjectInputStream(socket.inputStream)
            val resposta = ois.readObject() as? ResultadoLogin

            ois.close()
            oos.close()
            socket.close()

            resposta

        } catch (e: Exception) {
            e.printStackTrace()
            null // null = erro
        }
    }
}

@Composable
fun FazerLogin(
    loginMessage: String?, // Recebe erro/sucesso
    onConfirmClick: (String, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    val isInPreview = LocalInspectionMode.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fundo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Fundo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Botão de Voltar
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(100.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo HoraCerta",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Texto central
            Text(
                text = "HoraCerta\nFazer Login",
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Campo de Email
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email:") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Senha
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha:") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, "Mostrar/Esconder Senha")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Mensagem de erro
            if (loginMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = loginMessage,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Botão de Confirmar
            Button(
                onClick = {
                    if (!isInPreview) {
                        onConfirmClick(email.trim(), password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(70.dp)
            ) {
                Text(
                    text = "Confirmar",
                    color = Color.Black,
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewFazerLogin() {
    Surface(color = Color.Black) {
        FazerLogin(loginMessage = "Email ou senha incorretos.", onBackClick = {})
    }
}