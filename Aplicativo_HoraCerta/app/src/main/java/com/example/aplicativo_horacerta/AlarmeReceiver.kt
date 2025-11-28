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
        // 1. ACORDAR A CPU IMEDIATAMENTE (WAKELOCK)
        // Isso impede que o sistema "durma" enquanto tentamos abrir a tela
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "HoraCerta:AlarmeWakelock"
        )
        wakeLock.acquire(10 * 60 * 1000L /* 10 minutos */)

        try {
            Log.d("ALARME", "Disparou! Tentando abrir tela...")

            val nome = intent.getStringExtra("NOME_REMEDIO") ?: "Medicamento"
            val desc = intent.getStringExtra("DESCRICAO") ?: "Hora de tomar"

            // Repassamos todos os extras (IDs, horários, etc)
            val fullScreenIntent = Intent(context, AlarmeActivity::class.java).apply {
                putExtras(intent)
                // Flags essenciais para abrir saindo do background
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                (nome + desc).hashCode(), // ID único para não confundir remédios
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 2. CRIAR CANAL DE NOTIFICAÇÃO COM PRIORIDADE MÁXIMA
            // Mudei o ID para "canal_alarme_v2" para garantir que o Android reset as configurações
            val channelId = "canal_alarme_v2"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Alarme de Medicamentos",
                    NotificationManager.IMPORTANCE_HIGH // <--- IMPORTANTE: HIGH ou MAX
                ).apply {
                    description = "Toca e vibra na hora do remédio"
                    enableVibration(true)
                    setSound(null, null) // O som será tocado pela Activity, não pela notificação
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }

            // 3. MONTAR A NOTIFICAÇÃO "FULL SCREEN"
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("HORA DO REMÉDIO: $nome")
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_MAX) // Prioridade Máxima para versões antigas
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, true) // <--- O PULO DO GATO: Abre a activity direto
                .setAutoCancel(true)
                .setOngoing(true) // Impede de limpar arrastando pro lado

            // Verificação de permissão para Android 13+ (POST_NOTIFICATIONS)
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(12345, builder.build())
            } else {
                // Se não tiver permissão de notificação, tentamos abrir a Activity na força bruta
                // (Isso funciona em Androids mais antigos < 10)
                context.startActivity(fullScreenIntent)
            }

            Log.d("ALARME", "Notificação enviada com FullScreenIntent")

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Solta o WakeLock depois de 5 segundos para não gastar bateria se algo travar
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (wakeLock.isHeld) wakeLock.release()
            }, 5000)
        }
    }
}