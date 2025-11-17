package com.example.aplicativo_horacerta

import android.R.id.message
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.example.aplicativo_horacerta.network.Parceiro
import com.example.aplicativo_horacerta.network.PedidoDeLogin
import com.example.aplicativo_horacerta.network.ResultadoLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.net.Socket

import androidx.lifecycle.lifecycleScope

// Outras importações
import com.google.firebase.auth.FirebaseAuth



class FazerLoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth


    private fun handleLogin(email: String, senha: String) {


        auth.signInWithEmailAndPassword(email.trim(), senha.trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val firebaseUid = user?.uid

                    if (firebaseUid != null) {
                        Toast.makeText(this, "Login Firebase OK. Buscando Perfil...", Toast.LENGTH_SHORT).show()

                        // 3. SE O FIREBASE VALIDOU, CHAMA O SERVIDOR COM O UID
                        lifecycleScope.launch {

                            doLogin(email.trim(), firebaseUid) { result ->

                                val context = this@FazerLoginActivity

                                if (result.startsWith("SUCESSO")) {
                                    // 4. Extração de Dados do Servidor: SUCESSO:UID:Tipo
                                    val parts = result.split(":")
                                    if (parts.size >= 3) {
                                        val uid = parts[1]
                                        val profileType = parts[2]

                                        Toast.makeText(context, "Login OK. Perfil: $profileType", Toast.LENGTH_LONG).show()

                                        // 5. NAVEGAÇÃO CONDICIONAL (exemplo simplificado)
                                        if (profileType == "Cuidador") {
                                            val intent = Intent(context, HomeCuidadorActivity::class.java)
                                            // Opcional: passar dados para a próxima tela
                                            intent.putExtra("USER_UID", uid)
                                            intent.putExtra("PROFILE_TYPE", profileType)

                                            startActivity(intent)
                                            finish()
                                        } else {
                                            // TODO: Navegar para HomeIdosoActivity
                                            Toast.makeText(context, "Perfil Idoso detectado. Navegação pendente.", Toast.LENGTH_LONG).show()
                                        }

                                    } else {
                                        Toast.makeText(context, "Erro: Resposta do servidor incompleta.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    // 6. Tratamento de Falha de Rede/Servidor
                                    val errorMessage = when {
                                        result.startsWith("FALHA:") -> "Servidor: " + result.substringAfter("FALHA:").trim()
                                        result.startsWith("ERRO_CONEXAO:") -> "Falha na conexão com o servidor. Tente novamente."
                                        else -> "Erro desconhecido: $result"
                                    }
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Erro Firebase: UID não disponível.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // 7. TRATAMENTO DE FALHA DE AUTENTICAÇÃO FIREBASE (Email/Senha Inválidos)
                    Toast.makeText(this, "Falha no Login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 8. Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()

        setContent {
            Surface(color = Color.Black) {
                FazerLogin(
                    onLoginAttempt = { email, senha ->
                        handleLogin(email, senha)
                    },
                    // ... outras funções de navegação
                )
            }
        }
    }
}

// Função suspensa para realizar o Login via Socket
suspend fun doLogin(email: String, firebaseUid: String, onResult: (String) -> Unit) {

    val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000 // Verifique a porta correta do seu servidor

    // ✅ CORREÇÃO APLICADA: Cria o PedidoDeLogin com email e UID
    val pedido = PedidoDeLogin(email, firebaseUid)

    withContext(Dispatchers.IO) {
        var servidor: Parceiro? = null // Assumindo que Parceiro é sua classe de Socket
        try {
            val conexao = Socket(SERVER_IP, SERVER_PORT)

            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), Charsets.UTF_8))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream(), Charsets.UTF_8))

            // Assumindo que Parceiro é sua classe de wrapper de comunicação Socket
            servidor = Parceiro(conexao, receptor, transmissor)

            servidor.receba(pedido)

            val resposta = servidor.envie() // Assumindo que envie() retorna o ComunicadoJson

            // Aqui você deve tratar a resposta do servidor (ResultadoLogin ou ResultadoOperacao)
            // Se a resposta for um String (como no seu código original), use-a diretamente
            val result = resposta?.toString() ?: "ERRO_CONEXAO:Resposta nula"

            // O resultado é passado de volta para a Activity na thread principal
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult("ERRO_CONEXAO:" + e.message)
            }
        } finally {
            servidor?.adeus() // Garante que a conexão é fechada
        }
    }
}

@Composable
fun FazerLogin(
    onLoginAttempt: (String, String) -> Unit,
    onConfirmClick: (String, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    val isInPreview = LocalInspectionMode.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }

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
            /*
            // Mensagem de erro
            if (onLoginAttempt != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = loginMessage,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

             */
            Spacer(modifier = Modifier.height(50.dp))

            // Botão de Confirmar
            Button(
                onClick = {
                    var isValid = true
                    if (email.isBlank()) {
                        emailError = "Email obrigatório"
                        isValid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Senha obrigatória"
                        isValid = false
                    }

                    if (isValid) {
                        if (!isInPreview) {
                            // 1. Chama a função da Activity para iniciar o processo de login
                            onLoginAttempt(email.trim(), password.trim())
                        } else {
                            message = "Modo Preview: Rede desativada."
                        }
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
/*
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewFazerLogin() {
    Surface(color = Color.Black) {
        FazerLogin(loginMessage = "Email ou senha incorretos.", onBackClick = {})
    }
}

 */