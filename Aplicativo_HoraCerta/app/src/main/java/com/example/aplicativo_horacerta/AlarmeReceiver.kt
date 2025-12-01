package com.example.aplicativo_horacerta

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Recupera dados de login salvos
        val prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        val idLogado = prefs.getString("USER_UID", null)
        val tipoPerfil = prefs.getString("PROFILE_TYPE", "")

        Log.d("ALARME_CHECK", "Logado: $idLogado | Perfil: $tipoPerfil")

        // Validação: Só toca se houver usuário logado
        if (idLogado.isNullOrBlank()) {
            Log.w("ALARME_CHECK", "BLOQUEADO: Ninguém logado.")
            return
        }

        // Regra de negócio: Cuidador não recebe o alarme sonoro, apenas o Idoso
        if (tipoPerfil != "Idoso") {
            Log.w("ALARME_CHECK", "BLOQUEADO: Usuário é Cuidador.")
            return
        }

        // Wakelock: Garante que a CPU continue rodando para processar o alarme
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "HoraCerta:AlarmeWakelock"
        )
        wakeLock.acquire(10 * 60 * 1000L) // Mantém ativo por 10 min

        try {
            Log.d("ALARME", "Disparou! Tentando abrir tela...")

            val nome = intent.getStringExtra("NOME_REMEDIO") ?: "Medicamento"
            val desc = intent.getStringExtra("DESCRICAO") ?: "Hora de tomar"

            // Configura a Intent para abrir a tela cheia (AlarmeActivity)
            val fullScreenIntent = Intent(context, AlarmeActivity::class.java).apply {
                putExtras(intent)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                (nome + desc).hashCode(),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Tenta abrir a Activity imediatamente (funciona melhor em Androids antigos)
            try {
                context.startActivity(fullScreenIntent)
            } catch (e: Exception) {
                Log.e("ALARME", "Bloqueio de background: O Android preferiu a notificação.")
            }

            // Configuração da Notificação
            val channelId = "canal_alarme_v2"
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Cria canal de notificação (Obrigatório para Android 8+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Alarme de Medicamentos",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Toca e vibra na hora do remédio"
                    enableVibration(true)
                    setSound(null, null)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("HORA DO REMÉDIO: $nome")
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, true) // Tenta forçar tela cheia
                .setAutoCancel(true)
                .setOngoing(true)

            // Exibe notificação se tiver permissão
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(12345, builder.build())
            } else {
                // Se não tiver permissão de notificação, força a abertura da tela
                context.startActivity(fullScreenIntent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Libera o Wakelock após 5 segundos para economizar bateria
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (wakeLock.isHeld) wakeLock.release()
            }, 5000)
        }
    }
}