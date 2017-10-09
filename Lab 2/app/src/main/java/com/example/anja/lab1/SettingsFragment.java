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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Anja on 9/24/2017.
 * Edited by Jenny 10/02/2017.
 */

public class SettingsFragment extends android.support.v4.app.DialogFragment {
    private TextView fullname, username;
    private ImageView profilePhoto;
    private Button signOutButton;
    private LinearLayout about, alert;
    private Switch privacy;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings_fragment, container, false);

        profilePhoto = view.findViewById(R.id.profilePicture);
        fullname = view.findViewById(R.id.fullnameText);
        username = view.findViewById(R.id.usernameText);
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
        loadProfile();
        loadPreferences();
    }

    private void loadProfile() {
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

    private void loadPreferences() {
        SharedPreferences sp = getActivity().getSharedPreferences(
                getString(R.string.saved_info), Context.MODE_PRIVATE);
        username.setText(sp.getString("character name", ""));
        fullname.setText(sp.getString("full name", ""));

        if (sp != null) {
            fullname.setText("Jane Doe");
            username.setText("@jane");
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