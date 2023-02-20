package com.varsel.firechat.domain.use_case._util.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

val CHANNEL_ID = "message_channel"
val NOTIFICATION_ID = 0

class CreateNotificationMessageChannel_UseCase @Inject constructor(

) {
    operator fun invoke(activity: SignedinActivity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lightColor = Color.GREEN
                enableLights(true)
            }
            val manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}