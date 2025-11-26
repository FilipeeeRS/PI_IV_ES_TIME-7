package com.example.aplicativo_horacerta


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build

import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.aplicativo_horacerta.network.ResultadoOperacao
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmeActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acordarTela()
        tocarAlarme()

        // Recupera os dados que viajaram com o alarme
        val nomeRemedio = intent.getStringExtra("NOME_REMEDIO") ?: "Medicamento"
        val descricao = intent.getStringExtra("DESCRICAO") ?: "Hora do remédio"
        val dia = intent.getStringExtra("DIA")
        val idUsuario = intent.getStringExtra("ID_USUARIO") // <--- NOVO: Precisamos saber quem é o idoso

        setContent {
            Surface(color = Color.White) {
                TelaAlarme(
                    nomeRemedio = nomeRemedio,
                    descrição = descricao,
                    onConfirmar = {
                        pararAlarme()

                        // Se tivermos o ID, tentamos salvar no histórico
                        if (idUsuario != null) {
                            registrarNoHistorico(idUsuario, nomeRemedio, dia ?: "", descricao)
                        } else {
                            Toast.makeText(this, "Erro: Usuário não identificado", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                )
            }
        }
    }

    private fun registrarNoHistorico(idUsuario: String, nomeRemedio: String, dia: String, descricao: String) {

        Toast.makeText(this, "Registrando no histórico...", Toast.LENGTH_SHORT).show()

        // Usamos lifecycleScope para preparar a chamada de rede (que não pode travar a tela)
        lifecycleScope.launch {
            try {
                // TODO: AQUI SEU AMIGO COLOCA A CHAMADA DO RETROFIT
                // Exemplo de como ele vai fazer:
                // val resultado: ResultadoOperacao = ApiService.registrarHistorico(idUsuario, nomeRemedio)

                // MOCK (Simulação enquanto seu amigo não conecta):
                // Vamos fingir que o servidor respondeu usando a classe Java que você mandou
                val respostaDoServidor = ResultadoOperacao(true, "Medicamento registrado com sucesso!")

                if (respostaDoServidor.isSucesso) { // O Kotlin converte getSucesso() para .isSucesso
                    Toast.makeText(this@AlarmeActivity, "Confirmado: ${respostaDoServidor.mensagem}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@AlarmeActivity, "Erro no servidor: ${respostaDoServidor.mensagem}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@AlarmeActivity, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // Fecha a tela independente se deu certo ou errado, para o alarme parar de tocar
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pararAlarme()
    }

    private fun acordarTela() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun tocarAlarme() {
        try {
            val uriAlarme = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, uriAlarme)
            ringtone?.play()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pararAlarme() {
        if (ringtone != null && ringtone!!.isPlaying) {
            ringtone?.stop()
        }
        vibrator?.cancel()
    }

    companion object {
        @SuppressLint("ScheduleExactAlarm")
        fun agendar(
            context: Context,
            nome: String,
            dia: String,
            horario: String,
            descricao: String,
            idUsuario: String
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val partesHorario = horario.split(":")
            if (partesHorario.size < 2) return

            val horaInt = partesHorario[0].toIntOrNull() ?: 0
            val minutoInt = partesHorario[1].toIntOrNull() ?: 0

            val idUnicoAlarme = (nome + horario + idUsuario).hashCode()

            val intent = Intent(context, AlarmeReceiver::class.java).apply {
                putExtra("NOME_REMEDIO", nome)
                putExtra("DESCRICAO", descricao)
                putExtra("DIA", dia)
                putExtra("ID_USUARIO", idUsuario) // <--- Guardamos o ID aqui
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                idUnicoAlarme,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, horaInt)
                set(Calendar.MINUTE, minutoInt)
                set(Calendar.SECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("ALARME", "Agendado: $nome para usuario $idUsuario")
            } catch (e: SecurityException) {
                Log.e("ALARME", "Erro: ${e.message}")
            }
        }
    }
}


class AlarmeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nome = intent.getStringExtra("NOME_REMEDIO")
        val descricao = intent.getStringExtra("DESCRICAO")
        val dia = intent.getStringExtra("DIA")
        val idUsuario = intent.getStringExtra("ID_USUARIO") // <--- Recebemos o ID aqui

        val activityIntent = Intent(context, AlarmeActivity::class.java).apply {
            putExtra("NOME_REMEDIO", nome)
            putExtra("DESCRICAO", descricao)
            putExtra("DIA", dia)
            putExtra("ID_USUARIO", idUsuario) // <--- Passamos para a Activity
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(activityIntent)
    }
}

@Composable
fun TelaAlarme(
    nomeRemedio: String,
    descrição: String,
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