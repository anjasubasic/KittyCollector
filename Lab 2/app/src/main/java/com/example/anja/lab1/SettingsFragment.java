package com.example.anja.lab1;

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
        // TODO: Pull data from sign-in request
        profilePhoto.setImageResource(R.drawable.shiba);
        fullname.setText("Jane Doe");
        username.setText("@jane");
    }

    private void onSignOutClicked() {
        // TODO: Link sign out feature
        Log.d("STATE", "onSignOutClicked");
    }

    private void onAboutClicked() {
        // TODO: Need to make it show that it was clicked!
        // TODO: Lead to different page
        Log.d("STATE", "onAboutClicked");
    }

    private void onAlertClicked() {
        // TODO: Need to make it show that it was clicked!
        // TODO: Lead to different page
        Log.d("STATE", "onAlertClicked");
    }
}