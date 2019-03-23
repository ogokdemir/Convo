package com.example.ozangokdemir.convotracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class TrackerActivity extends Activity {

    private static final String TAG = TrackerActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 1;
    public static final String INTENT_RECEIVE_CODE = "0";
    String[] mEmailPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retrieve the user email and password from the LoginSignupActivity.

        Bundle fromLogin = getIntent().getExtras();
        mEmailPassword = fromLogin.getStringArray(INTENT_RECEIVE_CODE);


        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    private void startTrackerService() {

        //Pass the user's email and password to the service. It will use it for communicating the location to the Firebase database.
        Intent startServiceIntent = new Intent(this, TrackerService.class);
        Bundle box = new Bundle();
        box.putStringArray(TrackerService.INTENT_RECEIVE_CODE, mEmailPassword);
        startServiceIntent.putExtras(box);
        startService(startServiceIntent);

        finish();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            startTrackerService();
        } else {
            Toast.makeText(this, "Convo can't operate without your permission to track you", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}