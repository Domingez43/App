package com.example.doorlock.receivers;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.doorlock.TransmitRules;
import com.example.doorlock.TransmittingForegroundService;

// getApplicationContext().registerReceiver(broadcastReceiver, theFilter);


public class TransmitRulesReceiver extends BroadcastReceiver{

    private TransmittingForegroundService service;
    final IntentFilter TransmitRulesFilter = new IntentFilter();

    public TransmitRulesReceiver(TransmittingForegroundService transmittingForegroundService){
        service = transmittingForegroundService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        TransmitRulesFilter.addAction(Intent.ACTION_SCREEN_ON);
        TransmitRulesFilter.addAction(Intent.ACTION_SCREEN_OFF);
        TransmitRulesFilter.addAction(Intent.ACTION_USER_PRESENT);

        String strAction = intent.getAction();
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE); //IDK WHY THIS IS HERE

        if (strAction.equals(Intent.ACTION_USER_PRESENT) || strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON)) {
            if (strAction.equals(Intent.ACTION_USER_PRESENT) && service.getRule() == TransmitRules.UNLOCKED) {
                        if(!service.getStatus()){
                            try{
                                service.startHandler();
                            }catch (Exception e){
                                Log.e("SERVICE","CANNOT BE STARTED");
                            }
                        }
                Log.e("BroadcastReceiver", "SCREEN UNLOCKED");
            } else if (strAction.equals(Intent.ACTION_SCREEN_OFF) && (service.getRule() == TransmitRules.LOCKED || service.getRule() == TransmitRules.UNLOCKED)){
                        if(service.getStatus()){
                            try{
                                service.stopHandler();
                            }catch (Exception e){
                                Log.e("SERVICE","CANNOT BE STOPPED");
                            }
                        }
                Log.e("BroadcastReceiver", "SCREEN OFF");
            }else if(service.getRule() == TransmitRules.LOCKED) {
                        if(!service.getStatus()){
                            try{
                                service.startHandler();
                            }catch (Exception e){
                                Log.e("SERVICE","CANNOT BE STARTED");
                            }
                        }
                Log.e("BroadcastReceiver", "SCREEN ON");
            }
        }

    }




}







