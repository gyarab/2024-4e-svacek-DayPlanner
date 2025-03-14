package com.example.dayplanner.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dayplanner.R;
import com.example.dayplanner.auth.google.GoogleLoginFragment;
import com.example.dayplanner.auth.login.EmailLoginActivity;
import com.example.dayplanner.auth.signin.EmailSignInActivity;


public class AuthenticationActivity extends AppCompatActivity {

    Button emailButton;
    TextView textViewLogin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authentication);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        /*FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());*/

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new GoogleLoginFragment())
                    .commit();
        }

        emailButton = findViewById(R.id.email_button);

        emailButton.setBackgroundResource(R.drawable.email_button_background);
        emailButton.setBackgroundTintList(null);

        //GoogleLoginActivity googleLoginActivity = new GoogleLoginActivity();

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthenticationActivity.this, EmailSignInActivity.class);
                startActivity(intent);
            }
        });

        textViewLogin = findViewById(R.id.textview_login);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthenticationActivity.this, EmailLoginActivity.class);
                startActivity(intent);
            }
        });

    }
}