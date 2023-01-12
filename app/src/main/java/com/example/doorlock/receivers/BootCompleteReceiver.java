package com.example.doorlock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.doorlock.TransmittingForegroundService;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            SharedPreferences pref = context.getSharedPreferences("TransmitStatus", Context.MODE_PRIVATE);
            if (pref.getBoolean("status", false)) {
                Log.e("BCR",String.valueOf(pref.getBoolean("status", false)));
                Intent pushIntent = new Intent(context, TransmittingForegroundService.class);
                context.startForegroundService(pushIntent);
            }
        }
    }
}
