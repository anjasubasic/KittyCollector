package com.example.anja.lab1;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
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

        Bundle extras = getIntent().getExtras();
        Resources res = getResources();
        String detail = String.format(res.getString(R.string.detailMessage), extras.getString("name"));
        successDetail.setText(detail);

        Picasso.with(this).load(extras.getString("picUrl")).into(catPicture);
        catPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Toast toast;
                if (numPet < 1) {
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.meow),
                            Toast.LENGTH_SHORT);
                } else if (numPet < 5) {
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.purr),
                            Toast.LENGTH_SHORT);
                } else {
                    toast = Toast.makeText(getApplicationContext(), getString(R.string.growl),
                            Toast.LENGTH_SHORT);
                }
                numPet++;

                // showing short toasts referenced from
                // https://stackoverflow.com/questions/3775074/set-toast-appear-length/9715422#9715422
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 1000);
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