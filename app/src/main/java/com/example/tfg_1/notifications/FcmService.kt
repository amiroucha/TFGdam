package com.example.tfg_1.notifications

import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tfg_1.MyApp
import com.example.tfg_1.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService :FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        //obtener mensajes
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {
        val title = message.notification?.title ?: "Nuevo mensaje"
        val body = message.notification?.body ?: "Tienes un nuevo mensaje"

        val notification = NotificationCompat.Builder(this, MyApp.NOTIFICATION_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.logotfg)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token FCM: $token")
        // guardar el nuevo token en Firestore
    }
}