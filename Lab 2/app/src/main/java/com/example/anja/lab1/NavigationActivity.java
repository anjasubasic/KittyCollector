package com.example.anja.lab1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by jennyseong on 10/9/17.
 * NavigationActivity was created to determine the starting activity according to conditions.
 * https://stackoverflow.com/questions/44984200/how-to-choose-starting-activity-based-on-condition
 */

public class NavigationActivity extends AppCompatActivity {
    public Intent intent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);

        Log.d("LOGIN", "onCreate: logged in? " + sp.getBoolean("logged in", false));

        if (sp.getBoolean("login", false) == true) {
            intent = new Intent(this, MainActivity.class);
        }
        else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
