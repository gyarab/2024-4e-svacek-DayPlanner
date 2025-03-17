package com.example.dayplanner.auth.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dayplanner.auth.login.EmailLoginActivity;
import com.example.dayplanner.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailSignInActivity extends AppCompatActivity {

    EditText editEmail, editPassword, editConfirmPassword;
    Button btnRegister;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_in);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");

        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        btnRegister = findViewById(R.id.btn_register);

        firebaseAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String confirmPassword = editConfirmPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(EmailSignInActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(EmailSignInActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            handleEmailVerification(
                                    firebaseAuth,
                                    "User registered successfully, Please verify your email address",
                                    "Something went wrong"
                            );
                        } else {
                            Toast.makeText(EmailSignInActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            Log.d("USR", "EXCEPTION: " + task.getException().toString());
                        }
                        Intent intent = new Intent(EmailSignInActivity.this, EmailLoginActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    public void handleEmailVerification(FirebaseAuth firebaseAuth, String message_on_successful, String message_on_not_successful) {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(EmailSignInActivity.this, message_on_successful, Toast.LENGTH_SHORT).show();
                        editEmail.setText("");
                        editPassword.setText("");
                        editConfirmPassword.setText("");
                    } else {
                        Toast.makeText(EmailSignInActivity.this, message_on_not_successful, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
