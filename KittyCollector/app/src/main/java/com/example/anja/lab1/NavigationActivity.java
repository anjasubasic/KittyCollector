package com.example.anja.lab1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by jennyseong on 10/9/17.
 * NavigationActivity was created to determine the starting activity according to conditions.
 * https://stackoverflow.com/questions/44984200/how-to-choose-starting-activity-based-on-condition
 */

public class NavigationActivity extends AppCompatActivity {
    public Intent intent;
    private int tryNum = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        Log.d("LOGIN", "onCreate: login valid? " + loginValid);

        if (sp.getBoolean("login", false)) {
            intent = new Intent(this, MainActivity.class);
        }
        else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void checkLogin() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String username = sp.getString("username", "");
        final String password = sp.getString("password", "");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://cs65.cs.dartmouth.edu/profile.pl?name=";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET,
                url + username + "&password=" + password, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("LOGIN_RESULT", "onLoginRequest: " + response.toString());
                        try {
                            response.getString("error");
                            Toast.makeText(getApplicationContext(), R.string.credentials,
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        } catch (JSONException e) { }
                        tryNum = 0;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (tryNum < 3) {
                    checkLogin();
                    tryNum++;
                } else {
                    Toast.makeText(getApplicationContext(), R.string.serverErrorMessage,
                            Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("login", false);
                    editor.apply();
                    tryNum = 0;
                }
            }
        });
        queue.add(jsObjRequest);
    }
}
