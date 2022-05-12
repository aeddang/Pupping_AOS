package com.skeleton.module.firebase
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lib.model.WhereverYouCanGo
import com.lib.util.Log
import com.raftgroup.pupping.MainActivity
import com.raftgroup.pupping.R
import java.util.*


class FirebaseMessaging : FirebaseMessagingService() {
    companion object{
        const val PUSH_PAGE_KEY = "page"
        var pushTokenObservable: MutableLiveData<String?> = MutableLiveData(null)
    }

    private var appTag = javaClass.simpleName

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var pageJson:String? = null
        remoteMessage.data.isNotEmpty().let {
            pageJson = WhereverYouCanGo.parseJsonIwillGo(remoteMessage.data).stringfy()
        }
        remoteMessage.notification?.let {
            Log.i(
                appTag,
                "Message Notification forground : ${remoteMessage.data} , remoteMessage.notification : " +
                        "${remoteMessage.notification}"
            )
            createNotification(it.title, it.body,  pageJson)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(appTag, "onNewToken $token")
        pushTokenObservable.postValue(token)
    }

    private fun handleNow(messageParam: HashMap<String, Any>) {
        Log.d(appTag, "Short lived task is done.")
    }


    private fun createNotification(title:String?, text:String?, pageJson:String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(PUSH_PAGE_KEY , pageJson)

        val reCode = rand(1, 1000)
        val pendingIntent = PendingIntent.getActivity(
            this,  /* Request code */reCode, intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(ContextCompat.getColor(applicationContext, R.color.app_black))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1", name, importance).apply {
                description = descriptionText
            }

            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(reCode, notificationBuilder.build())

    }

    private fun rand(from: Int, to: Int): Int {
        val random = Random()
        return random.nextInt(to - from) + from
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

}