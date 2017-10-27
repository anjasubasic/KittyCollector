package com.example.anja.lab1;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * Created by jennyseong on 10/26/17.
 */

public class SuccessActivity extends AppCompatActivity {
    int numPet;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        ImageView catPicture = findViewById(R.id.catPic);
        TextView successDetail = findViewById(R.id.successDetail);
        Button againButton = findViewById(R.id.againButton);
        Button doneButton = findViewById(R.id.doneButton);
        numPet = 0;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Resources res = getResources();
        String detail = String.format(res.getString(R.string.detailMessage),
                sp.getString("catName", ""));
        successDetail.setText(detail);

        Picasso.with(this).load(sp.getString("catUrl", "")).into(catPicture);
        catPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numPet < 1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.meow),
                            Toast.LENGTH_SHORT).show();
                    numPet++;
                } else if (numPet < 3) {
                    Toast.makeText(getApplicationContext(), getString(R.string.purr),
                            Toast.LENGTH_SHORT).show();
                    numPet++;
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.growl),
                            Toast.LENGTH_SHORT).show();
                    numPet++;
                }
            }
        });


        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuccessActivity.this, MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // backstack navigation referenced from
                // https://stackoverflow.com/questions/13632480/android-build-a-notification-taskstackbuilder-addparentstack-not-working
                TaskStackBuilder stack = TaskStackBuilder.create(getApplicationContext());
                stack.addParentStack(MapActivity.class);
                stack.addNextIntent(intent);
                startActivity(intent);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuccessActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}
