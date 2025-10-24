package com.example.aplicativo_horacerta

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelecionarUsuario(
    onIdosoClick: () -> Unit = {},
    onCuidadorClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fundo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Fundo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(125.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo HoraCerta",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Texto central
            Text(
                text = "Por Favor\nSelecione",
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(70.dp))

            // Botões
            Button(
                onClick = onIdosoClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(70.dp)
            ) {
                Text(
                    text = "IDOSO",
                    color = Color.Black,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCuidadorClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(70.dp)
            ) {
                Text(
                    text = "CUIDADOR",
                    color = Color.Black,
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewSelecionarUsuario() {
    Surface(color = Color.Black) {
        SelecionarUsuario()
    }
}