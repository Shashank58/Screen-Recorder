package shashank.com.screenrecorder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.content.FileProvider
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import java.io.File

/**
 * Created by shashankm on 20/06/17.
 */
class ConvertMediaService : Service() {
    lateinit private var notificationManager: NotificationManager
    lateinit private var notification: Notification

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val expandedView = RemoteViews(packageName, R.layout.converting_notification_expanded)

        expandedView.setTextViewText(R.id.title, intent.getStringExtra("title"))

        val notificationBuilder = Notification.Builder(this).setOngoing(true).setAutoCancel(true)
        notification = notificationBuilder.build()
        notification.bigContentView = expandedView
        notification.icon = R.drawable.ic_stat_videocam

        startForeground(RecordService.FOREGROUND_NOTIFICATION, notification)
        notificationManager.notify(RecordService.FOREGROUND_NOTIFICATION, notification)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(NotificationCallbacks.CONVERSION_SUCCESS))
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(NotificationCallbacks.CONVERSION_FAILURE))

        return Service.START_NOT_STICKY
    }

    internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("Convert Media Service", "Coming with it")
            when (intent.action) {
                NotificationCallbacks.CONVERSION_SUCCESS -> {
                    // Stop indeterminate progressbar and make notification click redirect to converted media
                    val pendingIntent = PendingIntent.getActivity(this@ConvertMediaService, 0, Intent(Intent.ACTION_VIEW, FileProvider
                            .getUriForFile(context, context.applicationContext.packageName + ".provider", File(intent.getStringExtra
                            ("path")))).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_UPDATE_CURRENT)

                    val expandedView = RemoteViews(packageName, R.layout.converting_notification_expanded)
                    expandedView.setTextViewText(R.id.title, "Success")
                    expandedView.setViewVisibility(R.id.progress_bar, View.GONE)
                    expandedView.setViewVisibility(R.id.description, View.VISIBLE)

                    val notificationBuilder = Notification.Builder(this@ConvertMediaService).setOngoing(false).setAutoCancel(true)

                    notification = notificationBuilder.build()
                    notification.contentIntent = pendingIntent
                    notification.bigContentView = expandedView
                    notification.icon = R.drawable.ic_stat_videocam

                    startForeground(RecordService.FOREGROUND_NOTIFICATION, notification)
                    notificationManager.notify(RecordService.FOREGROUND_NOTIFICATION, notification)
                }

                NotificationCallbacks.CONVERSION_FAILURE -> {
                    // Display error message
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }
}