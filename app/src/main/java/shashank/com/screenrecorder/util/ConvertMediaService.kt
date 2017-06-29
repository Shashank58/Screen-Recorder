package shashank.com.screenrecorder.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import org.jetbrains.anko.intentFor
import shashank.com.screenrecorder.android.MainActivity
import shashank.com.screenrecorder.R
import shashank.com.screenrecorder.recorder.RecordService


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
        expandedView.setViewVisibility(R.id.progress_bar, View.VISIBLE)
        expandedView.setViewVisibility(R.id.description, View.GONE)

        val notificationBuilder = Notification.Builder(this).setOngoing(true).setAutoCancel(true)
        notification = notificationBuilder.build()
        notification.contentView = expandedView
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
                    val pendingIntent = PendingIntent.getActivity(this@ConvertMediaService, 0, Intent(Intent.ACTION_GET_CONTENT)
                            .setType(intent.getStringExtra("type")), PendingIntent.FLAG_UPDATE_CURRENT)

                    val expandedView = RemoteViews(packageName, R.layout.converting_notification_expanded)
                    expandedView.setTextViewText(R.id.title, "Success")
                    expandedView.setViewVisibility(R.id.progress_bar, View.GONE)
                    expandedView.setViewVisibility(R.id.description, View.VISIBLE)

                    val notificationBuilder = Notification.Builder(this@ConvertMediaService).setOngoing(false).setAutoCancel(true)

                    notification = notificationBuilder.build()
                    notification.contentIntent = pendingIntent
                    notification.contentView = expandedView
                    notification.icon = R.drawable.ic_convert_white

                    startForeground(RecordService.FOREGROUND_NOTIFICATION, notification)
                    notificationManager.notify(RecordService.FOREGROUND_NOTIFICATION, notification)
                }

                NotificationCallbacks.CONVERSION_FAILURE -> {
                    // Display error message
                    val pendingIntent = PendingIntent.getActivity(this@ConvertMediaService, 0, intentFor<MainActivity>()
                            , PendingIntent.FLAG_UPDATE_CURRENT)

                    val expandedView = RemoteViews(packageName, R.layout.converting_notification_expanded)
                    expandedView.setTextViewText(R.id.title, "Failure")
                    expandedView.setTextViewText(R.id.description, "Oops, looks like the media conversion failed, please try again")
                    expandedView.setViewVisibility(R.id.progress_bar, View.GONE)
                    expandedView.setViewVisibility(R.id.description, View.VISIBLE)

                    val notificationBuilder = Notification.Builder(this@ConvertMediaService).setOngoing(false).setAutoCancel(true)

                    notification = notificationBuilder.build()
                    notification.contentIntent = pendingIntent
                    notification.contentView = expandedView
                    notification.icon = R.drawable.ic_convert_white

                    startForeground(RecordService.FOREGROUND_NOTIFICATION, notification)
                    notificationManager.notify(RecordService.FOREGROUND_NOTIFICATION, notification)
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }
}