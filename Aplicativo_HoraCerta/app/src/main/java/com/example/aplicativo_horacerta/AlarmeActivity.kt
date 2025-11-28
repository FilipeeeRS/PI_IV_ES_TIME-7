package com.example.aplicativo_horacerta

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager // IMPORTANTE
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
import com.example.aplicativo_horacerta.network.Parceiro
import com.example.aplicativo_horacerta.network.PedidoDeConfirmarAlarme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmeActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        // 1. PRIMEIRA COISA: PARA TUDO!
                        pararAlarme()
                        cancelarNotificacao() // <--- O SEGREDO ESTÁ AQUI

                        // 2. Tenta avisar o servidor, mas fecha a tela logo
                        if (!idUsuario.isNullOrBlank()) {
                            confirmarNoServidor(idUsuario, nomeRemedio, dia, horario)
                        } else {
                            finish() // Fecha a tela imediatamente se não tiver ID
                        }
                    }
                )
            }
        }
    }

    // --- NOVA FUNÇÃO PARA MATAR A NOTIFICAÇÃO ---
    private fun cancelarNotificacao() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // O ID 12345 deve ser o mesmo usado no AlarmeReceiver.kt
            notificationManager.cancel(12345)
            Log.d("ALARME", "Notificação cancelada com sucesso.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun confirmarNoServidor(idUsuario: String, nomeRemedio: String, dia: String, horario: String) {
        Toast.makeText(this, "Confirmando...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            // Não esperamos a resposta para fechar a tela, para ser mais rápido
            // Mas mostramos o Toast baseado no resultado
            val sucesso = performConfirmarAlarme(idUsuario, nomeRemedio, dia, horario)

            if (sucesso) {
                Toast.makeText(getApplicationContext(), "Confirmado no Servidor!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplicationContext(), "Salvo localmente (Erro Servidor)", Toast.LENGTH_SHORT).show()
            }

            // GARANTE QUE A TELA FECHE DEPOIS DA OPERAÇÃO
            finishAndRemoveTask()
        }
    }

    private suspend fun performConfirmarAlarme(id: String, nome: String, dia: String, horario: String): Boolean {
        return withContext(Dispatchers.IO) {
            val SERVER_IP = "10.0.116.3" // <--- CONFIRA SEU IP AQUI
            val SERVER_PORT = 3000
            try {
                val socket = Socket(SERVER_IP, SERVER_PORT)
                val parceiro = Parceiro(socket, BufferedReader(InputStreamReader(socket.getInputStream())), BufferedWriter(OutputStreamWriter(socket.getOutputStream())))

                val pedido = PedidoDeConfirmarAlarme(id, nome, dia, horario)
                parceiro.receba(pedido)

                val resposta = parceiro.envie() // Espera boolean
                socket.close()
                resposta != null
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
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

    private fun tocarSomEVibrar() {
        try {
            // Tenta parar qualquer som anterior antes de começar um novo
            pararAlarme()

            val uriAlarme = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, uriAlarme)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()

            val vibratorManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            vibrator = vibratorManager
            // Vibra padrão SOS (... --- ...)
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
        cancelarNotificacao() // Garante que morre mesmo se fechar o app forçado
    }

    companion object {
        @SuppressLint("ScheduleExactAlarm")
        fun agendar(context: Context, nome: String, dia: String, horario: String, descricao: String, idUsuario: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val calendar = Calendar.getInstance()
            try {
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                val dataCompleta = format.parse("$dia $horario")

                // Se a data for nula ou já passou, não agenda
                if (dataCompleta == null) return

                calendar.time = dataCompleta

                // --- CORREÇÃO IMPORTANTE: Se o horário já passou hoje, ignora ou joga pro futuro ---
                // No seu caso, como a lista filtra futuros, assumimos que é futuro.
                // Mas se for passado imediato (ex: 1 min atrás), o alarme toca na hora.
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    Log.d("ALARME", "Horário $horario já passou. Ignorando agendamento.")
                    return
                }

            } catch (e: Exception) { return }

            val intent = Intent(context, AlarmeReceiver::class.java).apply {
                putExtra("NOME_REMEDIO", nome)
                putExtra("DESCRICAO", descricao)
                putExtra("DIA", dia)
                putExtra("HORARIO", horario)
                putExtra("ID_USUARIO", idUsuario)
                // Adiciona ação para diferenciar intents
                action = System.currentTimeMillis().toString()
            }

            val idAlarme = (nome + horario).hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context, idAlarme, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)

            Log.d("ALARME", "Agendado para: ${calendar.time}")
        }
    }
}

// Mantenha o @Composable TelaAlarme como está...
@Composable
fun TelaAlarme(nomeRemedio: String, descrição: String, onConfirmar: () -> Unit) {
    val alertColor = Color(0xFFFF5252)
    val confirmColor = Color(0xFF4CAF50)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 50.dp)) {
            Icon(Icons.Filled.NotificationsActive, "Alarme", tint = alertColor, modifier = Modifier.size(120.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("HORA DO REMÉDIO!", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = alertColor, textAlign = TextAlign.Center, lineHeight = 45.sp)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(nomeRemedio, fontSize = 35.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(descrição, fontSize = 22.sp, color = Color.Black, textAlign = TextAlign.Center)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 30.dp)) {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Text("JÁ TOMEI", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}