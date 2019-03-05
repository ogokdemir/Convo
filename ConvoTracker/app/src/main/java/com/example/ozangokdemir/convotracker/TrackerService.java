package com.example.ozangokdemir.convotracker;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class TrackerService extends Service {

    private static final String TAG = TrackerService.class.getSimpleName();
    public static final String INTENT_RECEIVE_CODE = "99";
    String mEmail, mPassword;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
    }

    /*
    This is called when an intent starts this service. In this context, this intent comes from the TrackerActivity
    and passes this service an email and a password to be used in the Firebase.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.i(TAG, "onStartCommand called.");

        Bundle received = intent.getExtras();
        String[] emailpassword = received.getStringArray(INTENT_RECEIVE_CODE);

        mEmail = emailpassword[0];
        mPassword = emailpassword[1];

        Log.i(TAG, "did the array arrive: "+ String.valueOf(emailpassword==null));
        Log.i(TAG, "Email: "+ emailpassword[0]);
        Log.i(TAG, "Password: "+ emailpassword[1]);

        loginToFirebase();

        return super.onStartCommand(intent, flags, startId);

    }

    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_tracker);
        startForeground(1, builder.build());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
            stopSelf();

            /**
             * Here I should add the code to remove the reference to the firebase database.
             */
        }
    };

    private void loginToFirebase() {
        // Authenticate with Firebase, and request location updates

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "firebase auth success");
                    Toast.makeText(TrackerService.this, "Success, tracker activated!", Toast.LENGTH_SHORT).show();
                    requestLocationUpdates();
                } else {
                    Log.d(TAG, "firebase auth failed");
                }
            }
        });
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000); //update the location of each active user in 10 seconds.
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //use both the GPS and Wifi, Blueetooth for best accuracy.
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        /**
         * Another problem is getting the user's name. For now, I will use their email as the key and display that on the map icon.
         */

        final String path = getString(R.string.firebase_path) + "/" + extractUsersNameFromNcfEmail(mEmail);

        //did the user grant location tracking permission to the app?
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //if they did
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    //access the firebase database that this app is connected to via the path (which is location).
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    //get the latest location result from this device's location manager.
                    Location location = locationResult.getLastLocation();

                    //if the location is not null, i.e., the location manager actually gave us some data.
                    if (location != null) {
                        Log.d(TAG, "location update " + location);

                        //set the value of the location path in the realtime database to the latest location.
                        ref.setValue(location);
                    }
                }
            }, null);
        }
    }

    /**
     * @param email NCF email address of the user.
     * @return extracts their name and last name from the NCF email address and returns it.
     */
    private String extractUsersNameFromNcfEmail(String email){
        String[] chunks = email.split("@");
        String[] fNamelName = chunks[0].split("\\.");

        String firstName = fNamelName[0].substring(0,1).toUpperCase()+fNamelName[0].substring(1);
        String lastName =  fNamelName[1].substring(0,1).toUpperCase()+fNamelName[1].substring(1,fNamelName[1].length()-2);

        return firstName+" "+lastName;

    }
}