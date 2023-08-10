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

public class PedometerService extends Service {

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
                    "PedometerLib",
                    "Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void startNotification(){
        String input = "Counting your steps...";
        Intent notificationIntent = new Intent(this, Bridge.myActivity.getClass());

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, "PedometerLib")
                .setContentTitle("Background Walking Service")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(112, notification);
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: CREATED"+Bridge.steps);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadData();
        saveSummarySteps(Bridge.summarySteps+Bridge.steps);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved: REMOVED"+Bridge.steps);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: STARTED");
        createNotificationChannel();
        startNotification();
        super.onCreate();
        Bridge.initialSteps=0;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        initialDate = Calendar.getInstance().getTime();
        editor.putString(Bridge.INIT_DATE, currentDate.toString());
        editor.apply();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: DESTROYED");
        loadData();
        saveSummarySteps(Bridge.summarySteps+Bridge.steps);
    }

    public void saveData(int currentSteps) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        currentDate = Calendar.getInstance().getTime();
        editor.putString(Bridge.DATE, currentDate.toString());
        Log.i(TAG, "saveData: saved! "+currentSteps);
        editor.putInt(Bridge.STEPS, currentSteps);
        editor.apply();
    }
    public void saveSummarySteps(int stepsToSave) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        currentDate = Calendar.getInstance().getTime();
        editor.putString(Bridge.DATE, currentDate.toString());
        Log.i(TAG, "saveSummarySteps: saved! "+stepsToSave);
        editor.putInt("summarySteps", stepsToSave);
        editor.apply();
    }
    public void loadData() {
        Bridge.steps = sharedPreferences.getInt(Bridge.STEPS, 0);
        Bridge.summarySteps = sharedPreferences.getInt("summarySteps",0);
        Log.i(TAG, "loadData: steps"+Bridge.steps);
        Log.i(TAG, "loadData: summarySteps "+Bridge.summarySteps);
    }
}