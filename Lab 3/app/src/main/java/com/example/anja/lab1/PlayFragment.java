package com.example.anja.lab1;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Anja on 9/24/2017.
 */

public class PlayFragment extends Fragment {
    private static final String TAG = "Play";
    private TextView helloTxt, scoreTxt;
    private Button playButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_fragment,container,false);

        helloTxt = view.findViewById(R.id.helloText);
        scoreTxt = view.findViewById(R.id.scoreText);
        playButton = view.findViewById(R.id.playButton);

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
        String scoreTrack = "You have befriended " + sp.getString("score", "0") + " cats.";
        helloTxt.setText(helloMessage);
        scoreTxt.setText(scoreTrack);

        if (sp == null) {
            helloTxt.setText(R.string.helloMessage);
            scoreTxt.setText(R.string.scoreTrack);
        }
    }

}