package com.example.anja.lab1;

import android.content.Context;
import android.content.DialogInterface;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;


/**
 * Created by jennyseong on 10/17/17.
 */

//Used some of this code to handle location permissions: https://stackoverflow.com/questions/34582370/how-can-i-show-current-location-on-a-google-map-on-android-marshmallow/34582595#34582595
public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient mGoogleApiClient;
    GoogleMap map;
    LocationRequest locationRequest;
    Location lastLocation;
    Marker meMarker;
    JSONArray catsJson;
    ImageView catPicture;
    TextView catName, catDistance;
    Button petButton;
    int catId;
    String username, password;
    Marker lastClicked = null;
    Boolean lastClickedPet = false;

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

        catPicture = findViewById(R.id.catPicture);
        catName = findViewById(R.id.catName);
        catDistance = findViewById(R.id.catDistance);
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
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                map.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                LatLng latLng = new LatLng(43.7068, -72.2874);
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                }
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));

                RequestCatLocations();

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
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
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
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_self));
        meMarker = map.addMarker(markerOptions); // no need to add a marker for current location, google maps already adds the current location (little blue thing)
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
            // TODO: The following returns catlist for the easy mode. Still need to implement hard mode
            String url ="http://cs65.cs.dartmouth.edu/catlist.pl?name=";
            JsonArrayRequest jsObjRequest = new JsonArrayRequest (Request.Method.GET,
                    url + username + "&password=" + password, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // ADD CATS
                            onCatlistRequest(response);
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

    private void onCatlistRequest(JSONArray response) {
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
        Log.d("CATLIST", "TEST");
        catsJson = response;
        //TODO: Add icons to markers, fix bug with camera

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
            marker.setTag(cat);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // change marker icon to indicate selected marker
        // https://stackoverflow.com/questions/40840866/android-change-google-map-markers-icon-on-click
        if (lastClicked != null) {
            if(lastClickedPet) {
                lastClicked.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker_petted));
            } else {
                lastClicked.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker));
            }
        }
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker_selected));
        lastClicked = marker;

        try {
            JSONObject cat = new JSONObject(marker.getTag().toString());

            catId = Integer.parseInt(cat.getString("catId"));
            catName.setText(cat.getString("name"));
            new DownloadImageTask(catPicture).execute(cat.getString("picUrl"));

            // distance calculating part referenced from
            // https://stackoverflow.com/questions/14394366/find-distance-between-two-points-on-map-using-google-map-api-v2
            Double catLng = Double.parseDouble(cat.getString("lng"));
            Double catLat = Double.parseDouble(cat.getString("lat"));
            LatLng catLocation = new LatLng(catLat, catLng);
            // TODO: Use actual location
            LatLng myLocation = new LatLng(43.7070, -72.2870);
            float[] results = new float[1];
            Location.distanceBetween(catLocation.latitude, catLocation.longitude,
                                 myLocation.latitude, myLocation.longitude, results);
            catDistance.setText(String.valueOf(results[0]));
            //TODO: Format string

            petButton.setVisibility(View.VISIBLE);
            if (cat.getBoolean("petted")) {
                petButton.setAlpha(.5f);
                petButton.setClickable(false);
                lastClickedPet = true;

            } else {
                petButton.setAlpha(1f);
                petButton.setClickable(true);
                lastClickedPet = false;
            }

        } catch (JSONException e) {
            Log.d("ERROR", "onMarkerClick: can't parse JSON");
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

    private void sendPetRequest() {
        String latitude, longitude;
        RequestQueue queue = Volley.newRequestQueue(this);
        if (lastLocation != null) {
            latitude = Double.toString(lastLocation.getLatitude());
            longitude = Double.toString(lastLocation.getLongitude());
        } else {
            latitude = "43.7048";
            longitude = "-72.2889";
        }
        Toast.makeText(getApplicationContext(), "Current location: " + latitude + " & " + longitude,
                Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Meow! I like you!",
                            Toast.LENGTH_SHORT).show();
                    //TODO: Move to Activity
                    lastClicked.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.catmarker_petted));

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
}
