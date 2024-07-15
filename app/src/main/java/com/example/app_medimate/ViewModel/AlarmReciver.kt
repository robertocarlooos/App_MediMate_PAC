package com.example.app_medimate.ViewModel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Play alarm sound or show notification
        Toast.makeText(context, "Tome o seu medicamento !", Toast.LENGTH_SHORT).show()
        // You can add code to play sound or show a notification here
    }
}