package com.example.dayplanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton, signInButton;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_email_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");

        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_login);
        signInButton = findViewById(R.id.btn_signin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                mAuth = FirebaseAuth.getInstance();

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if((email.length() == 0) || (password.length() == 0)) {
                    Toast.makeText(EmailLoginActivity.this, "Email or Password is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.hide();
                        if(task.isSuccessful()) {
                            Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(EmailLoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmailLoginActivity.this, AuthenticationActivity.class);
                startActivity(intent);
            }
        });

    }
}