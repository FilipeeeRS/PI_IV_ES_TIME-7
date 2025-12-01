package com.example.aplicativo_horacerta

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
import com.example.aplicativo_horacerta.socket.NetworkService
import com.example.aplicativo_horacerta.socket.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class FazerRegistroActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    private fun handleRegistration(
        nome: String,
        email: String,
        senha: String,
        profileType: String
    ) {
        // 1. Cria usuário no Firebase Auth
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val firebaseUid = user?.uid

                    if (firebaseUid != null) {
                        Toast.makeText(
                            this,
                            "Cadastro Completo. Enviando Dados...",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 2. Envia dados para o Backend (Socket/API)
                        lifecycleScope.launch {
                            val userRepository = UserRepository(NetworkService())

                            // O repository retorna a String bruta do servidor
                            val result = userRepository.createAccount(
                                nome.trim(),
                                email.trim(),
                                firebaseUid,
                                profileType
                            )

                            // 3. Processa a resposta JSON do servidor
                            try {
                                val jsonExterno = JSONObject(result)
                                val jsonInternoString = jsonExterno.getString("operacao")
                                val jsonInterno = JSONObject(jsonInternoString)

                                val sucesso = jsonInterno.getBoolean("resultado")

                                if (sucesso) {
                                    Toast.makeText(
                                        this@FazerRegistroActivity,
                                        "Conta Criada Com Sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this@FazerRegistroActivity,
                                        FazerLoginActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@FazerRegistroActivity,
                                        "Falha: Usuário Já Existe Ou Erro No Servidor.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                Toast.makeText(
                                    this@FazerRegistroActivity,
                                    "Erro Ao Processar Resposta: $result",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Erro: Usuario Não Encontrado.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Falha no Registro: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            Surface(color = Color.Black) {
                FazerRegistro(
                    onRegisterAttempt = { nome, email, senha, profileType ->
                        handleRegistration(nome, email, senha, profileType)
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FazerRegistro(
    onRegisterAttempt: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit = {}
) {
    val isInPreview = LocalInspectionMode.current

    // Estados de input
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados de controle e erro
    var message by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val profileOptions = listOf("Cuidador", "Idoso")
    var selectedProfileIndex by remember { mutableStateOf<Int?>(null) }

    var profileTypeError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

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

            Spacer(modifier = Modifier.height(50.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo HoraCerta",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "HoraCerta\nFazer Registro",
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(25.dp))

            // --- Seleção de Perfil ---
            Text(
                text = "   Tipo de Perfil:",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                profileOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        shape = RoundedCornerShape(50.dp),
                        onClick = { selectedProfileIndex = index },
                        selected = index == selectedProfileIndex,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color.White,
                            activeContentColor = Color.Black,
                            inactiveContainerColor = Color.Gray,
                            inactiveContentColor = Color.Black
                        )
                    ) {
                        Text(label, fontSize = 16.sp)
                    }
                }
            }

            if (profileTypeError != null) {
                Text(
                    text = profileTypeError!!,
                    color = Color.Red,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Formulário de Cadastro ---
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Completo:") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
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

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Senha:") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
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

                    // Validações
                    if (name.isBlank()) {
                        nameError = "Nome obrigatório"
                        isValid = false
                    }
                    if (email.isBlank()) {
                        emailError = "Email obrigatório"
                        isValid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Senha obrigatória"
                        isValid = false
                    }
                    if (confirmPassword.isBlank()) {
                        confirmPasswordError = "Confirme a senha"
                        isValid = false
                    } else if (password != confirmPassword) {
                        confirmPasswordError = "As senhas não coincidem"
                        isValid = false
                    }
                    if (selectedProfileIndex == null) {
                        profileTypeError = "Selecione um tipo de perfil"
                        isValid = false
                    } else {
                        profileTypeError = null
                    }

                    if (isValid) {
                        if (!isInPreview) {
                            val selectedProfile = profileOptions[selectedProfileIndex!!]
                            onRegisterAttempt(name.trim(), email.trim(), password, selectedProfile)
                        } else {
                            message = "Modo Preview: Firebase desativado."
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
                    text = "Registrar",
                    color = Color.Black,
                    fontSize = 22.sp
                )
            }
        }
    }
}