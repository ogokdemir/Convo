package com.example.ozangokdemir.convomap;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.example.ozangokdemir.convomap.utils.FirebaseUtils;
import com.example.ozangokdemir.convomap.utils.MapUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import static com.example.ozangokdemir.convomap.utils.FirebaseUtils.extractUsersNameFromNcfEmail;
import static com.example.ozangokdemir.convomap.utils.FirebaseUtils.recordUserMarkerTip;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = DisplayActivity.class.getSimpleName();
    private HashMap<String, Marker> mMarkers = new HashMap<>(); // maps the marker to the location it represents.
    private GoogleMap mMap;
    public static final String INTENT_RECEIVE_KEY = "mjollnir";
    private FirebaseUtils firebaseUtils; // a class that I wrote for keeping the firebase outside of the activity.
    String mEmail, mPassword;
    FloatingActionButton mFabSurveys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Intent starter = getIntent();
        String[] passedPackage = starter.getExtras().getStringArray(INTENT_RECEIVE_KEY);
        mEmail = passedPackage[0];
        mPassword = passedPackage[1];

        mFabSurveys = (FloatingActionButton) findViewById(R.id.fab);

        //When the user taps on the floating action button on the map, take them to the surveys activity.
        mFabSurveys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSurveys = new Intent(DisplayActivity.this, SurveysActivity.class);
                String username = extractUsersNameFromNcfEmail(mEmail).split(" ")[0];
                toSurveys.putExtra("user_name", username);
                startActivity(toSurveys);
            }
        });

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

        mMap.setOnMarkerClickListener(this); //set the marker listener.

    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        //First of all, get the user's own location and the tapped markers' location.
        LatLng tappedLocation = marker.getPosition(); //get the LatLng type position of the tapped
        Marker usersMarker = mMarkers.get(extractUsersNameFromNcfEmail(mEmail));

       //Check if the user herself has their tracker on as they are running the map.
       if( usersMarker== null){

           //If the user's tracker not on (they're not showing on the map) prompt them to go online to see their distance to others.
           Toast.makeText(this, "Please activate your tracker to see your distance to "+
                   marker.getTitle(),Toast.LENGTH_SHORT).show();
       }

       //If the user himself is online,
       else{
           //and the user has tapped on somebody else's location marker:
           if(!marker.getTitle().equals(extractUsersNameFromNcfEmail(mEmail))){

               LatLng usersLocation = usersMarker.getPosition();

               //computing the distance between the user and the marker they tapped.
               float[] distance = new float[1];
               Location.distanceBetween(tappedLocation.latitude, tappedLocation.longitude,
                       usersLocation.latitude, usersLocation.longitude, distance);

               final double distMiles = MapUtils.distMeterstoMiles(distance[0]); // this double contains the dist in miles.


               //Let's check in real-time whether the tapped user has a marker hint entered into the system or not.
               DatabaseReference ref = FirebaseDatabase.getInstance().getReference("markerhints");

               ref.child(marker.getTitle()).addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot snapshot) {
                       HashMap<String, Object> markerHint =  (HashMap<String, Object>) snapshot.getValue();
                       String hint = String.valueOf(markerHint.get("hint"));

                       //If they have a marker hint entered into the system display their hint and the distance to them.
                       if(hint!= null){
                           marker.setSnippet(hint + " "+ "Distance: "+ distMiles+ " miles");
                           marker.showInfoWindow();
                       }

                       //If they don't have a marker hint entered, just display the distance to them.
                       else{
                           marker.setSnippet("Distance: "+ distMiles+ " miles");
                           marker.showInfoWindow();
                       }

                   }
                   @Override
                   public void onCancelled(DatabaseError databaseError) {
                       //do nothing for now.
                   }
               });

           }

           // if the user has tapped on their own location, pop up the prompt dialog to ask them for a marker hint.
           if(marker.getTitle().equals(extractUsersNameFromNcfEmail(mEmail))){
               updateMarkerHint(marker);


           }
       }

        //If this does not return false the map toolbar does not show.
        return false;
    }


    private void updateMarkerHint(final Marker marker){

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialog = inflater.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(dialog);

        final EditText userInput = (EditText) dialog
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text

                                String statusUpdate = userInput.getText().toString();

                                //This method writes the user's marker hint to the database.
                                recordUserMarkerTip(mEmail, statusUpdate);
                                Toast.makeText(DisplayActivity.this,
                                        "You just shared this hint with others: "+ statusUpdate, Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
}



