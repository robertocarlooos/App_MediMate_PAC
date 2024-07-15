package com.example.app_medimate.ViewModel

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.app_medimate.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicamento = intent.getStringExtra("medicamento")
        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)
        val frequency = intent.getIntExtra("frequency", 24)

        // Crear el Intent para el botón de acción
        val markAsTakenIntent = Intent(context, MarkAsTakenReceiver::class.java)
        val markAsTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            markAsTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crear la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "medication_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Tomar Medicamento")
            .setContentText("Es hora de tomar tu medicamento: $medicamento")
            .setAutoCancel(true)
            .addAction(R.drawable.logo, "Marcar como tomado", markAsTakenPendingIntent)
            .build()

        notificationManager.notify(0, notification)
    }
}