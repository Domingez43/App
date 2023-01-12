package com.example.doorlock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


import com.example.doorlock.database.MqttDatabase;
import com.example.doorlock.receivers.BootCompleteReceiver;
import com.example.doorlock.receivers.TransmitRulesReceiver;
import com.example.doorlock.sharedPreferences.SharedPreferencesKeys;
import com.example.doorlock.sharedPreferences.SharedPreferencesManager;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


public class TransmittingForegroundService extends Service {

    // Log.e TAG
    protected static final String TAG = "BeaconTransmitting";

    //parameters provided by User
    private String password = "";
    private float txPower = -60;
    private float periodicity = 10;
    private TransmitRules rule = TransmitRules.ALWAYS;

    //Transmit status
    private boolean transmitting = false;
    private boolean isServiceOn = false;

    //Receiver for handling transmit rules
    private final TransmitRulesReceiver transmitRulesReceiver = new TransmitRulesReceiver(this);
    // intent filter for transmit receiver above
    private final IntentFilter transmitRulesFilter = new IntentFilter();

    //MQTT service which handles connection, receiving and sending messages
    //MQTT database used for saving received messages from mqtt
    private MqttDatabase db;
    private MqttService mqttService;

    //Handler used for transmitting
    private Handler taskHandler1;

    private SharedPreferencesManager preferences;

    @Override
    public void onCreate() {
        //adding actions for filter
        transmitRulesFilter.addAction(Intent.ACTION_SCREEN_ON);
        transmitRulesFilter.addAction(Intent.ACTION_SCREEN_OFF);
        transmitRulesFilter.addAction(Intent.ACTION_USER_PRESENT);

        preferences = new SharedPreferencesManager(getApplicationContext());

        db = MqttDatabase.getDbInstance(this.getApplicationContext());
        mqttService =new MqttService(db);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // create an MQTT client
        mqttService.startService();

        //receiving params from mainActivity
        dataReceiver();

        //registration of service
        if(rule != TransmitRules.ALWAYS){
            getApplicationContext().registerReceiver(transmitRulesReceiver, transmitRulesFilter);
        }

        //Status for BootCompleteReceiver
        setStatus(true);

        //creating new handler
        taskHandler1 = new Handler();
        startHandler();

        //Creating notification for service
        final String id = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(id,id,NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        startForeground(1,createNotification().build());

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startHandler() {
        transmitting = true;
        repetitiveTaskRunnable.run();
    }

    public void stopHandler() {
        transmitting = false;
        taskHandler1.removeCallbacks(repetitiveTaskRunnable);
    }


    private final Runnable repetitiveTaskRunnable = new Runnable() {
        public void run() {
            transmittingLoop();
            taskHandler1.postDelayed(this,(long)periodicity * 1000);
        }
    };

    private void transmittingLoop(){

        Beacon beacon = beaconBuilder();
        BeaconParser beaconParser = new BeaconParser().setBeaconLayout ("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter bt = new BeaconTransmitter(getApplicationContext(), beaconParser);
        Log.e("ADVERTISING","STARTING" + getMinor());
        startAdvertising(bt,beacon);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("ADVERTISING","STOPPING");
                bt.stopAdvertising();
            }
        }, 1500);
    }


    //------------------------------------STARTING ADVERTISING BEACON------------------------------------
    private void startAdvertising(BeaconTransmitter bt,Beacon beacon){
        bt.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: "+errorCode);
            }
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }
        });
    }

    /*
        CHANGELOG 19.11
        there were some problems when major and minor were 5digits and value exceeded some value for example 66889,
        was invalid number for major from timestamp 1668891510
     */
    private String getMajor(){
        return getTimestamp().substring(2,6);
    }

    private String getMinor(){
        return getTimestamp().substring(6,10);
    }


    //------------------------------------BUILDING BEACON WITH SPECIFIC MAJOR AND MINOR------------------------------------
    private Beacon beaconBuilder(){
        return new Beacon.Builder().setId1(sha256()).setId2(getMajor()).setId3(getMinor())
                .setManufacturer(0x004c) // Radius Networks.  Change this for other beacon layouts
                .setTxPower((int)txPower)
                .build();
    }

    //------------------------------------TIMESTAMP------------------------------------
    private String getTimestamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.toString();
    }

    @Override
    public void onDestroy(){
        setStatus(false);
        mqttService.disconnectClientV3();
        stopHandler();
        if(rule != TransmitRules.ALWAYS) {
            getApplicationContext().unregisterReceiver(transmitRulesReceiver);
        }
        super.onDestroy();
    }


    // ($HESLO|$BT_MAC|$UNIX_TIMESTAMP|$HESLO)
    //https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha-256-in-java
    private String sha256() {
        final StringBuilder sb = new StringBuilder();
        sb.append(password);
        sb.append(getTimestamp());
        sb.append(password);
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.substring(0,33);
        } catch(Exception ex){
            Log.e("HASH", "SOMETHING WRONG");
        }
        return "";
    }

    private void setStatus(Boolean b) {
        isServiceOn = !isServiceOn;
        SharedPreferences pref = getSharedPreferences("TransmitStatus", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("status",b);
        editor.apply();
    }

    public TransmitRules getRule(){
        return rule;
    }

    public boolean getStatus(){
        return transmitting;
    }

    private Notification.Builder createNotification(){
        final String id = "Foreground Service ID";
        return new  Notification.Builder(this,id)
                .setContentText("Transmitting")
                .setContentTitle("DoorLock")
                .setSmallIcon(R.drawable.ic_launcher_foreground);
    }


    //------------------------------------RECEIVING PASSWORD PERIODICITY AND TX POWER FROM MAIN ACTIVITY------------------------------------
    private void dataReceiver(){
        password = preferences.getStringVal(SharedPreferencesKeys.USER_PASSWORD.getKey(),
                SharedPreferencesKeys.USER_PASSWORD.getDefaultVal());
        Log.e("Service","Received password " + password);

        txPower = preferences.getFloatVal(SharedPreferencesKeys.TXPOWER.getKey(),
                Float.parseFloat(SharedPreferencesKeys.TXPOWER.getDefaultVal()));
        Log.e("Service","Received txPower " + txPower);

        periodicity = preferences.getFloatVal(SharedPreferencesKeys.PERIODICITY.getKey(),
                Float.parseFloat(SharedPreferencesKeys.PERIODICITY.getDefaultVal()));
        Log.e("Service","Received periodicity " + periodicity);

        String str = preferences.getStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(),
                SharedPreferencesKeys.TRANSMIT_RULE.getDefaultVal());

        rule = str.equals("ALWAYS") ? TransmitRules.ALWAYS : str.equals("LOCKED") ? TransmitRules.LOCKED : TransmitRules.UNLOCKED;
        Log.e("Service","Received rule " + rule);
    }

}
//----------------TRANSMITTER NOTES
//        m - matching byte sequence for this beacon type to parse (exactly one required)
//        s - ServiceUuid for this beacon type to parse (optional, only for Gatt-based beacons)
//        i - identifier (at least one required, multiple allowed)
//        p - power calibration field (exactly one required)
//        d - data field (optional, multiple allowed)
//        x - extra layout. Signifies that the layout is secondary to a primary layout with the same matching byte sequence (or ServiceUuid).
//            Extra layouts do not require power or identifier fields and create Beacon objects without identifiers.
