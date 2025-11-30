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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.auth.AuthResult;
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

    // Google Sign in Variables
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    // Preferecences Variables
    private CheckBox rememberMeCheckbox;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    // Remember me variables
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_EMPLOYER = "isEmployer";
    public static Boolean isEmployer = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        // Initialize Shared Preferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Initialize Firebase Auth FIRST
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Google Sign-In
        initializeGoogleSignIn();

        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginbutton = findViewById(R.id.loginButton);
        Button registerbutton = findViewById(R.id.RegisterButton);
        Button googleSignIn = findViewById(R.id.loginButton2);
        rememberMeCheckbox = findViewById(R.id.checkBox);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        //Check for remembered user and auto-fill credentials
        checkRememberedUser(emailInput, passwordInput);

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredEmail = emailInput.getText().toString().trim();
                String enteredPassword = passwordInput.getText().toString().trim();

                if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
                    Alertor.toast(LoginScreen.this, "Please enter email and password");
                    return;
                }

                db.collection("Accounts")
                        .whereEqualTo("email", enteredEmail)
                        .whereEqualTo("password", enteredPassword)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {

                                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);

                                        Boolean employerStatus = document.getBoolean("employer");

                                        if (employerStatus != null) {
                                            isEmployer = employerStatus;
                                        } else {
                                            isEmployer = false;
                                        }

                                        Log.d("EMAIL_CHECK", "Account found: " + enteredEmail);

                                        //--- Remember me handler---
                                        handleRememberMe(enteredEmail, enteredPassword, isEmployer);

                                        //ensures connection is established before launching main screen
                                        firebaseAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                if(task.isSuccessful()){
                                                                    handleRememberMe(enteredEmail, enteredPassword,isEmployer);
                                                                    startActivity(new Intent(LoginScreen.this, MainActivity.class));
                                                                    finish();
                                                                }else{
                                                                    Alertor.toast(LoginScreen.this,"Athentication Failed");
                                                                }
                                                            }
                                                        });

                                    } else {
                                        Log.d("EMAIL_CHECK", "Account not found: " + enteredEmail);
                                        Alertor.toast(LoginScreen.this, "Incorrect email or password");
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
                    Alertor.toast(LoginScreen.this, "Google Sign-In not available. Please try again.");
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


    // Check if user credentials are remembered
    private void checkRememberedUser(EditText emailInput, EditText passwordInput) {
        boolean shouldRemember = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);

        if (shouldRemember) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

            emailInput.setText(savedEmail);
            passwordInput.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);

            Log.d("REMEMBER_ME", "Auto-filled credentials for: " + savedEmail);
        }
    }

    private void handleRememberMe(String email, String password,boolean isEmployer) {

        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        editor.putString(KEY_EMAIL, email);

        if (rememberMeCheckbox.isChecked()) {
            // Save long-term credentials
            editor.putBoolean(KEY_REMEMBER_ME, true);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_IS_EMPLOYER, isEmployer);

            Log.d("REMEMBER_ME", "Credentials saved for: " + email);
        } else {

            editor.putBoolean(KEY_REMEMBER_ME, false);
            editor.remove(KEY_PASSWORD);

            Log.d("REMEMBER_ME", "Credentials cleared (except current session email)");
        }

        editor.apply();
    }

    //Clear remembered credentials
    public static void clearRememberedCredentials(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putBoolean(KEY_IS_EMPLOYER, false);
        editor.apply();
        Log.d("REMEMBER_ME", "Credentials cleared on logout");
    }


    // check if user is already logged in
    public static boolean isUserLoggedIn(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }


    // get remembered email
    public static String getRememberedEmail(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return preferences.getString(KEY_EMAIL, "");
    }
    //--- Get Remembered employer status---
    public static boolean getRememberedEmployerStatus(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_IS_EMPLOYER, false);
    }

    private void initializeGoogleSignIn() {
        try {

            // Get the web client ID from resources
            String webClientId = getString(R.string.default_web_client_id);

            // Check if the web client ID is properly set
            if (webClientId.equals("YOUR_WEB_CLIENT_ID") || webClientId.isEmpty()) {
                Log.e(TAG, "Web Client ID not set in strings.xml");
                Alertor.toast(this, "Google Sign-In configuration error");
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
            Alertor.toast(this, "Error initializing Google Sign-In");
        }
    }

    private void signInWithGoogle() {
        if (googleSignInClient == null) {
            Alertor.toast(this, "Google Sign-In not available");
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


                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                    editor.putString(KEY_EMAIL, account.getEmail());

                    if (rememberMeCheckbox.isChecked()) {
                        editor.putBoolean(KEY_REMEMBER_ME, true);
                        editor.apply();
                    } else {
                        editor.putBoolean(KEY_REMEMBER_ME, false);
                        editor.apply();
                    }
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Alertor.toast(this, "Google Sign-In failed: No account data");
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Alertor.toast(this, "Google sign in failed: " + e.getStatusCode());
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
                            checkEmployerStatusAndProceed(user.getEmail());
                            Alertor.toast(LoginScreen.this, "Signed in as: " + user.getEmail());
                            startActivity(new Intent(LoginScreen.this, MainActivity.class));
                            finish();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Alertor.toast(LoginScreen.this, "Authentication failed.");
                        }
                    }
                });
    }

    private void checkEmployerStatusAndProceed(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Accounts")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            Boolean employerStatus = document.getBoolean("employer");
                            boolean isEmployer = employerStatus != null ? employerStatus : false;

                            // Save employer status for Google sign-in
                            editor.putBoolean(KEY_IS_EMPLOYER, isEmployer);
                            editor.apply();

                            Log.d("GOOGLE_SIGNIN", "Employer status saved: " + isEmployer);
                        }

                        Alertor.toast(LoginScreen.this, "Signed in as: " + email);
                        startActivity(new Intent(LoginScreen.this, MainActivity.class));
                        finish();
                    }
                });
    }



    @Override
    protected void onStart() {
        super.onStart();
        boolean shouldRemember = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Only auto-login if user explicitly chose "Remember Me" AND is still logged in
        if (shouldRemember && isLoggedIn && currentUser != null) {
            Log.d("REMEMBER_ME", "Auto-login triggered for remembered user: " + currentUser.getEmail());
            startActivity(new Intent(LoginScreen.this, MainActivity.class));
            finish();
        }
        else {
            Log.d("REMEMBER_ME", "Stay on login screen. Remember: " + shouldRemember +
                    ", LoggedIn: " + isLoggedIn + ", FirebaseUser: " + (currentUser != null));
        }
    }
}