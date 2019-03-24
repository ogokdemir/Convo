package com.example.ozangokdemir.convomap;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
        loginToFirebase();

    }


    private void loginToFirebase() {


        // Authenticate with Firebase and subscribe to updates
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            //If the app successfully authenticated the user
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    subscribeToUpdates(); //subcribe to the database so that the map dipslays their locations.


                    Toast.makeText(DisplayActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "firebase auth success");
                } else {
                    Log.d(TAG, "firebase auth failed");
                    //notify the user that their credentials are wrong.
                    Toast.makeText(DisplayActivity.this,
                            "Hmm, looks like your email or password is wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void subscribeToUpdates() {

        //get a hold of the firebase database so that we can subscribe to changes in the location.
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path));
        ref.addChildEventListener(new ChildEventListener() {

            //Called when a new user just become online.
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                NotificationUtils.notifyUserSomebodyBecomeActive(dataSnapshot.getKey(), DisplayActivity.this);
                setMarker(dataSnapshot);
            }

            //Updated every tine a user's location changes. Everyone is notified.
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            //When a user goes offline, removes their map icon from the map.
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                //Remove the logged off user from the markers list.
                mMarkers.remove(dataSnapshot.getKey());

                //populate the map with the new markers list (if there are any active users.)
                if(mMarkers.size()!=0){
                    for(Marker marker: mMarkers.values())
                        mMap.addMarker(new MarkerOptions().title(marker.getTitle()).position(marker.getPosition()));

                }
                //if nobody is active, notify the user and encourage them to be the first one.
                else{
                        Toast.makeText(DisplayActivity.this,
                                "No active users, how about being the first one? Just activate your tracker!", Toast.LENGTH_LONG).show();
                        mMap.clear();
                    }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void setMarker(DataSnapshot dataSnapshot) {
        // When a location update is received, put, update, or remove
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once


        String key = dataSnapshot.getKey(); // key is the user's name.

        //Parse the data into an hashmap. String --> user name, Object --> contains all the other attributes of the user entry.
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();

        //unpacking the latitude and longitude of the user's location from the object just received.
        double lat = Double.parseDouble(value.get("latitude").toString());
        double lng = Double.parseDouble(value.get("longitude").toString());

        //LatLng is similar to a Python tuple, it's specifically for (lat, lng) as the name suggests.
        LatLng location = new LatLng(lat, lng);

        //If a new user got online, add a new marker with their location.
        if (!mMarkers.containsKey(key)) {
            mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location)));

           //If the location of an already online user (marker) is updated, just set the marker to new location.
        } else {
            mMarkers.get(key).setPosition(location);
        }

        // LatLngBounds.Builder takes in a bunch of markers and focuses the map to display all of them at once.
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));

    }


}



