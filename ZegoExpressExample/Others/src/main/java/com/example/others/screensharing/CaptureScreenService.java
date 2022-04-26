package com.example.others.screensharing;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.others.R;

import java.util.Objects;

import static com.example.others.screensharing.ScreenSharingActivity.mMediaProjectionManager;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CaptureScreenService extends Service {
    private int mResultCode;
    private Intent mResultData;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        ScreenSharingActivity.mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, Objects.requireNonNull(mResultData));
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());

        // 以下是对Android 8.0的适配
        // The following is Android 8.0 adaptation
        // normal notification
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        // Foreground services Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // get built Notification
        notification.defaults = Notification.DEFAULT_SOUND; // set it as default sound
        startForeground(110, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}