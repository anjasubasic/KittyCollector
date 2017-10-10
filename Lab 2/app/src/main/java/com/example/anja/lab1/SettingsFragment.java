package com.example.anja.lab1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Anja on 9/24/2017.
 * Edited by Jenny 10/02/2017.
 */

public class SettingsFragment extends android.support.v4.app.DialogFragment {
    private TextView fullnameTxt, usernameTxt;
    private ImageView profilePhoto;
    private Button signOutButton;
    private LinearLayout about, alert;
    private Switch privacy;

    // Get username and password from activity start intent
    // https://stackoverflow.com/questions/2405120/how-to-start-an-intent-by-passing-some-parameters-to-it
//    Intent myIntent = getActivity().getIntent();
//    String username = myIntent.getStringExtra("username");
//    String password= myIntent.getStringExtra("password");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings_fragment, container, false);

        profilePhoto = view.findViewById(R.id.profilePicture);
        fullnameTxt = view.findViewById(R.id.fullnameText);
        usernameTxt = view.findViewById(R.id.usernameText);
        signOutButton = view.findViewById(R.id.signOutButton);

        setProfile();
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onSignOutClicked(); }
        });

        about = view.findViewById(R.id.aboutSettings);
        alert = view.findViewById(R.id.alertSettings);
        privacy = view.findViewById(R.id.privacySwitch);

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onAboutClicked(); }
        });
        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onAlertClicked(); }
        });
        privacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Log.d("PRIVACY", "public");
                } else {
                    // The toggle is disabled
                    Log.d("PRIVACY", "private");
                }
            }
        });

        return view;
    }

    private void setProfile() {
        loadProfilePic();
        loadUserInfo();
    }

    private void loadProfilePic() {
        try {
            FileInputStream inputImage = getContext().openFileInput(getString(R.string.profileFileName));
            Bitmap profile = BitmapFactory.decodeStream(inputImage);
            profilePhoto.setImageBitmap(profile);
            inputImage.close();
        }
        // get default profile photo if photo file not found
        catch (IOException e) {
            profilePhoto.setImageResource(R.drawable.shiba);
        }
    }

    private void loadUserInfo() {
        SharedPreferences sp = getActivity().getSharedPreferences(
                getString(R.string.saved_info), Context.MODE_PRIVATE);
        String username = sp.getString("character name", "");
        String fullname = sp.getString("full name", "");
        fullnameTxt.setText(fullname);
        usernameTxt.setText(username);

//        RequestQueue queue = Volley.newRequestQueue(this.getContext());
//        String url ="http://cs65.cs.dartmouth.edu/nametest.pl?";
//        JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                (Request.Method.GET, url + username + "&" + password, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                    // get stuff
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        //TODO: what's the proper way to handle this?
//                    }
//                });
//        queue.add(jsObjRequest);

        if (sp != null) {
            fullnameTxt.setText("Jane Doe");
            usernameTxt.setText("@jane");
        }
    }

    // OnSignOutClicked: Close main activity, clear back stack and restart the login activity
    private void onSignOutClicked() {
        Log.d("STATE", "onSignOutClicked");
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        // Clearing back stack code referenced from
        // https://stackoverflow.com/questions/5794506/android-clear-the-back-stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void onAboutClicked() {
        // TODO: Lead to different page
        Log.d("STATE", "onAboutClicked");
    }

    private void onAlertClicked() {
        // TODO: Lead to different page
        Log.d("STATE", "onAlertClicked");
    }
}