package com.example.dayplanner;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailSignInActivity extends AppCompatActivity {

    EditText editEmail, editPassword;
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

        Log.d("USR", editEmail.getText().toString().trim() + " " + editPassword.getText().toString().trim());
        btnRegister = findViewById(R.id.btn_register);

        firebaseAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(EmailSignInActivity.this, "Email or Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailSignInActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EmailSignInActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            Log.d("USR", "EXCEPTION: " + task.getException().toString());
                        }
                        Intent intent = new Intent(EmailSignInActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
