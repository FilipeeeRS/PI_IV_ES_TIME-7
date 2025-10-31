package com.example.aplicativo_horacerta

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class FazerRegistroActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        setContent {
            Surface(color = Color.Black) {
                FazerRegistro(
                    onBackClick = { finish() },
                    onNavigateToLogin = {
                        startActivity(Intent(this, FazerLoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

/** Cria a conta e retorna mensagem de sucesso/erro pelo callback */
fun createAccount(
    name: String,
    email: String,
    password: String,
    context: android.content.Context,
    onResult: (success: Boolean, message: String) -> Unit
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                // Salva dados básicos já com emailVerified=false
                val userMap = hashMapOf(
                    "nome" to name,
                    "email" to email,
                    "androidId" to androidId,
                    "emailVerified" to (user?.isEmailVerified == true)
                )

                if (userId != null) {
                    db.collection("usuarios").document(userId)
                        .set(userMap)
                        .addOnFailureListener { e ->
                            // Log simples
                            println("Erro ao salvar dados: ${e.message}")
                        }
                }

                // Envia verificação
                user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                    if (verifyTask.isSuccessful) {
                        onResult(true, "Conta criada! Enviamos um e-mail de verificação.")
                    } else {
                        onResult(true, "Conta criada, mas houve falha ao enviar o e-mail de verificação. Você pode tentar novamente pelo app.")
                    }
                }
            } else {
                val exception = task.exception
                val error = when ((exception as? FirebaseAuthException)?.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está em uso."
                    "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                    "ERROR_WEAK_PASSWORD" -> "A senha deve ter pelo menos 6 caracteres."
                    else -> "Erro ao criar conta: ${exception?.localizedMessage ?: "tente novamente"}"
                }
                onResult(false, error)
            }
        }
}

@Composable
fun FazerRegistro(
    onBackClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var uiMessage by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var showVerificationDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    fun isValidEmail(s: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()

    Box(Modifier.fillMaxSize()) {
        // Fundo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Fundo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Voltar
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

            Spacer(Modifier.height(125.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo HoraCerta",
                modifier = Modifier.size(150.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "HoraCerta\nFazer Registro",
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(50.dp))

            // Nome
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Nome completo") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = nameError != null
                )
                AnimatedVisibility(nameError != null) {
                    Text(nameError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Email
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (it.isNotBlank()) emailError = null
                    },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = emailError != null
                )
                AnimatedVisibility(emailError != null) {
                    Text(emailError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Senha
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (it.isNotBlank()) passwordError = null
                    },
                    label = { Text("Senha") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = passwordError != null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha",
                                tint = Color.Gray
                            )
                        }
                    }
                )
                AnimatedVisibility(passwordError != null) {
                    Text(passwordError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Confirmar Senha
            Column {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (it.isNotBlank()) confirmPasswordError = null
                    },
                    label = { Text("Confirmar senha") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = confirmPasswordError != null,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Ocultar senha" else "Mostrar senha",
                                tint = Color.Gray
                            )
                        }
                    }
                )
                AnimatedVisibility(confirmPasswordError != null) {
                    Text(confirmPasswordError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(50.dp))

            // Botão Registrar
            Button(
                onClick = {
                    // Validações básicas
                    var ok = true
                    if (name.isBlank()) { nameError = "Nome obrigatório"; ok = false }
                    if (email.isBlank()) { emailError = "Email obrigatório"; ok = false }
                    else if (!isValidEmail(email.trim())) { emailError = "Formato de e-mail inválido"; ok = false }
                    if (password.isBlank()) { passwordError = "Senha obrigatória"; ok = false }
                    else if (password.length < 6) { passwordError = "Mínimo de 6 caracteres"; ok = false }
                    if (confirmPassword.isBlank()) { confirmPasswordError = "Confirme a senha"; ok = false }
                    else if (password != confirmPassword) { confirmPasswordError = "As senhas não coincidem"; ok = false }

                    if (!ok) return@Button

                    loading = true
                    createAccount(name.trim(), email.trim(), password, context) { success, msg ->
                        loading = false
                        uiMessage = msg
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

                        if (success) {
                            showVerificationDialog = true
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.width(220.dp).height(70.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Text("Registrar", color = Color.Black, fontSize = 22.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Já tem uma conta? ", color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Entrar",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }
        }

        // Dialog de verificação
        if (showVerificationDialog) {
            AlertDialog(
                onDismissRequest = { /* impede fechar tocando fora */ },
                confirmButton = {
                    TextButton(onClick = onNavigateToLogin) { Text("Ir para o Login") }
                },
                title = { Text("Verifique seu e-mail") },
                text = { Text("Enviamos um link de verificação para o seu e-mail. Valide-o para acessar sua conta.") }
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewFazerRegistro() {
    Surface(color = Color.Black) {
        FazerRegistro()
    }
}