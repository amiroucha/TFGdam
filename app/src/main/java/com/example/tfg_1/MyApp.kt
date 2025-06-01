package com.example.tfg_1

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MyApp : Application() {
    // id único para el canal de notificaciones
    companion object {
        const val NOTIFICATION_ID = "my_channel_id"
    }

    //ejecuta la 1 vez que la app se creea
    override fun onCreate(){
        super.onCreate()
        // Crea el canal de notificaciones si es necesario
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        // Verifica si el sistema soporta canales de notificación (API >= 26)
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            // Crea el canal con ID, nombre y nivel de importancia alto
            val channel = NotificationChannel(
                NOTIFICATION_ID,
                "NOTIFICACIONES DE APP",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "NOTIFICACIONES DE APP."
            // Obtiene el gestor de notificaciones del sistema y crea el canal
            val notificationManager = this.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

}