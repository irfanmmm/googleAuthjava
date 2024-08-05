package com.example.googleauthenticationapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    private SignInClient ontaSignInClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setContentView(R.id.imageView2);

        textView = findViewById(R.id.textView);
        ontaSignInClient = Identity.getSignInClient(this);

        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId("870896567149-49fk5vip1spfhqgnaepbh0j4slrq7on6.apps.googleusercontent.com") // TODO
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();
    }

    public void buttonGoogleSignIn(View view){
        ontaSignInClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("TAG", "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "SignIn failed: " + e.getLocalizedMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_ONE_TAP:
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        SignInCredential credential = ontaSignInClient.getSignInCredentialFromIntent(data);
                        String idToken = credential.getGoogleIdToken();
                        String username = credential.getId();
                        String password = credential.getPassword();
                        String displayName = credential.getDisplayName();
                        Uri profilePic = credential.getProfilePictureUri();

                        System.out.print(profilePic + "  hellow");
                        textView.setText(displayName); // outputs to set display
                        if (idToken != null) {
                            // Use ID token to authenticate with your backend
                            Log.d("TAG", "Got ID token.");
                        } else if (password != null) {
                            // Use username and password to authenticate with your backend
                            Log.d("TAG", "Got password.");
                        }
                    } catch (ApiException e) {
                        Log.e("TAG", "ApiException: " + e.getStatusCode());
                        textView.setText(e.toString());
                    }
                } else {
                    Log.d("TAG", "Result code is not OK or data is null.");
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }
}