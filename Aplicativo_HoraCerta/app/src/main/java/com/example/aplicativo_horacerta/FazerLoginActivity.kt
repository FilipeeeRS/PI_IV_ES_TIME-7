package com.example.aplicativo_horacerta

import android.content.Context
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
import java.io.OutputStreamWriter
import java.net.Socket
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson


class FazerLoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        const val PREFS_NAME = "AuthPrefs"
        const val KEY_USER_UID = "USER_UID"
        const val KEY_PROFILE_TYPE = "PROFILE_TYPE"
    }

    // Verifica se há uma sessão salvos no SharedPreferences
    private fun checkIfAlreadyLoggedIn() {
        val currentUser = auth.currentUser
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Verifica se o Firebase tem um usuário salvo
        if (currentUser != null && prefs.contains(KEY_PROFILE_TYPE)) {
            val uid = prefs.getString(KEY_USER_UID, null)
            val profileType = prefs.getString(KEY_PROFILE_TYPE, null)

            // Redireciona se os dados estiverem presentes
            if (uid != null && profileType != null) {
                Toast.makeText(this, "Sessão restaurada. Bem-vindo(a) de volta.", Toast.LENGTH_SHORT).show()

                navigateToHome(uid, profileType)
            }
        }
    }

    private fun navigateToHome(uid: String, profileType: String) {
        val nextActivity = when (profileType) {
            "Cuidador" -> HomeCuidadorActivity::class.java
            "Idoso" -> HomeIdosoActivity::class.java
            else -> {
                Toast.makeText(this, "Erro: Perfil inválido ($profileType).", Toast.LENGTH_LONG).show()
                return // Impede a navegação
            }
        }

        val intent = Intent(this, nextActivity)
        intent.putExtra(KEY_USER_UID, uid)
        intent.putExtra(KEY_PROFILE_TYPE, profileType)
        startActivity(intent)
        finish()
    }

    private fun handleLogin(email: String, senha: String) {

        auth.signInWithEmailAndPassword(email.trim(), senha.trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val firebaseUid = user?.uid

                    if (firebaseUid != null) {
                        Toast.makeText(this, "Login Firebase OK. Buscando Perfil...", Toast.LENGTH_SHORT).show()

                        lifecycleScope.launch {

                            doLogin(email.trim(), firebaseUid) { result ->

                                val context = this@FazerLoginActivity

                                if (result.startsWith("SUCESSO")) {
                                    // Extração de dados do servidor
                                    val parts = result.split(":")
                                    if (parts.size >= 3) {
                                        val uid = parts[1]
                                        val profileType = parts[2]


                                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                        prefs.edit().apply {
                                            putString(KEY_USER_UID, uid)
                                            putString(KEY_PROFILE_TYPE, profileType)
                                            apply()
                                        }
                                        Toast.makeText(context, "Login OK. Perfil: $profileType", Toast.LENGTH_LONG).show()
                                        navigateToHome(uid, profileType)

                                    } else {
                                        Toast.makeText(context, "Erro: Resposta do servidor incompleta.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
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
                    Toast.makeText(this, "Falha no Login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        checkIfAlreadyLoggedIn()
        setContent {
            Surface(color = Color.Black) {
                FazerLogin(
                    onLoginAttempt = { email, senha ->
                        handleLogin(email, senha)
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}

// Crie esta Data Class para mapear o JSON externo
data class ComunicadoWrapper(
    val operacao: String
)

// Função  para fazer login pelo Socket
suspend fun doLogin(email: String, firebaseUid: String, onResult: (String) -> Unit) {

    val SERVER_IP = "10.0.116.3"
    //val SERVER_IP = "10.0.2.2"
    val SERVER_PORT = 3000
    val gson = Gson()

    val pedido = PedidoDeLogin(email, firebaseUid)

    withContext(Dispatchers.IO) {
        var servidor: Parceiro? = null
        try {
            val conexao = Socket(SERVER_IP, SERVER_PORT)

            val transmissor = BufferedWriter(OutputStreamWriter(conexao.getOutputStream(), Charsets.UTF_8))
            val receptor = BufferedReader(InputStreamReader(conexao.getInputStream(), Charsets.UTF_8))

            servidor = Parceiro(conexao, receptor, transmissor)

            servidor.receba(pedido)

            val resposta = servidor.envie()
            val jsonBruto = resposta?.toString() ?: ""

            val wrapper = gson.fromJson(jsonBruto, ComunicadoWrapper::class.java)

            val jsonInterno = wrapper.operacao

            val resultadoLogin = gson.fromJson(jsonInterno, ResultadoLogin::class.java)


            val result: String = if (resultadoLogin.isSuccessful) {

                "SUCESSO:${resultadoLogin.getFirebaseUid()}:${resultadoLogin.getProfileType()}"
            } else {
                "FALHA:Login rejeitado pelo servidor. Mensagem: ${resultadoLogin.getMensagem()}"
            }

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        } catch (e: Exception) {

            withContext(Dispatchers.Main) {
                onResult("ERRO_CONEXAO:Falha na rede ou no processamento da resposta: ${e.message}")
            }
        } /*finally {
            servidor?.adeus()
        }

        */
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
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Fundo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo HoraCerta",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "HoraCerta\nFazer Login",
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(50.dp))

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

            /*Mensagem de erro
            if (onLoginAttempt != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = loginMessage,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }*/

            Spacer(modifier = Modifier.height(50.dp))

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

/*@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewFazerLogin() {
    Surface(color = Color.Black) {
        FazerLogin(loginMessage = "Email ou senha incorretos.", onBackClick = {})
    }
}*/