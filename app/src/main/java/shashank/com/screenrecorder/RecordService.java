package shashank.com.screenrecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;

public class RecordService extends Service {
    public static final int FOREGROUND_NOTIFICATION = 0;

    private static final String TAG = RecordService.class.getSimpleName();

    private NotificationManager notificationManager;
    private Notification notification;

    public RecordService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);
        PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0, new Intent(NotificationCallbacks.STOP), 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                        MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT);

        expandedView.setOnClickPendingIntent(R.id.stop, stopIntent);

        Notification.Builder notificationBuilder =
                new Notification.Builder(this).setOngoing(true).setContentTitle("Title")
                        .setContentText("Text").setAutoCancel(true);
        notification = notificationBuilder.build();
        notification.contentIntent = pendingIntent;
        notification.bigContentView = expandedView;
        notification.icon = R.drawable.ic_stat_videocam;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationCallbacks.PLAY_PAUSE);
        intentFilter.addAction(NotificationCallbacks.STOP);

        registerReceiver(receiver, intentFilter);
        startForeground(FOREGROUND_NOTIFICATION, notification);

        notificationManager.notify(FOREGROUND_NOTIFICATION, notification);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NotificationCallbacks.STOP:
                    ScreenRecordHelper.INSTANCE.stopRecording();
                    unregisterReceiver(this);
                    stopForeground(true);
                    stopSelf();
                    notificationManager.cancel(FOREGROUND_NOTIFICATION);
                    break;

                case NotificationCallbacks.PLAY_PAUSE:
                    if (ScreenRecordHelper.INSTANCE.isRecording()) {
                        ScreenRecordHelper.INSTANCE.pauseRecorder();
                    } else {
                        ScreenRecordHelper.INSTANCE.resumeRecorder();
                    }
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
}
