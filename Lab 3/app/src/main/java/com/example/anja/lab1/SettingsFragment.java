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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anja on 9/24/2017.
 * Edited by Jenny 10/10/2017.
 */

public class SettingsFragment extends android.support.v4.app.DialogFragment {
    private TextView fullnameTxt, usernameTxt;
    private ImageView profilePhoto;
    private Button resetButton, passwordButton;
    int requestCode = 123;
    private Fragment fragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings_fragment, container, false);

        profilePhoto = view.findViewById(R.id.profilePicture);
        fullnameTxt = view.findViewById(R.id.fullnameText);
        usernameTxt = view.findViewById(R.id.usernameText);
        Button signOutButton = view.findViewById(R.id.signOutButton);
        passwordButton = view.findViewById(R.id.pwdChangeButton);
        resetButton = view.findViewById(R.id.reset);
        fragment = this;

        setProfile();
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onSignOutClicked(); }
        });

        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                PasswordChangeDialog myDialog = new PasswordChangeDialog();
                myDialog.setTargetFragment(fragment, requestCode);
                myDialog.show(manager, "ChangePasswordDialog");
            }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ConfirmPasswordDialog.requestCode) {
            String newPassword = PasswordChangeDialog.dialogPassword;
            String currPassword = PasswordChangeDialog.currentPassword;
            updatePassword(currPassword, newPassword);
        }
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        postUserInfo();
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

    // postUserInfo: Volley POST request, referenced from class example
    public void postUserInfo() {
        String url = "http://cs65.cs.dartmouth.edu/profile.pl";
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        JSONObject userInfo = buildJSONObject();
        Log.d("SIGN_OUT", "postUserInfo: " + userInfo.toString());

        if (userInfo == null) { return; }
        else {
            // Request a string response from the provided URL.
            JsonObjectRequest joRequest = new JsonObjectRequest(url,  // POST is presumed
                    userInfo,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            postResultsToUI(response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    postResultsToUI("Error" + error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        // params.put("Accept", "application/json");
                        params.put("Accept-Encoding", "identity");
                        // params.put("Content-Type", "application/json");
                        return params;
                    }
                }
            };
            queue.add(joRequest);
        }
    }

    private void postResultsToUI(final String res) {
        if (res == null)
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noConnectionText),
                    Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(getActivity().getApplicationContext(), res,
//                    Toast.LENGTH_SHORT).show();
    }

    private JSONObject buildJSONObject(){
        JSONObject json = new JSONObject();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Log.d("REQUEST", "buildJSONObject: " + sp.getString("loginResponse", ""));
        try {
            JSONObject loginResponse = new JSONObject(sp.getString("loginResponse", ""));
            // things from the original login
            json.put("name", sp.getString("username", ""));
            json.put( "password", sp.getString("password", ""));
            json.put("full_name", loginResponse.get("full_name"));
            json.put("photo", loginResponse.get("photo"));
            // Add settings
            json.put("update_frequency", sp.getString("update_frequency", "1000"));
            json.put("hard", sp.getBoolean("hard", false));
            json.put("cat_radius", sp.getString("cat_radius", "500"));
        }
        catch(JSONException e){
            Toast.makeText(getActivity().getApplicationContext(),
                    "Invalid JSON" + e.toString(), Toast.LENGTH_LONG).show();
            return null;
        }
        return json;
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

    public void updatePassword(String currentPass, final String newPass) {
        String url = "http://cs65.cs.dartmouth.edu/changepass.pl?name=";
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = sp.getString("username", "");

        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.GET,
                url + username + "&password=" + currentPass + "&newpass=" + newPass,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("PWD_RESPONSE", "onResponse: " + response.toString());
                        if (response == null) {
                            Toast.makeText(getActivity(),
                                    R.string.noConnectionText, Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                if (response.getString("status").equals("OK")) {
                                    Toast.makeText(getActivity(), "Password changed",
                                            Toast.LENGTH_SHORT).show();
                                    // don't forget to save new password to shared preferences!!
                                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("password", newPass);
                                    editor.commit();
                                } else {
                                    if (response.getString("code").equals("AUTH_FAIL")) {
                                        Toast.makeText(getActivity(),
                                                response.getString("error"), Toast.LENGTH_LONG).show();
                                    } else if (response.getString("status").equals("ERROR")) {
                                        Toast.makeText(getActivity(),
                                                response.getString("error"), Toast.LENGTH_LONG).show();
                                    }
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