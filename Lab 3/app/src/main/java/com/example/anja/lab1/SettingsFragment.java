package com.example.anja.lab1;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Anja on 9/24/2017.
 * Edited by Jenny 10/10/2017.
 */

public class SettingsFragment extends android.support.v4.app.DialogFragment {
    private TextView fullnameTxt, usernameTxt;
    private ImageView profilePhoto;
    private Button resetButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings_fragment, container, false);

        profilePhoto = view.findViewById(R.id.profilePicture);
        fullnameTxt = view.findViewById(R.id.fullnameText);
        usernameTxt = view.findViewById(R.id.usernameText);
        Button signOutButton = view.findViewById(R.id.signOutButton);
        resetButton = view.findViewById(R.id.reset);

        setProfile();
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onSignOutClicked(); }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onrResetClicked(); }
        });

        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new PrefsFragment());
//        transaction.addToBackStack(null);
        transaction.commit();

        return view;
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
        }
    }

    private void setProfile() {
        loadProfilePic();
        loadUserInfo();
    }

    private void loadProfilePic() {
        // TODO: load profile from image fetched from server
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String fullname = sp.getString("full_name", "No Name Set");
        String username = "@" + sp.getString("username", "");
        fullnameTxt.setText(fullname);
        usernameTxt.setText(username);

        if (sp == null) {
            fullnameTxt.setText("Jane Doe");
            usernameTxt.setText("@jane");
        }
    }

    // OnSignOutClicked: Close main activity, clear back stack and restart the login activity
    private void onSignOutClicked() {
        Log.d("STATE", "onSignOutClicked");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        if(!sp.getBoolean("remember", false)) {
            editor.clear();
            getContext().deleteFile(getString(R.string.profileFileName));
        } else {
            String username = sp.getString("username", "");
            String password = sp.getString("password", "");
            Boolean remember = sp.getBoolean("remember", false);
            editor.clear();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.putBoolean("remember", remember);
        }
        editor.putBoolean("login", false);
        editor.commit();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        // Clearing back stack code referenced from
        // https://stackoverflow.com/questions/5794506/android-clear-the-back-stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void onrResetClicked() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = sp.getString("username", "");
        String password = sp.getString("password", "");
        RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url ="http://cs65.cs.dartmouth.edu/resetlist.pl?name=";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.GET,
                    url + username + "&password=" + password, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (response == null) {
                                Toast.makeText(getActivity(),
                                        R.string.noConnectionText, Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    if (response.getString("status").equals("OK")) {
                                        Toast.makeText(getActivity(), "cat list reset",
                                                Toast.LENGTH_SHORT).show();
                                    } else if (response.getString("status").equals("ERROR")) {
                                        Toast.makeText(getActivity(),
                                                response.getString("error"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(getActivity(),
                                            "Unable to parse response: " + response.toString(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(), "Error: " + error.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        queue.add(jsObjRequest);
    }
}