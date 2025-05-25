package com.example.tfg_1.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tfg_1.MainActivity
import com.example.tfg_1.MyApp
import com.example.tfg_1.R
import com.example.tfg_1.repositories.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService :FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        //obtener mensajes
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {
        //para que si pulso encima me lleve al chat
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigateToChat", true)  // indicamos que queremos abrir chat
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, MyApp.NOTIFICATION_ID)
            .setContentTitle("FlowHome")
            .setContentText("${message.notification?.title} : ${message.notification?.body}")
            .setSmallIcon(R.drawable.logotfg)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token FCM: $token")
        // guardar el nuevo token en Firestore
    }
}