package com.example.dayplanner.auth.login;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dayplanner.R;
import com.example.dayplanner.auth.AuthenticationActivity;
import com.example.dayplanner.auth.signin.EmailSignInActivity;
import com.example.dayplanner.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailLoginActivity extends AppCompatActivity {

    TextView signInButton;
    EditText emailEditText, passwordEditText;
    Button loginButton, sendVerificationLink;
    FirebaseAuth mAuth;

    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EmailLoginActivity.this,
                                        "Verification email sent! Please check your inbox.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EmailLoginActivity.this,
                                        "Failed to send verification email: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_email_login);

        mAuth = FirebaseAuth.getInstance();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");

        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_login);
        signInButton = findViewById(R.id.btn_signin);

        /**Button for resending verification for user**/
        sendVerificationLink = findViewById(R.id.btn_send_verification);
        sendVerificationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth.getCurrentUser() != null) {
                    sendVerificationEmail(mAuth.getCurrentUser());
                } else {
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();

                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(EmailLoginActivity.this, "Please enter email and password to resend verification.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> signInTask) {
                                    if (signInTask.isSuccessful()) {
                                        sendVerificationEmail(mAuth.getCurrentUser());
                                    } else {
                                        Toast.makeText(EmailLoginActivity.this,
                                                "Sign in failed: Please wait - " + signInTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

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
                            if(mAuth.getCurrentUser().isEmailVerified()) {
                                Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(EmailLoginActivity.this, "Please verify your email address", Toast.LENGTH_SHORT).show();
                            }
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