package com.example.ozangokdemir.convomap.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.example.ozangokdemir.convomap.LoginActivity;
import com.example.ozangokdemir.convomap.R;

public class NotificationUtils {

    //Notification stuff.
    private static final String CHANNEL_ID = "convomap_notifications";
    private static final String CHANNEL_NAME = "Convo Map";


    /**
     * This method notifies this user that another user just became active. It even shares their name!
     */
    public static void notifyUserSomebodyBecomeActive(String name, Context context){

        //First notify the user with a Toast in case they have the ConvoMap on the foreground.
        Toast.makeText(context,
                "Convo: "+ String.valueOf(name)+ " just became online!", Toast.LENGTH_LONG).show();

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        //This pending intent will start the DisplayActivity when the user taps on the notification.
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, LoginActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        //Second, post a notification for the user. When they tap it, it will direct them to the map activity.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_user_activated)
                        .setContentTitle("ConvoMap")
                        .setContentIntent(contentIntent)
                        .setContentText(name+ " just became online, tap to connect!");


        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }



}
