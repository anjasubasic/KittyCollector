package com.example.anja.lab1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Edited by Jenny 11/06/2017
 */

public class HistoryFragment extends ListFragment {
    private static final String TAG = "History";
    private int tryNum = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment,container,false);
        getCatList();
        return view;
    }

    // gets the cat list and updates the listView
    // was made public because it is used in the MainActivity's ViewPager
    public void getCatList() {
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
                        setListAdapter(new CatListAdapter(getActivity(), response));
                        tryNum = 0;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(tryNum < 3) {
                    getCatList();
                    tryNum++;
                } else {
                    Toast.makeText(getActivity(), R.string.serverErrorMessage,
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
        queue.add(jsObjRequest);
    }

}
