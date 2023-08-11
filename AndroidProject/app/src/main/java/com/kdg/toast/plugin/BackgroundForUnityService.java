package com.kdg.toast.plugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BackgroundForUnityService extends Service {

    public SharedPreferences sharedPreferences;
    String TAG = "PEDOMETER";
    boolean running;
    Date currentDate;
    Date initialDate;
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

    private void startNotification(){
        String input = "Searching for game";
        Intent notificationIntent = new Intent(this, BridgeBackground.myActivity.getClass());

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, "DEFAULT_CHANNEL_ID")
                .setContentTitle("Neostesia matchmaking service")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher_icon_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(112, notification);
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: CREATED"+ BridgeBackground.steps);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadData();
        saveSummarySteps(BridgeBackground.summarySteps+ BridgeBackground.steps);
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
        BridgeBackground.initialSteps=0;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        initialDate = Calendar.getInstance().getTime();
        editor.putString(BridgeBackground.INIT_DATE, currentDate.toString());
        editor.apply();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: DESTROYED");
        loadData();
        saveSummarySteps(BridgeBackground.summarySteps+ BridgeBackground.steps);
    }

    public void saveData(int currentSteps) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        currentDate = Calendar.getInstance().getTime();
        editor.putString(BridgeBackground.DATE, currentDate.toString());
        Log.i(TAG, "saveData: saved! "+currentSteps);
        editor.putInt(BridgeBackground.STEPS, currentSteps);
        editor.apply();
    }
    public void saveSummarySteps(int stepsToSave) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        currentDate = Calendar.getInstance().getTime();
        editor.putString(BridgeBackground.DATE, currentDate.toString());
        Log.i(TAG, "saveSummarySteps: saved! "+stepsToSave);
        editor.putInt("summarySteps", stepsToSave);
        editor.apply();
    }
    public void loadData() {
        BridgeBackground.steps = sharedPreferences.getInt(BridgeBackground.STEPS, 0);
        BridgeBackground.summarySteps = sharedPreferences.getInt("summarySteps",0);
        Log.i(TAG, "loadData: steps"+ BridgeBackground.steps);
        Log.i(TAG, "loadData: summarySteps "+ BridgeBackground.summarySteps);
    }
}