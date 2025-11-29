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
        // "Acorda" cpu WAKELOCK, impede que o sistema durma
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "HoraCerta:AlarmeWakelock"
        )

        //10 minutis
        wakeLock.acquire(10 * 60 * 1000L)

        try {
            Log.d("ALARME", "Disparou! Tentando abrir tela...")

            val nome = intent.getStringExtra("NOME_REMEDIO") ?: "Medicamento"
            val desc = intent.getStringExtra("DESCRICAO") ?: "Hora de tomar"

            // Repassa todos as outras infos (IDs, horários, etc)
            val fullScreenIntent = Intent(context, AlarmeActivity::class.java).apply {
                putExtras(intent)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                // Único para não confundir remédios
                (nome + desc).hashCode(),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Canal de notificação com alta prioridade
            val channelId = "canal_alarme_v2"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

            // Formato notificalção "FULL SCREEN"
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("HORA DO REMÉDIO: $nome")
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .setOngoing(true)

            // Verificação de permissão para Android 13+ (POST_NOTIFICATIONS)
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(12345, builder.build())
            } else {
                // Se não tiver permissão de notificação, tentamos abrir a Activity na força bruta
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