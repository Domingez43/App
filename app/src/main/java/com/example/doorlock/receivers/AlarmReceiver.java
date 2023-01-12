package com.example.doorlock.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.doorlock.R;
import com.example.doorlock.TransmittingForegroundService;

public class AlarmReceiver extends BroadcastReceiver {
    //CREATE CLASS FOR CREATING NOTIFICATIONS
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, TransmittingForegroundService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);
        final String id = "Foreground Service ID";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Starting Transmitting")
                .setContentTitle("DoorLock")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(2,builder.build());
    }
}
