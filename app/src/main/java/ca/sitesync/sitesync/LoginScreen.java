/*
Anthony Mancia (N01643670) OCB
Chris Garcia (N01371506) 0CA
Ngoc Le (N01643011) 0CA
Tyler Meira (N01432291) 0CA
*/
package ca.sitesync.sitesync;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class LoginScreen extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private void showNotification(String message, String action){
        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(action.equals(getString(R.string.try_again))){
                            askForPermission();
                        }
                    }
                })
                .show();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showNotification(getString(R.string.permission_granted), getString(R.string.dismiss));
                } else {
                    showNotification(getString(R.string.permission_denied), getString(R.string.try_again));
                }
            });

    protected void askForPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        else{
            showNotification(getString(R.string.permission_granted), getString(R.string.dismiss));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        // Initialize Firebase Auth FIRST
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Google Sign-In with error handling
        initializeGoogleSignIn();

        askForPermission();

        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginbutton = findViewById(R.id.loginButton);
        Button registerbutton = findViewById(R.id.RegisterButton);
        Button googleSignIn = findViewById(R.id.loginButton2);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredEmail = emailInput.getText().toString().trim();
                String enteredPassword = passwordInput.getText().toString().trim();
                db.collection("Accounts")
                        .whereEqualTo("email", enteredEmail)
                        .whereEqualTo("password", enteredPassword)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        Log.d("EMAIL_CHECK", "Account found: " + enteredEmail);
                                        startActivity(new Intent(LoginScreen.this, MainActivity.class));
                                    } else {
                                        Log.d("EMAIL_CHECK", "Account not found: " + enteredEmail);
                                        Toast.makeText(LoginScreen.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.w("EMAIL_CHECK", "Query failed.", task.getException());
                                }
                            }
                        });
            }
        });

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (googleSignInClient != null) {
                    signInWithGoogle();
                } else {
                    Toast.makeText(LoginScreen.this, "Google Sign-In not available. Please try again.", Toast.LENGTH_SHORT).show();
                    // Try to reinitialize
                    initializeGoogleSignIn();
                }
            }
        });

        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this, RegisterScreen.class));
            }
        });
    }

    private void initializeGoogleSignIn() {
        try {
            // Get the web client ID from resources
            String webClientId = getString(R.string.default_web_client_id);

            // Check if the web client ID is properly set
            if (webClientId.equals("YOUR_WEB_CLIENT_ID") || webClientId.isEmpty()) {
                Log.e(TAG, "Web Client ID not set in strings.xml");
                Toast.makeText(this, "Google Sign-In configuration error", Toast.LENGTH_LONG).show();
                return;
            }

            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "Google Sign-In initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Google Sign-In", e);
            Toast.makeText(this, "Error initializing Google Sign-In", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            Toast.makeText(this, "Google Sign-In not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Google Sign-In failed: No account data", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(LoginScreen.this, "Signed in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginScreen.this, MainActivity.class));
                            finish();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginScreen.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginScreen.this, MainActivity.class));
            finish();
        }
    }
}