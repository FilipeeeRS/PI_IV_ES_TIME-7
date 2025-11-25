package com.example.aplicativo_horacerta
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AlarmeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Futuramente: código para acordar a tela e tocar som

        val nomeRemedio = intent.getStringExtra("NOME_REMEDIO") ?: "Buscopan"
        // --- CORREÇÃO AQUI: Renomeei para 'descricao' para facilitar ---
        val descricao = intent.getStringExtra("DOSAGEM") ?: "1 Comprimido"

        setContent {
            Surface(color = Color.White) {
                TelaAlarme(
                    nomeRemedio = nomeRemedio,
                    // --- CORREÇÃO AQUI: Agora passamos a variável correta ---
                    descrição = descricao,
                    onConfirmar = {
                        // TODO: Enviar confirmação para o servidor (Socket)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun TelaAlarme(
    nomeRemedio: String,
    descrição: String, // O parâmetro continua com o nome que você escolheu
    onConfirmar: () -> Unit,
) {
    val alertColor = Color(0xFFFF5252)
    val confirmColor = Color(0xFF4CAF50)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Ícone do Alarme
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 50.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = "Alarme",
                tint = alertColor,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HORA DO REMÉDIO!",
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                color = alertColor,
                textAlign = TextAlign.Center,
                lineHeight = 55.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = nomeRemedio,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = descrição,
                    fontSize = 25.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 30.dp)
        ) {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Text(
                    text = "JÁ TOMEI",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewTelaAlarme() {
    TelaAlarme(
        nomeRemedio = "Dipirona",
        descrição = "30 gotas",
        onConfirmar = {},
    )
}