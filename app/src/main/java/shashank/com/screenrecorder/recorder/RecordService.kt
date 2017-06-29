package shashank.com.screenrecorder.recorder

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
import android.widget.RemoteViews
import shashank.com.screenrecorder.util.AppUtil
import shashank.com.screenrecorder.android.MainActivity
import shashank.com.screenrecorder.util.NotificationCallbacks
import shashank.com.screenrecorder.R

class RecordService : Service() {

    lateinit private var notificationManager: NotificationManager
    lateinit private var notification: Notification

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val expandedView = RemoteViews(packageName, R.layout.screen_record_notification_expanded)
        val stopIntent = PendingIntent.getBroadcast(this, 0, Intent(NotificationCallbacks.STOP), 0)
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this,
                MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT)

        expandedView.setOnClickPendingIntent(R.id.stop, stopIntent)

        val notificationBuilder = Notification.Builder(this).setOngoing(true).setContentTitle("Screen Recorder")
                .setContentText("Your screen is being recorded").setAutoCancel(true)
        notification = notificationBuilder.build()
        notification.contentIntent = pendingIntent
        notification.bigContentView = expandedView
        notification.icon = R.drawable.ic_stat_videocam

        val intentFilter = IntentFilter()
        intentFilter.addAction(NotificationCallbacks.PLAY_PAUSE)
        intentFilter.addAction(NotificationCallbacks.STOP)

        registerReceiver(receiver, intentFilter)
        startForeground(FOREGROUND_NOTIFICATION, notification)

        notificationManager.notify(FOREGROUND_NOTIFICATION, notification)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                 IntentFilter(NotificationCallbacks.STOP))
    }

    internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                NotificationCallbacks.STOP -> {
                    ScreenRecordHelper.stopRecording()
                    unregisterReceiver(this)
                    stopForeground(true)
                    stopSelf()
                    notificationManager.cancel(FOREGROUND_NOTIFICATION)
                    LocalBroadcastManager.getInstance(this@RecordService).sendBroadcast(Intent(AppUtil.SCREEN_SHARE_STOPPED))
                }

                NotificationCallbacks.PLAY_PAUSE -> if (ScreenRecordHelper.isRecording()) {
                    ScreenRecordHelper.pauseRecorder()
                } else {
                    ScreenRecordHelper.resumeRecorder()
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    companion object {
        val FOREGROUND_NOTIFICATION = 0

        private val TAG = RecordService::class.java.simpleName
    }
}
