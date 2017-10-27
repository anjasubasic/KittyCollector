package com.example.anja.lab1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anja on 10/4/2017.
 * Edited by Jenny 10/10/2017
 */

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    EditText username, password;
    TextView createAccountTxtView;
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkPermissions();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        checkBox = findViewById(R.id.remember);
        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onLoginClicked(); }
        });

        createAccountTxtView = (TextView) findViewById(R.id.create_account_txt);
        createAccountTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onNewAccountClicked(); }
        });

        loadPreferences();
    }

    /**
     * Code to check for runtime permissions.
     * (code taken from camera example by Varun)
     */
    private void checkPermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        }else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)||shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    //Show an explanation to the user *asynchronously*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This permission is important for the app.")
                            .setTitle("Important permission required");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
                            }

                        }
                    });
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
                }
                else {
                    //Never ask again and handle your app without permission.
                }
            }
        }
    }

    private void onNewAccountClicked() {
        android.support.v4.app.Fragment registerAccountFragment = new RegisterAccountFragment();
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // TODO: adding to backstack causes endless backstack when you go back and forth
        ft.addToBackStack(null);
        ft.add(R.id.fragment_container, registerAccountFragment).commit();
    }

    private void onLoginClicked() {
        writePreferences();
        sendLoginRequest(username.getText().toString(), password.getText().toString());
    }

    private void sendLoginRequest(String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://cs65.cs.dartmouth.edu/profile.pl?name=";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.GET,
                url + username + "&password=" + password, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onLoginRequest(response);
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

    private void onLoginRequest(JSONObject response) {
        Log.d("LOGIN_RESULT", "onLoginRequest: " + response.toString());
        if (response == null) {
            Toast.makeText(getApplicationContext(),
                    R.string.noConnectionText, Toast.LENGTH_SHORT).show();
        }
        else {
            Boolean noError = false;
            try {
                getError(response);
            }
            catch (JSONException e) {
                noError = true;
            }
            try {
                doLogIn(response);
            }
            catch (JSONException e){
                if (noError == true) {
                    Toast.makeText(getApplicationContext(),
                            "Unable to parse response: " + response.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getError(JSONObject response) throws JSONException {
        Log.d("LOGIN", "getError: ");
        Toast.makeText(getApplicationContext(),
                "Error: " + response.getString("error"), Toast.LENGTH_SHORT).show();
    }

    private void doLogIn(JSONObject response) throws JSONException {
        Log.d("LOGIN", "doLogIn: ");
        // save user information from response
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("login", true);
        editor.putString("username", response.getString("name"));
        editor.putString("password", response.getString("password"));
        editor.putBoolean("freshLogin", true);
        editor.putString("loginResponse", response.toString());
        editor.commit();

        // move to main activity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        LoginActivity.this.startActivity(intent);
        finish();
    }

    private void loadPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        username.setText(sp.getString("username", ""));
        password.setText(sp.getString("password", ""));
        if(sp.getBoolean("remember", false)) { checkBox.setChecked(true); }
    }

    private void writePreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        if(checkBox.isChecked()) {
            editor.putString("username", username.getText().toString());
            editor.putString("password", password.getText().toString());
            editor.putBoolean("remember", true);
        }
        else {
            editor.putBoolean("remember", false);
        }
        editor.commit();
    }
}
