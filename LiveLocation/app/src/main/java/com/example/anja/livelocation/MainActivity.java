package com.example.anja.livelocation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener{

    private static final String SERVER_LAT_KEY = "lat";
    private static final String SERVER_LNG_KEY = "lng";
    private static final int MY_PERMISSIONS_REQUEST = 301;
    private GoogleMap map;
    private Marker own;
    private Marker server;
    private boolean zoomedOut = true;
    private LatLng current;
    private Button send;
    private Button receive;


    OnReceivedFromServer recvTask;
    SendToServer sendTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        send = (Button) findViewById(R.id.send);
        receive = (Button) findViewById(R.id.receive);
        send.setEnabled(false);
        receive.setEnabled(false);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Either you are sending your location to the server or you are receiving
                // location updates from the server. So destroy the recvTask if 'send' is clicked
                // and vice-versa.
                if (sendTask == null) {
                    if (recvTask != null) {
                        recvTask.cancel(true);
                        recvTask = null;
                    }
                    sendTask = new SendToServer();
                    sendTask.execute();
                }
            }
        });
        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recvTask == null) {
                    if (sendTask != null) {
                        sendTask.cancel(true);
                        sendTask = null;
                    }
                    recvTask = new OnReceivedFromServer();
                    recvTask.execute();
                }
            }
        });
        setupMap();
    }

    // The Google Map is added programmatically and a container for it is declared in the
    // activity.xml. There are other ways to do this.
    private void setupMap(){
        if (map == null) {
            SupportMapFragment mapFrag = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            mapFrag.getMapAsync(this);
            if (map != null) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }

    // You received a GoogleMap object.
    // Experiment with cooler looking map types that MAP_TYPE_NORMAL.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        send.setEnabled(true);
        receive.setEnabled(true);
        getLocation();
    }

    public void requestPermissions(){
        // Here, thisActivity is the current activity
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                    // permissions not obtained
                    Toast.makeText(this,"failed request permission!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // You NEED to first check for location permissions before using the location.
    // Make sure you declare the corresponding permission in your manifest.
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = getCriteria();
            String provider;
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                provider = locationManager.getBestProvider(criteria, true);
                Location l = locationManager.getLastKnownLocation(provider);
                if(l!=null){
                    updateWithNewLocation(l);
                }
                locationManager.requestLocationUpdates(provider, 0, 0, this);
            }
        }
    }

    // Application criteria for selecting a location provider. See line 158 "getBestProvider"
    // https://developer.android.com/reference/android/location/Criteria.html
    private Criteria getCriteria(){
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        return criteria;
    }

    // Put the marker at given location and zoom into the location
    private void updateWithNewLocation(Location location) {
        if (location != null) {
            LatLng l = fromLocationToLatLng(location);
            current = l;
            drawMarker(l, false);
            moveToCurrentLocation(l);
        }
    }

    // Remove old marker and place new marker.
    private void drawMarker(LatLng l, boolean serverLoc){
        if(serverLoc) {
            if (server != null)
                server.remove();
            server = map.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        } else{
            if (own != null)
                own.remove();
            own = map.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    // LatLng stores the "location" as two doubles, latitude and longitude.
    public static LatLng fromLocationToLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onLocationChanged(Location location) {
        // Called whenever the location is changed.
        updateWithNewLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Called when the provider status changes.
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // The AsyncTask that receives the latitude and longitude from the server and puts a marker at that location.
    private class OnReceivedFromServer extends AsyncTask<Void, Double, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
            if(values.length >=2 ){
                drawMarker(new LatLng(values[0], values[1]), true);
                moveToCurrentLocation(new LatLng(values[0], values[1]));
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String res;
            // Keep receiving the updated location from the server every 2 seconds.
            while (!isCancelled()) {
                try {
                    URL url = new URL("http://cs65.cs.dartmouth.edu/profile.pl?name=demo&password=1234" );
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    InputStream in = conn.getInputStream();
                    res = readStream(in);
                    JSONObject o = new JSONObject(res);
                    if(o != null){
                        publishProgress(o.getDouble(SERVER_LAT_KEY),o.getDouble(SERVER_LNG_KEY));
                        //publishProgress(43.44,44.33); // to test multiple markers
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onCancelled() {
            //when cancelled, simply stop
        }

    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    // AsyncTask to send the sever location updates every 2s.
    private class SendToServer extends AsyncTask<Void, Double, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
            if(values.length >=2 ){
                drawMarker(new LatLng(values[0], values[1]), true);
                moveToCurrentLocation(new LatLng(values[0], values[1]));
                String text = String.valueOf(values[0]).concat(String.valueOf(values[1]));
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String res = null, req = null;
            while (!isCancelled()) {
                // Right now, we just post the latitude and longitude as POST parameters.
                // The server echoes them back to anyone who wants to receive the location.
                try {
                    // the 'current' global variable stores the last known location.
                    // it is updated in onLocationChanged.
                    if(current != null) {
                        URL url = new URL("http://cs65.cs.dartmouth.edu/profile.pl");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        req = buildJSONReq(current);
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept-Encoding", "identity");
                        conn.setFixedLengthStreamingMode(req.length());
                        OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                        out.write(req.getBytes());
                        out.flush();
                        out.close();
                        // after sending our location to the server, also reflect it on the screen.
                        // might be redundant since we update our location every time the
                        // onLocationChanged() callback is called.
                        publishProgress(current.latitude,current.longitude);
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            //when cancelled, simply stop
        }

        private String buildJSONReq(LatLng location){
            JSONObject o = new JSONObject();
            if(location != null){
                try {
                    o.put("name", "demo");
                    o.put(SERVER_LAT_KEY, location.latitude);
                    o.put(SERVER_LNG_KEY, location.longitude);
                    o.put("password", "1234");
                }
                catch( JSONException e){
                    Log.d("JSON", e.toString());
                }
            }
            return o.toString();
        }

    }

    // If the given location is visible on the mobile screen (visible region), do not zoom in.
    // zoomedOut is a variable that is initialized to True and ensures that the zoom level is set.
    private void moveToCurrentLocation(LatLng currentLocation)
    {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        if(!bounds.contains(currentLocation) || zoomedOut ){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 1 second.
            map.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
            zoomedOut = false;
        }
    }

}