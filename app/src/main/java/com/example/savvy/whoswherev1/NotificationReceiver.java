package com.example.savvy.whoswherev1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationReceiver extends BroadcastReceiver {

    NotificationManager nm;
    NotificationChannel channel;
    NotificationCompat.Builder notification;

    @Override
    public void onReceive(Context context, Intent intent) {
        nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        channel = new NotificationChannel("default", "Who's Where?", nm.IMPORTANCE_DEFAULT);
        nm.createNotificationChannel(channel);

        notification = new NotificationCompat.Builder(context, channel.getId());
        notification.setAutoCancel(true);

        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Someone checked in!");
        notification.setContentText("User checked in to your spot");

        Intent intent1 = new Intent(context, spotInfo.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        // Builds notification and issues it
        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(12346, notification.build());
    }
}
