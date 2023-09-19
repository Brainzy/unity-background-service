package com.kdg.toast.plugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Random;

public class BackgroundForUnityService extends Service {

    public SharedPreferences sharedPreferences;
    String TAG = "NeostesiaService";
    static Notification notification;
    static NotificationManager notificationManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel notificationChannel = new NotificationChannel(
                    "DEFAULT_CHANNEL_ID",
                    "Neostesia background service channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void startNotification() {
        String input = "";
        Intent notificationIntent = new Intent(this, BridgeBackground.myActivity.getClass());

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, "DEFAULT_CHANNEL_ID")
                .setContentTitle("Neostesia Network Service")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher_icon_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(112, notification);

        CreateNotification();
    }

    public static void ExecuteNotification(){
        Random random = new Random();
        notificationManager.notify(random.nextInt(1000), notification);
    }


    public void CreateNotification(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Intent notificationIntent = new Intent(this, BridgeBackground.myActivity.getClass());
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            notification = new Notification.Builder(getApplicationContext(), "DEFAULT_CHANNEL_ID")
                    .setContentTitle("Neostesia")
                    .setContentText("Your match is ready!")
                    .setSmallIcon(R.mipmap.ic_launcher_icon_background)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: CREATED"+ BridgeBackground.steps);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved: REMOVED"+ BridgeBackground.steps);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: STARTED");
        createNotificationChannel();
        startNotification();
        super.onCreate();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: DESTROYED Neostesia Service");
        super.onDestroy();
    }

}