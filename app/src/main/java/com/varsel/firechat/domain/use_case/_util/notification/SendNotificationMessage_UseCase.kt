package com.varsel.firechat.domain.use_case._util.notification

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.varsel.firechat.R
import com.varsel.firechat.common._utils.ImageUtils
import com.varsel.firechat.data.local.Notification.NotificationItem
import com.varsel.firechat.presentation.MainActivity
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import javax.inject.Inject

val INDIVIDUAL_GROUP_KEY = "INDIVIDUAK_MESSAGE_NOTIFICATIONS"
class SendNotificationMessage_UseCase @Inject constructor(

) {

    operator fun invoke(notifications: List<NotificationItem>, activity: SignedinActivity) {
        val intent = Intent(activity, SignedinActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        val notificationManager = NotificationManagerCompat.from(activity)


        notifications.forEachIndexed { i, v ->
            val contentView_large = RemoteViews(activity.packageName, R.layout.notification_message_large)
            contentView_large.setTextViewText(R.id.title, v.sender)
            contentView_large.setTextViewText(R.id.timestamp, v.timestamp)
            contentView_large.setTextViewText(R.id.body, v.messageBody)

            if(v.profile_image != null) {
                contentView_large.setImageViewBitmap(R.id.profile_image, ImageUtils.base64ToBitmap(v.profile_image!!))
            }
            val notificationBuilder = NotificationCompat.Builder(activity, CHANNEL_ID)
                .setContentTitle(v.sender)
                .setContentText(v.messageBody)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setGroup(INDIVIDUAL_GROUP_KEY)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(v.messageBody)
                )

            if(v.profile_image != null) {
                notificationBuilder.setLargeIcon(ImageUtils.base64ToBitmap(v.profile_image!!))
            }

            val notification = notificationBuilder.build()

            notificationManager.notify((1..999999999).random(), notification)

            if(i == notifications.size -1) {
                val summaryNotification = NotificationCompat.Builder(activity, CHANNEL_ID)
                    .setContentTitle("Summary")
                    .setContentText("Two new messages")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setStyle(NotificationCompat.InboxStyle()
                        .addLine("Heyyy... check this out")
                        .addLine("Yoooo.... what's up?")
                        .setBigContentTitle("2 new messages")
                        .setSummaryText("Ligma Balls")
                    )
                    .setGroup(INDIVIDUAL_GROUP_KEY)
                    .setGroupSummary(true)
                    .build()

                notificationManager.notify(0, summaryNotification)
            }
        }
    }
}