package com.example.anja.lab1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Anja on 10/4/2017.
 */

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    TextView createAccountTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onLoginClicked(); }
        });

        createAccountTxtView = (TextView) findViewById(R.id.create_account_txt);
        createAccountTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onNewAccountClicked(); }
        });
    }

    private void onNewAccountClicked() {
        android.support.v4.app.Fragment registerAccountFragment = new RegisterAccountFragment();
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.add(R.id.fragment_container, registerAccountFragment).commit();
    }

    private void onLoginClicked() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(intent);
    }
}
