package com.example.ozangokdemir.convomap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.ozangokdemir.convomap.utils.NotificationUtils;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// TODO: 3/23/2019 Now I'm working on putting the user status check to a background service.


/**
 * This service will run in the background and notify the user about status changes of other users.
 * The service will be started by the LoginActivity as soon as a successful login is established.
 */
public class UserStatusService extends Service {

    private static final String TAG = UserStatusService.class.getSimpleName();
    public static final String INTENT_RECEIVE_CODE = "99";
    FirebaseAuth mAuth;


    public UserStatusService() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}


    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance(); // get a hold of the firebase auth api reference.
        subscribeToUserStatusUpdates();
    }


    private void subscribeToUserStatusUpdates() {

        //get a hold of the firebase database so we can listen for the changes in active users.
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path));
        ref.addChildEventListener(new ChildEventListener() {

            /**
             *Will notify this user about another user just went online.
             */
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                //Build the notification and display it to the user.
                NotificationUtils.notifyUserSomebodyBecomeActive(dataSnapshot.getKey(), UserStatusService.this);
            }

            /**
             *Can this notify the user when somebody else gets in range?
             */
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });
    }

}
