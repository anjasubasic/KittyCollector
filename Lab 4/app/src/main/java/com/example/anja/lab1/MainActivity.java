package com.example.anja.lab1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager)findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        loadPreferences();
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new PlayFragment(), "Play");
        adapter.addFragment(new HistoryFragment(), "History");
        adapter.addFragment(new SettingsFragment(), "Settings");

        viewPager.setAdapter(adapter);
    }

    private void loadPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("freshLogin", false)) {
            SharedPreferences.Editor editor = sp.edit();
            try {
                JSONObject loginResponse = new JSONObject(sp.getString("loginResponse", ""));
                editor.putString("full_name", loginResponse.getString("full_name"));
                editor.putString("photo", loginResponse.getString("photo"));
                editor.putString("update_frequency", loginResponse.getString("update_frequency"));
                editor.putBoolean("hard", loginResponse.getBoolean("hard"));
                editor.putString("cat_radius", loginResponse.getString("cat_radius"));
                editor.putBoolean("freshLogin", false);
                editor.commit();
            } catch (JSONException e) {
                Log.d("PREF_UPDATE", "loadPreferences: Unable to parse response");
            }
        }
    }
}
