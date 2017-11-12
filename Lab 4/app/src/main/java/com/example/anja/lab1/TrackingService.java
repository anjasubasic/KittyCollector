package com.example.anja.lab1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


/**
 * Created by Anja on 11/8/2017.
 */

public class TrackingService extends Service {
    public boolean isRunning;
    private static int FOREGROUND_ID = 143;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private LocationManager mLocationManager = null;
    private static double distanceFromCat = -1;
    private static int catId;
    private static String catName;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private Notification trackingNotification;
    private Location lastLocation;
    private int tryNum = 0;

    LocationListener mLocationListener = new LocationListener(LocationManager.NETWORK_PROVIDER);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        catId = intent.getIntExtra("Cat", -1);
        catName = intent.getStringExtra("CatName");
        lastLocation = intent.getParcelableExtra("LastLocation");
        requestDistanceFromCat(lastLocation);
        buildForegroundNotification();
        startForeground(FOREGROUND_ID, trackingNotification);
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onCreate()
    {
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);
        } catch (java.lang.SecurityException ex) {
            Log.i("TrackingService", "Failed to request location update", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("TrackingService", "Network provider does not exist, " + ex.getMessage());
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void buildForegroundNotification() {
        Notification notification;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        String distanceMessage = String.format("%.2f", distanceFromCat) + " m";
        String title = "Tracking " + catName;
        Intent stopIntent = new Intent(this, StopReceiver.class);
        Intent notificationIntent = new Intent(this, MapActivity.class);
        PendingIntent pendingIntentStop = PendingIntent.getBroadcast(this, 12, stopIntent, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        builder.setOngoing(true)
                .setContentTitle(title)
                .setContentText(distanceMessage)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.catmarker_selected)
                .setTicker("Ticker")
                .addAction(R.drawable.cross, "Stop", pendingIntentStop);

        trackingNotification = builder.build();
    }

    public static class StopReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            context.stopService(new Intent(context, TrackingService.class));
        }
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location lastLocation;

        public LocationListener(String provider)
        {
            Log.e("TrackingService", "LocationListener " + provider);
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e("TrackingService", "onLocationChanged: " + location);
            lastLocation.set(location);
            requestDistanceFromCat(location);
        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle bundle) {
            Log.e("TrackingService", "onStatusChanged: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e("TrackingService", "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e("TrackingService", "onProviderEnabled: " + provider);
        }
    }

    private void requestDistanceFromCat(Location location) {
        if (location == null) {
            Location placeholderLocation = new Location("");
            placeholderLocation.setLatitude(43.70315698);
            placeholderLocation.setLongitude(-72.29038673);
            location = placeholderLocation;
        }
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sp.getString("username", "");
        String password = sp.getString("password", "");
        RequestQueue queue = Volley.newRequestQueue(this);

        // set cat list request URL according to game mode
        String url ="http://cs65.cs.dartmouth.edu/track.pl?name=";
        String requestUrl;
        requestUrl = url + username + "&password=" + password + "&catid=" + catId + "&lat=" + latitude + "&lng=" + longitude;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.GET,
                requestUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onDistanceRequest(response);
                        tryNum = 0;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(tryNum < 3) {
                    requestDistanceFromCat(lastLocation);
                    tryNum++;
                } else {
                    Toast.makeText(getApplicationContext(), R.string.serverErrorMessage,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        queue.add(jsObjRequest);
    }

    private void onDistanceRequest(JSONObject response) {
        if (response == null) {
            Toast.makeText(getApplicationContext(),
                    R.string.noConnectionText, Toast.LENGTH_SHORT).show();
        }

        else {
            updateNotificationDistance(response);
        }
    }

    private void updateNotificationDistance(JSONObject response) {
        String notificationText = "Unable to update location.";

        try {
            if (response.getString("status").equals("OK")) {
                distanceFromCat = response.getDouble("distance");
                notificationText = String.format("%.2f", distanceFromCat) + " m";

            } else if (response.getString("status").equals("ERROR")) {
                Toast.makeText(getApplicationContext(),
                        response.getString("reason"), Toast.LENGTH_SHORT).show();
            }
        }

        catch (JSONException e) {
            Toast.makeText(getApplicationContext(),
                    "Unable to parse response: " + response.toString(),
                    Toast.LENGTH_SHORT).show();
        }

        builder.setContentText(notificationText);
        notificationManager.notify(FOREGROUND_ID, builder.build());
    }

    @Override
    public void onDestroy()
    {
        Log.e("TrackingService", "onDestroy");
        isRunning = false;
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.i("TrackingService", "Failed to remove location listener", ex);
            }
        }
    }
}