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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

/**
 * Created by Anja on 9/24/2017.
 */

public class PlayFragment extends Fragment {
    private static final String TAG = "Play";
    private TextView helloTxt, scoreTxt;
    private Button playButton;
    private int numCats;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_fragment,container,false);

        helloTxt = view.findViewById(R.id.helloText);
        scoreTxt = view.findViewById(R.id.scoreText);
        playButton = view.findViewById(R.id.playButton);
        numCats = 0;

        loadUserInfo();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                startActivity(intent);
            }
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
                        String scoreTrack = "You have " + numCats + " cats waiting!";
                        scoreTxt.setText(scoreTrack);
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