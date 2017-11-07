package com.example.anja.lab1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Created by jennyseong on 10/17/17.
 * Edited by jenny on 11/05/2017
 */

//Used some of this code to handle location permissions: https://stackoverflow.com/questions/34582370/how-can-i-show-current-location-on-a-google-map-on-android-marshmallow/34582595#34582595
public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient mGoogleApiClient;
    GoogleMap map;
    LocationRequest locationRequest;
    Location lastLocation;
    Marker meMarker, currentCatMarker;
    JSONArray catsJson;
    ImageView catPicture;
    TextView catName, catDistance;
    Button petButton, trackButton;
    int catId;
    String username, password;
    Marker lastClicked = null;
    Boolean lastClickedPet= false, hardMode = false;
    JSONObject cat;
    ArrayList<Marker> catMarkers;
    DecimalFormat mDistance = new DecimalFormat("#.00 m");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        username = sp.getString("username", "");
        password = sp.getString("password", "");
        hardMode = sp.getBoolean("hard", false);

        catPicture = findViewById(R.id.catPicture);
        catName = findViewById(R.id.catName);
        catDistance = findViewById(R.id.catDistance);
        trackButton = findViewById(R.id.trackButton);
        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { sendTrackRequest(); }
        });
        petButton = findViewById(R.id.petButton);
        petButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { sendPetRequest(); }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient != null && locationRequest != null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                map.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                LatLng latLng = new LatLng(43.7068, -72.2874);
                if (lastLocation != null) {
                    latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));

                RequestCatLocations();
                hideOutOfBoundsMarkers();

            } else {
                checkLocationPermission();
                RequestCatLocations();
            }
        } else {
            buildGoogleApiClient();
            map.setMyLocationEnabled(true);
        }
        map.setOnMarkerClickListener(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String update_frequency = sp.getString("update_frequency", "1000");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Integer.parseInt(update_frequency));
        locationRequest.setFastestInterval(Integer.parseInt(update_frequency));
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (meMarker != null) {
            meMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Starting Position");
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker_self",100,100)));
        meMarker = map.addMarker(markerOptions);

        hideOutOfBoundsMarkers();

        //The line below updates the camera on location change. Except it thinks you're
        //always moving unless the phone is perfectly still and doesn't let you look around the map properly. It's what's in the requirements but
        //it's really annoying so let's just comment it out and explain ourselves in the readme. It works perfectly,
        //it's just not a good feature for this type of app because it won't let you look around the map while you're
        //moving at all, it keeps going back to the current location marker. It's also not necessary since we have
        //a button that takes you back  to the current location. Leave the line commented out just so we don't
        //lose points.

        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        map.setMyLocationEnabled(true);
                    }

                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    // CAT LOCATION METHODS //
    private void RequestCatLocations() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // set cat list request URL according to game mode
        String url ="http://cs65.cs.dartmouth.edu/catlist.pl?name=";
        String requestUrl;
        if (hardMode) requestUrl = url + username + "&password=" + password + "&mode=hard";
        else requestUrl = url + username + "&password=" + password + "&mode=easy";

        JsonArrayRequest jsObjRequest = new JsonArrayRequest (Request.Method.GET,
                requestUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // ADD CATS
                        onCatListRequest(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsObjRequest);
    }

    private void onCatListRequest(JSONArray response) {
        Log.d("LOGIN_RESULT", "onLoginRequest: " + response.toString());
        if (response == null) {
            Toast.makeText(getApplicationContext(),
                    R.string.noConnectionText, Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                addCatsToMap(response);
            }
            catch (JSONException e){
                    Toast.makeText(getApplicationContext(),
                            "Unable to parse response: " + response.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

    private void addCatsToMap(JSONArray response) throws JSONException {
        catMarkers = new ArrayList<>();
        Log.d("CATLIST", "TEST");
        catsJson = response;

        for (int i = 0; i < catsJson.length(); i++) {
            JSONObject cat = catsJson.getJSONObject(i);
            MarkerOptions markerOptions = new MarkerOptions();

            String name = cat.getString("name");
            String lat = cat.getString("lat");
            String lng = cat.getString("lng");
            String petted = cat.getString("petted");

            LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            markerOptions.position(latLng);
            markerOptions.title(name);

            // NOTE for testing:
            // To test if cats have been petted with cat 1:
            // http://cs65.cs.dartmouth.edu/pat.pl?name=anja&password=anja&catid=1&lat=43.706838&lng=-72.287409
            Boolean catPetted = Boolean.parseBoolean(petted);
            if (catPetted) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker_petted));
            }
            else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker));
            }

            Marker marker = map.addMarker(markerOptions);
            catMarkers.add(marker);
            marker.setTag(cat);
        }

        hideOutOfBoundsMarkers();
    }

    private void hideOutOfBoundsMarkers() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String catRadius = sp.getString("cat_radius", "500");

        if (catMarkers != null && !catRadius.equals("infinite")) {
            for (int i = 0; i < catMarkers.size(); i++) {

                Marker catMarker = catMarkers.get(i);
                LatLng catMarkerPosition = catMarker.getPosition();

                Location catLocation = new Location("");
                catLocation.setLatitude(catMarkerPosition.latitude);
                catLocation.setLongitude(catMarkerPosition.longitude);

                float distanceFromCat;
                if (lastLocation != null) {
                    distanceFromCat = lastLocation.distanceTo(catLocation);
                } else {
                    Location placeholderLocation = new Location("");
                    placeholderLocation.setLatitude(43.70315698);
                    placeholderLocation.setLongitude(-72.29038673);
                    distanceFromCat = placeholderLocation.distanceTo(catLocation);
                }

                // update cat distance when location is updated
                if (catMarker.equals(currentCatMarker)) {
                    catDistance.setText(mDistance.format(distanceFromCat));
                }

                if (distanceFromCat < Integer.parseInt(catRadius)) {
                    catMarker.setVisible(true);
                } else {
                    //catMarker.setIcon(BitmapDescriptorFactory.defaultMarker()); uncomment to see marker change if cat is too far away
                    catMarker.setVisible(false);
                    if (catMarker.equals(currentCatMarker)) {
                        catName.setText(R.string.catNamePlaceholder);
                        catDistance.setText(R.string.catDistPlaceholder);
                        catPicture.setImageResource(R.drawable.click);
                        trackButton.setVisibility(View.INVISIBLE);
                        petButton.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // change marker icon to indicate selected marker
        // https://stackoverflow.com/questions/40840866/android-change-google-map-markers-icon-on-click
        if (!marker.equals(meMarker)) {
            currentCatMarker = marker;
            if (lastClicked != null) {
                if (lastClickedPet) {
                    lastClicked.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker_petted));
                } else {
                    lastClicked.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker));
                }
            }
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker_selected));
            lastClicked = marker;

            try {
                if (marker.getTag() != null) {
                    cat = new JSONObject(marker.getTag().toString());

                    catId = Integer.parseInt(cat.getString("catId"));
                    catName.setText(cat.getString("name"));
//                    Picasso.with(this).load(cat.getString("picUrl")).into(catPicture);
                    new DownloadImageTask(catPicture).execute(cat.getString("picUrl"));

                    Location catLocation = new Location("");
                    catLocation.setLatitude(Double.parseDouble(cat.getString("lat")));
                    catLocation.setLongitude(Double.parseDouble(cat.getString("lng")));

                    float distanceFromCat;
                    if (lastLocation != null) {
                        distanceFromCat = lastLocation.distanceTo(catLocation);
                    } else {
                        Location placeholderLocation = new Location("");
                        placeholderLocation.setLatitude(43.70315698);
                        placeholderLocation.setLongitude(-72.29038673);
                        distanceFromCat = placeholderLocation.distanceTo(catLocation);
                    }
                    catDistance.setText(mDistance.format(distanceFromCat));

                    trackButton.setVisibility(View.VISIBLE);
                    // TODO: make track button into STOP when already tracking
                    petButton.setVisibility(View.VISIBLE);
                    if (cat.getBoolean("petted")) {
                        petButton.setAlpha(.5f);
                        petButton.setClickable(false);
                        trackButton.setAlpha(.5f);
                        trackButton.setClickable(false);
                        lastClickedPet = true;

                    } else {
                        petButton.setAlpha(1f);
                        petButton.setClickable(true);
                        trackButton.setAlpha(1f);
                        trackButton.setClickable(true);
                        lastClickedPet = false;
                    }
                }
            } catch (JSONException e) {
                Log.d("ERROR", "onMarkerClick: can't parse JSON");
            }
        }
        return true;
    }

    // Image loading from URL referenced from
    // https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            catPicture.setImageBitmap(result);
        }
    }

    private void sendTrackRequest() {
        Toast.makeText(this, "track me!", Toast.LENGTH_SHORT).show();
    }

    private void sendPetRequest() {
        String latitude, longitude;
        RequestQueue queue = Volley.newRequestQueue(this);
        if (lastLocation != null) {
            latitude = Double.toString(lastLocation.getLatitude());
            longitude = Double.toString(lastLocation.getLongitude());
            // uncomment this to pet Sherlock (may need to reset list for him to show up)
//            latitude = "43.70315698";
//            longitude = "-72.29038673";
        } else {
            latitude = "43.70315698";
            longitude = "-72.29038673";
        }
//        Toast.makeText(getApplicationContext(), "Current location: " + latitude + " & " + longitude,
//                Toast.LENGTH_SHORT).show();
        String url ="http://cs65.cs.dartmouth.edu/pat.pl?name=";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.GET,
                url + username + "&password=" + password + "&catid=" + catId +
                "&lat=" + latitude + "&lng=" + longitude,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onPetRequest(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsObjRequest);
    }

    private void onPetRequest(JSONObject response) {
        Log.d("PET_RESULT", "onPetRequest: " + response.toString());
        if (response == null) {
            Toast.makeText(getApplicationContext(),
                    R.string.noConnectionText, Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                if (response.getString("status").equals("OK")) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("catName", catName.getText().toString());
                    editor.putString("catUrl", cat.getString("picUrl"));
                    editor.commit();
                    Intent intent = new Intent(this, SuccessActivity.class);
                    startActivity(intent);
                } else if (response.getString("status").equals("ERROR")) {
                    Toast.makeText(getApplicationContext(),
                            response.getString("reason"), Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(),
                        "Unable to parse response: " + response.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
}