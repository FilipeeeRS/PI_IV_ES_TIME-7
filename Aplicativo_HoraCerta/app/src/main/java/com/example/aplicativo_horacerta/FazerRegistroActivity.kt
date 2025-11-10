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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.aplicativo_horacerta.network.PedidoDeCadastro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class FazerRegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.Black) {
                FazerRegistro(
                    onRegisterClick = {
                        // Ação: trocar de tela (login)
                        val intent = Intent(this, FazerLoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

// Cria a conta
suspend fun createAccount(nome: String, email: String, senha: String, profileType: String, onResult: (String) -> Unit) {

    val SERVER_IP = "10.0.116.3"
    val SERVER_PORT = 3000

    val pedido = PedidoDeCadastro(nome, email, senha, profileType)

    // Roda a rede em background
    withContext(Dispatchers.IO) {
        try {
            val socket = Socket(SERVER_IP, SERVER_PORT)

            val oos = ObjectOutputStream(socket.outputStream)
            oos.writeObject(pedido)
            oos.flush()

            val ois = ObjectInputStream(socket.inputStream)
            val resposta = ois.readObject() // TODO: Criar a classe de resposta (???)

            ois.close()
            oos.close()
            socket.close()

            // Manda a resposta de volta
            withContext(Dispatchers.Main) {
                // TODO: Processar a resposta
                onResult("Conta criada!")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult("Erro de conexão: ${e.message}")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FazerRegistro(
    onRegisterClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {

    // Preview
    val isInPreview = LocalInspectionMode.current

    // Guardar o que o usuário digita
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Estado do perfil
    val profileOptions = listOf("Cuidador", "Idoso")
    var selectedProfileIndex by remember { mutableStateOf<Int?>(null) }
    var profileTypeError by remember { mutableStateOf<String?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var showVerificationDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Fundoa
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

            Spacer(modifier = Modifier.height(50.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo HoraCerta",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Texto central
            Text(
                text = "HoraCerta\nFazer Registro",
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Selecionar perfil
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
                        Text(label, fontSize = 16.sp,)
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

            // Campo de Nome
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

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Confirmar Senha
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Senha:") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmPasswordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

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

            // Botão de Registrar
            Button(
                onClick = {
                    var isValid = true

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

                            // Chama a nova função de rede (Socket)
                            scope.launch {
                                createAccount(name.trim(), email.trim(), password, selectedProfile) { result ->
                                    message = result
                                    if (result.contains("Conta criada!")) {
                                        onRegisterClick()
                                    }
                                }
                            }
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewFazerRegistro() {
    Surface(color = Color.Black) {
        FazerRegistro()
    }
}