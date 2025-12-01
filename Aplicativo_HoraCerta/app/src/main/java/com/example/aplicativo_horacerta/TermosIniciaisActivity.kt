package com.example.aplicativo_horacerta

import android.content.Intent
import androidx.compose.ui.tooling.preview.Preview
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class TermosIniciaisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verifica se os Termos já foram aceitos
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_done", false)

        if (onboardingDone) {
            // Foi Aceito: continua
            val intent = Intent(this, InicioTelaActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Não foi aceito: mostra os termos
        setContent {
            Surface(color = Color.Black) {
                TermosTela(
                    onAcceptClick = {
                        // Salva a preferência
                        prefs.edit().putBoolean("onboarding_done", true).apply()

                        val intent = Intent(this, InicioTelaActivity::class.java)
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

@Composable
fun TermosTela(
    onAcceptClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val scroll = rememberScrollState()

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
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scroll),
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
                text = "Termos de Uso",
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
            ) {
                Text(
                    text = termosTexto(),
                    color = Color.Black,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onAcceptClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(70.dp)
            ) {
                Text(
                    text = "Aceitar",
                    color = Color.Black,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(25.dp))
        }
    }
}

@Composable
fun termosTexto(): String {
    return """
O HoraCerta é um aplicativo que auxilia no controle de medicamentos, notificando horários e registrando confirmações de uso pelo idoso.

Ao utilizar este aplicativo, você concorda com:

• Permitir o gerenciamento de lembretes de medicação;
• Permitir o acesso pelo cuidador às confirmações realizadas pelo idoso;
• Uso de dados cadastrais para funcionamento do sistema;
• Não compartilhar dados fora do aplicativo sem autorização.

O usuário é responsável por fornecer informações verídicas e manter seus dados de login seguros.

O HoraCerta não substitui recomendações médicas. Caso ocorra alguma dúvida sobre tratamento ou medicamento, consulte um profissional de saúde.

Para mais informações, consulte nossa Política de Privacidade.
""".trimIndent()
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewTermos() {
    Surface(color = Color.Black) {
        TermosTela()
    }
}