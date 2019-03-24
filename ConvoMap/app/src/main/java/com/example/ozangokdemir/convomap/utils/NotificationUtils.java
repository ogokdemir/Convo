package com.example.ozangokdemir.convomap.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.ozangokdemir.convomap.R;

public class NotificationUtils {

    /**
     * This method notifies this user that another user just became active. It even shares their name!
     */
    public static void notifyUserSomebodyBecomeActive(String name, Context context){

        //First notify the user with a Toast in case they have the ConvoMap on the foreground.
        Toast.makeText(context,
                String.valueOf(name)+ " just became online!", Toast.LENGTH_LONG).show();


        //Second, post a notification for the user. When they tap it, it will direct them to the map activity.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_user_activated)
                        .setContentTitle("ConvoMap")
                        .setContentText(name+ " just became online!");


        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }
}
