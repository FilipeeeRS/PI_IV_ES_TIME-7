package com.example.aplicativo_horacerta

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.aplicativo_horacerta.socket.MedicamentoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmeActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Garante que a tela ligue e o som toque assim que a Activity abrir
        acordarTela()
        tocarSomEVibrar()

        val nomeRemedio = intent.getStringExtra("NOME_REMEDIO") ?: "Medicamento"
        val descricao = intent.getStringExtra("DESCRICAO") ?: "Hora do remédio"
        val dia = intent.getStringExtra("DIA") ?: ""
        val horario = intent.getStringExtra("HORARIO") ?: ""
        val idUsuario = intent.getStringExtra("ID_USUARIO")

        setContent {
            Surface(color = Color.White) {
                TelaAlarme(
                    nomeRemedio = nomeRemedio,
                    descrição = descricao,
                    onConfirmar = {
                        // Ao confirmar: Para o som, cancela notificação e avisa API
                        pararAlarme()
                        cancelarNotificacao()

                        if (!idUsuario.isNullOrBlank()) {
                            confirmarNoServidor(idUsuario, nomeRemedio, dia, horario)
                        } else {
                            finish()
                        }
                    }
                )
            }
        }
    }

    private fun cancelarNotificacao() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(12345)
            Log.d("ALARME", "Notificação cancelada com sucesso.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun confirmarNoServidor(
        idUsuario: String,
        nomeRemedio: String,
        dia: String,
        horario: String
    ) {
        Toast.makeText(this, "Confirmando...", Toast.LENGTH_SHORT).show()

        // Executa a confirmação em uma corrotina para não travar a UI
        lifecycleScope.launch {
            val sucesso = MedicamentoRepository.performConfirmarAlarme(
                idUsuario,
                nomeRemedio,
                dia,
                horario
            )

            if (sucesso) {
                Toast.makeText(
                    getApplicationContext(),
                    "Confirmado no Servidor!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    getApplicationContext(),
                    "Salvo localmente (Erro Servidor)",
                    Toast.LENGTH_SHORT
                ).show()
            }
            finishAndRemoveTask()
        }
    }

    private fun acordarTela() {
        // Configurações para ligar a tela mesmo se estiver bloqueada
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

    private fun tocarSomEVibrar() {
        try {
            pararAlarme() // Garante que não haja outro som tocando

            val uriAlarme = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, uriAlarme)

            // Loop infinito no Android P ou superior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()

            // Configura vibração (Compatibilidade com novas e velhas APIs)
            val vibratorManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            vibrator = vibratorManager
            // Padrão: Espera 0ms, Vibra 500ms, Pausa 200ms, Vibra 500ms
            vibrator?.vibrate(longArrayOf(0, 500, 200, 500), 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pararAlarme() {
        try {
            if (ringtone != null && ringtone!!.isPlaying) {
                ringtone?.stop()
            }
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pararAlarme()
        cancelarNotificacao()
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
            val calendar = Calendar.getInstance()

            try {
                // Converte Strings de data/hora para objeto Calendar
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                val dataCompleta = format.parse("$dia $horario")

                if (dataCompleta == null) return

                calendar.time = dataCompleta

                // Evita agendar alarmes no passado
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    Log.d("ALARME", "Horário $horario já passou. Ignorando agendamento.")
                    return
                }
            } catch (e: Exception) {
                return
            }

            val intent = Intent(context, AlarmeReceiver::class.java).apply {
                putExtra("NOME_REMEDIO", nome)
                putExtra("DESCRICAO", descricao)
                putExtra("DIA", dia)
                putExtra("HORARIO", horario)
                putExtra("ID_USUARIO", idUsuario)
                action = System.currentTimeMillis().toString() // Action única para diferenciar intents
            }

            val idAlarme = (nome + horario).hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                idAlarme,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)

            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
            Log.d("ALARME", "Agendado para: ${calendar.time}")
        }
    }
}

@Composable
fun TelaAlarme(
    nomeRemedio: String,
    descrição: String,
    onConfirmar: () -> Unit
) {
    val alertColor = Color(0xFFFF5252)
    val confirmColor = Color(0xFF000000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = alertColor,
                textAlign = TextAlign.Center,
                lineHeight = 45.sp
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
                    fontSize = 35.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = descrição,
                    fontSize = 22.sp,
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
        }
    }
}