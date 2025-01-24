package com.example.dayplanner.auth.google;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dayplanner.R;
import com.example.dayplanner.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleLoginFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Show ProgressDialog
                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Signing in using Google");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);  // Make dialog non-cancelable
                progressDialog.show();

                if (result.getResultCode() == getActivity().RESULT_OK) {
                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount googleSignInAccount = accountTask.getResult(Exception.class);
                        if (googleSignInAccount != null) {
                            firebaseAuthWithGoogle(googleSignInAccount, progressDialog);  // Pass the dialog here
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Sign-in failed", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();  // Dismiss the progress dialog if sign-in fails
                    }
                } else {
                    progressDialog.dismiss();  // Dismiss if result is not OK
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_google_login, container, false);

        FirebaseApp.initializeApp(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions);

        view.findViewById(R.id.signIn).setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(signInIntent);
        });

        return view;
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account, ProgressDialog progressDialog) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            progressDialog.dismiss();  // Dismiss the dialog after the authentication is complete

            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Signed in successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MainActivity.class));
                requireActivity().finish();
            } else {
                Toast.makeText(getActivity(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}