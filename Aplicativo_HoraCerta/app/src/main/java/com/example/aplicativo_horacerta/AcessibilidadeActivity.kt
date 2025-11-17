package com.example.aplicativo_horacerta

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextAlign

class AcessibilidadeActivity : ComponentActivity() {
    private var profileType: String = "CUIDADOR"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileType = intent.getStringExtra("PERFIL_USUARIO") ?: "CUIDADOR"
        setContent {
            Surface(color = Color.Black) {
                AcessibilidadeScreen(
                    profileType = profileType,
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun AcessibilidadeScreen(
    profileType: String,
    onBackClick: () -> Unit
) {
    val titleBarColor = Color(0xFFEEEEEE)
    val cardBackgroundColor = Color(0xFFF0F0F0)
    val contentColor = Color.White

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
                        contentDescription = "Fundo do Cabeçalho",
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
                            text = profileType.uppercase(),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(titleBarColor)
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ACESSIBILIDADE",
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
                .background(contentColor)
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
                    modifier = Modifier.size(64.dp),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 400.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    when (profileType.uppercase()) {
                        "CUIDADOR" -> CuidadorAcessibilidadeContent()
                        "IDOSO" -> IdosoAcessibilidadeContent()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Mais funcionalidades, serão adicionadas no futuro.",
                        fontSize = 18.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "DESCONECTAR",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

// Tela cuidador
@Composable
fun CuidadorAcessibilidadeContent() {
    var emailIdoso by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {

        Text("ADICIONAR IDOSO (POR EMAIL)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = emailIdoso,
            onValueChange = { emailIdoso = it },
            label = { Text("Email do Idoso") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Você so pode convidar 1 idoso",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Lógica para enviar o convite por email */ },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Convidar", fontSize = 18.sp,
            )
        }
    }
}

// Tela idoso
@Composable
fun IdosoAcessibilidadeContent() {
}

@Preview(showSystemUi = true, showBackground = true, name = "Acessibilidade (Cuidador)")
@Composable
fun PreviewAcessibilidadeScreenCuidador() {
    Surface(color = Color.White) {
        AcessibilidadeScreen(profileType = "CUIDADOR", onBackClick = {})
    }
}

@Preview(showSystemUi = true, showBackground = true, name = "Acessibilidade (Idoso)")
@Composable
fun PreviewAcessibilidadeScreenIdoso() {
    Surface(color = Color.White) {
        AcessibilidadeScreen(profileType = "IDOSO", onBackClick = {})
    }
}