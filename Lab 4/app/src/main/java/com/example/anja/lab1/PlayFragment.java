package com.example.anja.lab1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anja on 9/24/2017.
 * Updated by Jenny 11/05/2017
 */

public class PlayFragment extends Fragment {
    private static final String TAG = "Play";
    private TextView helloTxt, scoreTxt;
    private Button playButton, resetButton;
    private int numCats;
    private int tryNum = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_fragment,container,false);

        helloTxt = view.findViewById(R.id.helloText);
        scoreTxt = view.findViewById(R.id.scoreText);
        playButton = view.findViewById(R.id.playButton);
        resetButton = view.findViewById(R.id.resetButton);
        numCats = 0;

        loadUserInfo();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                startActivity(intent);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onResetClicked(); }
        });

        return view;
    }

    private void loadUserInfo() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String helloMessage = "Hi, @" + sp.getString("username", "") + "!";
        helloTxt.setText(helloMessage);

        getCatNum();

        if (sp == null) {
            helloTxt.setText(R.string.helloMessage);
            scoreTxt.setText(R.string.scoreTrack);
        }
    }

    private void getCatNum() {
        //TODO: Calculate how many cats have been petted
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = sp.getString("username", "");
        String password = sp.getString("password", "");
        Boolean hardMode = sp.getBoolean("hard", false);

        // set cat list request URL according to game mode
        String url ="http://cs65.cs.dartmouth.edu/catlist.pl?name=";
        String requestUrl;
        if (hardMode) requestUrl = url + username + "&password=" + password + "&mode=hard";
        else requestUrl = url + username + "&password=" + password + "&mode=easy";

        final JsonArrayRequest jsObjRequest = new JsonArrayRequest (Request.Method.GET,
                requestUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        numCats = response.length();
                        String scoreTrack = "You have pet " + getPetNum(response) +
                                " out of " + numCats + " cats!";
                        scoreTxt.setText(scoreTrack);
                        tryNum = 0;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(tryNum < 3) {
                    getCatNum();
                    tryNum++;
                } else {
                    Toast.makeText(getActivity(), R.string.serverErrorMessage,
                            Toast.LENGTH_SHORT).show();
                    tryNum = 0;
                }
            }
        });
        queue.add(jsObjRequest);
    }

    private int getPetNum(JSONArray response) {
        int petNum = 0;
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject cat = response.getJSONObject(i);
                if(cat.getBoolean("petted")) { petNum++; }
            }
        } catch (JSONException e) {}

        return petNum;
    }

    private void onResetClicked() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = sp.getString("username", "");
        String password = sp.getString("password", "");
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("listReset", true);
        editor.apply();
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
                            tryNum = 0;
                            try {
                                if (response.getString("status").equals("OK")) {
                                    Toast.makeText(getActivity(), "cat list reset",
                                            Toast.LENGTH_SHORT).show();
                                    getCatNum();
                                } else {
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
                if(tryNum < 3) {
                    onResetClicked();
                    tryNum++;
                } else {
                    Toast.makeText(getActivity(), R.string.serverErrorMessage,
                            Toast.LENGTH_SHORT).show();
                    tryNum = 0;
                }

            }
        });
        queue.add(jsObjRequest);
    }

}