package com.example.ozangokdemir.convomap;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.ozangokdemir.convomap.utils.FirebaseUtils;
import com.example.ozangokdemir.convomap.utils.MapUtils;
import com.example.ozangokdemir.convomap.utils.NotificationUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = DisplayActivity.class.getSimpleName();
    private HashMap<String, Marker> mMarkers = new HashMap<>(); // maps the marker to the location it represents.
    private GoogleMap mMap;
    public static final String INTENT_RECEIVE_KEY = "mjollnir";
    private FirebaseUtils firebaseUtils; // a class that I wrote for keeping the firebase outside of the activity.

    String mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent starter = getIntent();
        String[] passedPackage = starter.getExtras().getStringArray(INTENT_RECEIVE_KEY);
        mEmail = passedPackage[0];
        mPassword = passedPackage[1];

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Authenticate with Firebase when the Google map is loaded
        mMap = googleMap;
        mMap.setMaxZoomPreference(16);

        //Create a reference to the firebase utils object.
        firebaseUtils = new FirebaseUtils(this, mMarkers, mMap);
        //Login to firebase and start observing updates to display them on the map.
        firebaseUtils.loginToFirebase(mEmail, mPassword);

    }
}



