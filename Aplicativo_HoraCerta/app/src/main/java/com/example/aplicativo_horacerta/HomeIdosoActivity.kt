package com.example.aplicativo_horacerta

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

class HomeIdosoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.Black) {
                HomeIdosoScreen(
                    onLogoutClick = {
                        val intent = Intent(this, InicioTelaActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

// DADOS EXEMPLO
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
    onLogoutClick: () -> Unit = {}
) {
    val cardColor = Color(0xFFEEEEEE)
    val headerHeight = 130.dp
    val context = LocalContext.current

    Scaffold(
        topBar = {
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

                // Conteúdo do Cabeçalho (Logo + Título)
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
                        text = "IDOSO",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Botão de Deslogar
                    IconButton(
                        onClick = onLogoutClick
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Sair / Deslogar",
                            tint = Color.White,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                // Botão Acessibilidade
                IconButton(
                    onClick = {
                        val intent = Intent(context, AcessibilidadeActivity::class.java)
                        intent.putExtra("PERFIL_USUARIO", "IDOSO")
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Acessibilidade",
                        modifier = Modifier.size(100.dp),
                        tint = Color.Black
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                        text = "PRÓXIMO REMÉDIO",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )

                    Text(
                        text = medicamento.nome,
                        fontSize = 55.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = medicamento.dia,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = medicamento.horario,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, name = "Home Idoso")
@Composable
fun PreviewHomeIdosoScreen() {
    HomeIdosoScreen()
}