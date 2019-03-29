package com.example.ozangokdemir.convomap.utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

public class MapUtils {

    /**
     *
     * @param dataSnapshot the datasnapshot that the firebase database listener returns.
     * @param markers a hashmap of keys and titles.
     * @param map the google map object.
     */
    public static void setMarker(DataSnapshot dataSnapshot, HashMap<String, Marker> markers, GoogleMap map) {
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
        if (!markers.containsKey(key)) {
            markers.put(key, map.addMarker(new MarkerOptions().title(key).position(location)));

            //If the location of an already online user (marker) is updated, just set the marker to new location.
        } else {
            markers.get(key).setPosition(location);
        }

        // LatLngBounds.Builder takes in a bunch of markers and focuses the map to display all of them at once.
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers.values()) {
            builder.include(marker.getPosition());
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));

    }

    /**
     * Helper method for converting the distance in meters to miles.
     *
     * @param distInMeters Distance in meters.
     * @return distance in meters converted to miles. type: double
     */
    public static double distMeterstoMiles(float distInMeters){
        double dist = distInMeters*0.000621371192; //convert the distance to miles.
        double roundOff = Math.round(dist * 100.0) / 100.0; // round the distance up to two decimal digits.
        return roundOff;
    }
}
