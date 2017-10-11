package com.example.anja.lab1;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Anja on 9/24/2017.
 */

public class ScoreFragment extends Fragment {
    private static final String TAG = "Score";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.score_fragment,container,false);

        return view;
    }

}
