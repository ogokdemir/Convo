package com.example.ozangokdemir.convomap.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.example.ozangokdemir.convomap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FirebaseUtils {

    private static final String TAG = FirebaseUtils.class.getSimpleName();
    private  final Context mContext;  //context in which an object of this class created.
    private  final HashMap<String, Marker> mMarkers; //passed to the constructor from the context in which this utils object is created.
    private  final GoogleMap mMap; //passed to the constructor from the context in which this utils object is created.

    /**
     *
     * @param context context in which this utils object is used.
     * @param markers an hashmap of keys and markers, passed from the context that uses this utils object.
     * @param map a google map object passed by the context that used this firebase utils object.
     */
    public FirebaseUtils(Context context, HashMap<String, Marker> markers, GoogleMap map){
        mContext = context;
        mMarkers = markers;
        mMap = map;
    }

    /**
     *
     * @param email the email of the user that just logged into the ConvoMap.
     * @param password the password of the user that just logged into the ConvoMap.
     */
    public void loginToFirebase(final String email, String password) {

        //params have to be final because they are used in an inner class.
        final String mEmail = email;
        final String mPassword = password;

        // Authenticate with Firebase and subscribe to updates
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            //If the app successfully authenticated the user
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    subscribeToUpdates(mEmail); //subcribe to the database so that the map dipslays their locations.


                    Toast.makeText(mContext, "Tap on the person you want to chat with and use the blue arrow icon on the bottom right!",
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, "firebase auth success");
                } else {
                    Log.d(TAG, "firebase auth failed");
                    //notify the user that their credentials are wrong.
                    Toast.makeText(mContext,
                            "Hmm, looks like your email or password is wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Starts observing the database and updates the map accordingly.
     * @param email takes the email from the loginToFirebase method and passes it along to the MapUtils.setMarker method.
     */
    public void subscribeToUpdates(final String email) {

        //get a hold of the firebase database so that we can subscribe to changes in the location.
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.firebase_path));

        ref.addChildEventListener(new ChildEventListener() {

            //Called when a new user just become online.
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                MapUtils.setMarker(dataSnapshot, mMarkers, mMap, email);
            }

            //Updated every tine a user's location changes. Everyone is notified.
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                MapUtils.setMarker(dataSnapshot, mMarkers, mMap, email);
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
                    Toast.makeText(mContext,
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

    /**
     * This should probably be in a util class but let it stay around for now. Extracts name and last name from ncf email as key to Firebase.
     * @param email NCF email address of the user.
     * @return extracts their name and last name from the NCF email address and returns it.
     */
    public static String extractUsersNameFromNcfEmail(String email){
        String[] chunks = email.split("@");
        String[] fNamelName = chunks[0].split("\\.");
        String firstName = fNamelName[0].substring(0,1).toUpperCase()+fNamelName[0].substring(1);
        String lastName =  fNamelName[1].substring(0,1).toUpperCase()+fNamelName[1].substring(1,fNamelName[1].length()-2);
        return firstName+" "+lastName;
    }

}
