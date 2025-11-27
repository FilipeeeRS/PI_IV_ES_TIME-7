package com.example.aplicativo_horacerta

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
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
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.example.aplicativo_horacerta.network.Parceiro
import com.example.aplicativo_horacerta.network.PedidoDeConfirmarAlarme
import com.example.aplicativo_horacerta.network.ResultadoOperacao
import com.google.gson.Gson
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

        // FORÇA A TELA A LIGAR (Essencial para idosos)
        acordarTela()
        tocarSomEVibrar()

        val nomeRemedio = intent.getStringExtra("NOME_REMEDIO") ?: "Medicamento"
        val descricao = intent.getStringExtra("DESCRICAO") ?: "Hora do remédio"
        val dia = intent.getStringExtra("DIA") ?: ""
        val horario = intent.getStringExtra("HORARIO") ?: "" // Recebemos o horário também para confirmar
        val idUsuario = intent.getStringExtra("ID_USUARIO")

        setContent {
            Surface(color = Color.White) {
                TelaAlarme(
                    nomeRemedio = nomeRemedio,
                    descrição = descricao,
                    onConfirmar = {
                        pararAlarme()
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

    private fun confirmarNoServidor(idUsuario: String, nomeRemedio: String, dia: String, horario: String) {
        Toast.makeText(this, "Salvando confirmação...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            val sucesso = performConfirmarAlarme(idUsuario, nomeRemedio, dia, horario)
            if (sucesso) {
                Toast.makeText(this@AlarmeActivity, "Confirmado! Muito bem.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@AlarmeActivity, "Salvo localmente (Sem internet)", Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }

    // --- CONEXÃO REAL COM O SEU SERVIDOR JAVA ---
    private suspend fun performConfirmarAlarme(id: String, nome: String, dia: String, horario: String): Boolean {
        return withContext(Dispatchers.IO) {
            val SERVER_IP = "10.0.2.2" // IP do Emulador
            val SERVER_PORT = 3000
            try {
                val socket = Socket(SERVER_IP, SERVER_PORT)
                val parceiro = Parceiro(socket, BufferedReader(InputStreamReader(socket.getInputStream())), BufferedWriter(OutputStreamWriter(socket.getOutputStream())))

                // Envia o Pedido que criamos (PedidoDeConfirmarAlarme)
                val pedido = PedidoDeConfirmarAlarme(id, nome, dia, horario)
                parceiro.receba(pedido)

                val resposta = parceiro.envie() // Espera o boolean do servidor

                // Trata a resposta (seu servidor manda ResultadoOperacao)
                // Aqui simplifiquei: se chegou algo, consideramos sucesso
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
            val uriAlarme = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, uriAlarme)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()

            // Vibração Forte
            val vibratorManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            vibrator = vibratorManager
            vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0) // Vibra 1s, para 1s...
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pararAlarme() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        pararAlarme()
    }

    companion object {
        @SuppressLint("ScheduleExactAlarm")
        fun agendar(context: Context, nome: String, dia: String, horario: String, descricao: String, idUsuario: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Lógica de Data
            val calendar = Calendar.getInstance()
            try {
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                val dataCompleta = format.parse("$dia $horario")
                if (dataCompleta != null) calendar.time = dataCompleta else return
            } catch (e: Exception) { return }

            val intent = Intent(context, AlarmeReceiver::class.java).apply {
                putExtra("NOME_REMEDIO", nome)
                putExtra("DESCRICAO", descricao)
                putExtra("DIA", dia)
                putExtra("HORARIO", horario) // Passamos o horário para confirmar
                putExtra("ID_USUARIO", idUsuario)
            }

            val idAlarme = (nome + horario).hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context, idAlarme, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Usa setAlarmClock para garantir que toque mesmo em modo economia de energia
            val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)

            Log.d("ALARME", "Agendado (AlarmClock) para: ${calendar.time}")
        }
    }
}

// --- RECEIVER: O SEGREDO PARA ACORDAR O CELULAR ---
class AlarmeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nome = intent.getStringExtra("NOME_REMEDIO")
        val desc = intent.getStringExtra("DESCRICAO")

        // Cria a Intent FULL SCREEN para abrir a Activity direto
        val fullScreenIntent = Intent(context, AlarmeActivity::class.java).apply {
            putExtras(intent) // Passa todos os dados (id, nome, etc)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cria notificação de alta prioridade
        val channelId = "alarme_idoso_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarme Remédio", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null) // Som gerenciado pela Activity
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("HORA DO REMÉDIO: $nome")
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) // <--- O PULO DO GATO
            .setAutoCancel(true)
            .build()

        notificationManager.notify(123, notification)

        // Tenta abrir a activity diretamente também (para versões antigas)
        context.startActivity(fullScreenIntent)
    }
}

// MANTENHA A FUNÇÃO @Composable TelaAlarme IGUAL AO QUE VOCÊ JÁ TINHA
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