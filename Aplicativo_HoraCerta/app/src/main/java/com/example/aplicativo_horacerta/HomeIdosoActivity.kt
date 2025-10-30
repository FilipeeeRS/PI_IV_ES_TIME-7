package com.example.aplicativo_horacerta

import androidx.compose.ui.draw.scale

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip

// Esta Activity precisará ser registrada no AndroidManifest.xml
class HomeIdosoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Define o estado de navegação (tela atual)
            var currentScreen by remember { mutableStateOf<IdosoScreen>(IdosoScreen.Home) }

            Surface(color = Color.Black) {
                when (currentScreen) {
                    IdosoScreen.Home -> HomeIdosoScreen(
                        onSettingsClick = { currentScreen = IdosoScreen.Settings },
                        // Ação de confirmar ingestão (seria integrada com o backend)
                        onConfirmClick = {
                            // Lógica de confirmação aqui.
                            // Por enquanto, apenas um placeholder:
                            println("Medicamento confirmado!")
                        }
                    )
                    IdosoScreen.Settings -> AcessibilidadeIdosoScreen(
                        onBackClick = { currentScreen = IdosoScreen.Home },
                        onLogoutClick = {
                            // TODO: Implementar logout (voltar para a tela inicial de Login/Registro)
                            // Exemplo: startActivity(Intent(this, InicioTelaActivity::class.java))
                            // finish()
                        }
                    )
                }
            }
        }
    }
}

// Classe selada para gerenciar as telas da navegação interna do Idoso (simples)
sealed class IdosoScreen {
    data object Home : IdosoScreen()
    data object Settings : IdosoScreen()
}
//exemplo
data class ProximoMedicamento(
    val nome: String,
    val dia: String,
    val horario: String
)

val sampleProximoRemedio = ProximoMedicamento(
    nome = "Buscopan",
    dia = "SEGUNDA-FEIRA\n25/07",
    horario = "13:00 HORAS"
)


@Composable
fun HomeIdosoScreen(
    medicamento: ProximoMedicamento = sampleProximoRemedio,
    onSettingsClick: () -> Unit,
    onConfirmClick: (ProximoMedicamento) -> Unit
) {
    var isConfirmed by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFF00bcd4) // Cor de destaque (Ciano)
    val cardColor = if (isConfirmed) Color(0xFFE0F7FA) else Color(0xFFEEEEEE)
    val confirmButtonColor = if (isConfirmed) Color.Gray else primaryColor
    val headerHeight = 100.dp // Altura padrão do Box do cabeçalho simples

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Fundo do Cabeçalho",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Mantém a imagem em full screen
            )

            // Conteúdo do Cabeçalho (Logo + Título + Botão de Configurações)
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
                    modifier = Modifier.size(70.dp) // Tamanho ajustado para caber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "IDOSO",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f)) // Empurra o ícone de Settings para a direita

                // Botão de Configurações
                IconButton(
                    onClick = onSettingsClick
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Acessibilidade e Configurações",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = headerHeight, start = 24.dp, end = 24.dp), // Padding superior = altura do cabeçalho
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(400.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = if (isConfirmed) "MEDICAMENTO CONFIRMADO!" else "PRÓXIMO REMÉDIO",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isConfirmed) Color(0xFF00796B) else Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Divider(
                        modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.Gray.copy(alpha = 0.3f))
                    )
                    Text(
                        text = medicamento.nome,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = medicamento.dia,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isConfirmed) "TOMADO AGORA" else medicamento.horario,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    if (!isConfirmed) {
                        isConfirmed = true
                        onConfirmClick(medicamento)
                    }
                },
                modifier = Modifier
                    .width(250.dp)
                    .height(90.dp),
                colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor),
                shape = RoundedCornerShape(16.dp),
                enabled = !isConfirmed
            ) {
                Text(
                    text = if (isConfirmed) "INGESTÃO REALIZADA" else "CONFIRMAR INGESTÃO",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
// --- Preview para Home Idoso ---
@Preview(showSystemUi = true, showBackground = true, name = "Home Idoso")
@Composable
fun PreviewHomeIdosoScreen() {
    HomeIdosoScreen(
        onSettingsClick = {},
        onConfirmClick = {}
    )
}


@Composable
fun AcessibilidadeIdosoScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    val responsavelNumber = "11 3291 2311"
    val primaryColor = Color(0xFF00bcd4)
    val bgColor = if (isDarkTheme) Color.Black else Color.White
    val cardColor = if (isDarkTheme) Color(0xFF222222) else Color(0xFFEEEEEE)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val headerHeight = 100.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
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
                    .padding(horizontal = 8.dp), // Padding ajustado para o IconButton
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(35.dp) // Tamanho ajustado
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))


                Text(
                    text = "ACESSIBILIDADE",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = headerHeight + 20.dp, start = 24.dp, end = 24.dp), // Padding superior ajustado
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Opção de Tema
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tema Escuro:",
                            fontSize = 24.sp,
                            color = textColor
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { isDarkTheme = it },
                            modifier = Modifier.scale(1.5f)
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Número do Responsável
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Contato do Responsável:",
                            fontSize = 24.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = responsavelNumber,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Botão Sair da Área do Idoso
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = onLogoutClick
                        ) {
                            Text(
                                text = "SAIR DA ÁREA DO IDOSO",
                                fontSize = 20.sp,
                                color = Color.Red,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Preview para Acessibilidade Idoso ---
@Preview(showSystemUi = true, showBackground = true, name = "Acessibilidade Idoso")
@Composable
fun PreviewAcessibilidadeIdosoScreen() {
    AcessibilidadeIdosoScreen(
        onBackClick = {},
        onLogoutClick = {}
    )
}