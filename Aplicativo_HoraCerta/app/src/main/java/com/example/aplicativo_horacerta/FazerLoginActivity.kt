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
import androidx.lifecycle.lifecycleScope
import com.example.aplicativo_horacerta.socket.NetworkService // <--- Import
import com.example.aplicativo_horacerta.socket.UserRepository // <--- Import
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FazerLoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        const val PREFS_NAME = "AuthPrefs"
        const val KEY_USER_UID = "USER_UID"
        const val KEY_PROFILE_TYPE = "PROFILE_TYPE"
    }

    private fun checkIfAlreadyLoggedIn() {
        val currentUser = auth.currentUser
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (currentUser != null && prefs.contains(KEY_PROFILE_TYPE)) {
            val uid = prefs.getString(KEY_USER_UID, null)
            val profileType = prefs.getString(KEY_PROFILE_TYPE, null)

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
                return
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
                            // Instancia o repositório (NetworkConfig)
                            val userRepository = UserRepository(NetworkService())

                            // Chama a função de login (suspensa)
                            val result = userRepository.doLogin(email.trim(), firebaseUid)

                            val context = this@FazerLoginActivity

                            if (result.startsWith("SUCESSO")) {
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
                                    result.contains("Connection refused") -> "Servidor offline. Verifique o IP no NetworkConfig."
                                    result.contains("host") -> "Erro de IP. O endereço do servidor está errado."
                                    result.contains("timeout") -> "O servidor demorou muito para responder."
                                    result.startsWith("FALHA:") -> result.substringAfter("FALHA:").trim()
                                    result.startsWith("ERRO_CONEXAO:") -> result.substringAfter("ERRO_CONEXAO:").trim()
                                    else -> "Erro desconhecido. Tente novamente."
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
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