package com.kdg.toast.plugin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unity3d.player.UnityPlayer;

import java.util.Calendar;
import java.util.Date;

public final class BridgeBackground extends Application {
    static ClientConnection clientConnection;
    static GameServerConnection gameServerConnection;
    static int testSteps;
    static int summarySteps;
    static int steps;
    static int initialSteps;
    static boolean isUnityPaused;
    static Activity myActivity;
    static Context appContext;
    static final String STEPS="steps";
    static final String SUMMARY_STEPS="summarySteps";
    static final String DATE="currentDate";
    static final String INIT_DATE="initialDate";
    public static final Intent[] POWERMANAGER_INTENTS = new Intent[]{
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    };

    public static void SendStringMessageToServer(String message){
        clientConnection.send(message);
    }
    public static void SendStringMessageToGameServer(String message){ gameServerConnection.send(message);}

    public static boolean IsUnityTabbed(){
        return isUnityPaused;
    }

    public static void ReceiveActivityInstance(Activity tempActivity) {
        myActivity = tempActivity;
        String[] perms= new String[1];
        perms[0]=Manifest.permission.FOREGROUND_SERVICE;
        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("NeostesiaService", "Permision isnt granted!");
            ActivityCompat.requestPermissions(BridgeBackground.myActivity,
                    perms,
                    1);

        }
    }

    public static void ConnectToWorldServer(String worldServerIp, String port){
        Log.i("NeostesiaService", "Received request for connecting to game server ");
        clientConnection = new ClientConnection(worldServerIp, Integer.parseInt(port));
    }

    public static void ConnectToGameServer(String gameServerIp, String port){
        Log.i("NeostesiaService", "Received request for connecting to game server ");
        gameServerConnection = new GameServerConnection(gameServerIp, Integer.parseInt(port));
    }

    public static void DisconnectFromWorldServer() {
        try{
            if (clientConnection != null && clientConnection.webSocketClient != null){
                Log.i("NeostesiaService", "Trying to close client connection to world server");
                clientConnection.webSocketClient.close(5000, 1000, "Closed by server");
            }
        }catch (Exception e){
            Log.i("NeostesiaService", "Error closing client connection to world server "+e);
        }
    }

    public static void DisconnectFromGameServer() {
        gameServerConnection.webSocketClient.close(5000, 1000, "Closed by server");
    }

    public static void NotifyUnityAppWhenFocusedAboutMatch(final String message){
        Log.i("NeostesiaService", "Waiting for Unity to tab back in for message  "+ message);

        myActivity.runOnUiThread(new Runnable() {
            public void run() {
                BackgroundForUnityService.ExecuteNotification();
                Toast.makeText(appContext, "Your match is ready!", Toast.LENGTH_LONG).show();
            }
        });

        Log.i("NeostesiaService", "Entering countdown timer bellow "+ message);
        myActivity.runOnUiThread(new Runnable() {
            public void run() {
                new CountDownTimer(Long.MAX_VALUE, 200) {
                    public void onTick(long millisUntilFinished) {
                        if (IsUnityTabbed() == false){
                            UnityPlayer.UnitySendMessage("BackgroundService", // gameObject name
                                    "ReceiveByteMessageFromServer", // this is a callback in C#
                                    "10|$cancelFindGame|gameMode:CONSTRUCTED|cause:USER_CANCELED"); // msg
                            Log.i("NeostesiaService", "Timer for waiting for Unity tab back in started");
                            UnityPlayer.UnitySendMessage("BackgroundService", // gameObject name
                                    "ReceiveByteMessageFromServer", // this is a callback in C#
                                    message); // msg
                            cancel(); // stops timer
                        }
                    }
                    public void onFinish() {}
                }.start();
            }
        });
    }

    public static void ChangeUnityAppIsTabbedStatus(String newStatus){
        boolean pauseStatus = false;
        if (newStatus.equals("True")){
            pauseStatus = true;
            Log.i("NeostesiaService", "pause status is true");
        }
        isUnityPaused = pauseStatus;

        Log.i("NeostesiaService", "Setting UnityAppIsPaused to "+ newStatus);
    }

    public static void StartService() {
        if (myActivity != null) {
            incrementTestSteps();
            final SharedPreferences sharedPreferences = myActivity.getSharedPreferences("service_settings", MODE_PRIVATE);
            if (!sharedPreferences.getBoolean("auto_start", false)) {
                for (final Intent intent : POWERMANAGER_INTENTS) {
                    if (myActivity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                        AlertDialog alertDialog = new AlertDialog.Builder(myActivity).create();
                        alertDialog.setTitle("Auto start is required");
                        alertDialog.setMessage("Please enable auto start to provide correct work");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        sharedPreferences.edit().putBoolean("auto_start", true).apply();
                                        myActivity.startActivity(intent);
                                    }
                                });
                        alertDialog.show();
                        break;
                    }
                }
            }
            start();
        }
        else{
            start();
        }
    }

    private static void incrementTestSteps(){
        new CountDownTimer(Long.MAX_VALUE, 9000) {

            public void onTick(long millisUntilFinished) {
                if (clientConnection!=null)
                    clientConnection.send("0|$a|cc:0|afk:0|s:4"); // this is keep alive heart beat
                testSteps++;
            }
            public void onFinish() {
                testSteps++;
            }

        }.start();
    }

    private static void start(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myActivity.startForegroundService(new Intent(myActivity, BackgroundForUnityService.class));
        }
    }
    public static void StopService(){
        Intent serviceIntent = new Intent(myActivity, BackgroundForUnityService.class);
        myActivity.stopService(serviceIntent);

    }
    public static int GetCurrentSteps(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Date currentDate = Calendar.getInstance().getTime();
        editor.putString(DATE, currentDate.toString());
        int walkedSteps = sharedPreferences.getInt(STEPS, 0);
        int allSteps = sharedPreferences.getInt(SUMMARY_STEPS,0);
        summarySteps = walkedSteps + allSteps + testSteps;
        Log.i("NeostesiaService", "FROM BRIDGE CLASS - GetCurrentSteps:"+summarySteps);
        return summarySteps;
    }
    public static String SyncData(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        int stepsToSend=GetCurrentSteps();
        String firstDate = sharedPreferences.getString(INIT_DATE,"");
        String lastDate = sharedPreferences.getString(DATE,"");
        String data = firstDate+'#'+lastDate+'#'+stepsToSend;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEPS,0);
        editor.putInt(SUMMARY_STEPS,0);
        steps=0;
        summarySteps=0;
        initialSteps=0;
        Date currentDate = Calendar.getInstance().getTime();
        editor.putString(INIT_DATE,currentDate.toString());
        editor.putString(DATE,currentDate.toString());
        editor.apply();
        Log.i("NeostesiaService", "SyncData: " + steps + ' ' + summarySteps + data + " ");

        UnityPlayer.UnitySendMessage("BackgroundService", // gameObject name
                "PluginCallback", // this is a callback in C#
                "Hello from android plugin"); // msg
        return data;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BridgeBackground.appContext=getApplicationContext();

    }
}
