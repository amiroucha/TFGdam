package com.example.tfg_1

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MyApp : Application() {

    companion object {
        const val NOTIFICATION_ID = "my_channel_id"
    }

    override fun onCreate(){
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_ID,
                "NOTIFICACIONES DE APP",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "NOTIFICACIONES DE APP."
            val notificationManager = this.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

}