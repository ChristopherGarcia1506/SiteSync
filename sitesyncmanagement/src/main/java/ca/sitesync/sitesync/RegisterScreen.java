/*
Anthony Mancia (N01643670) OCB
Chris Garcia (N01371506) 0CA
Ngoc Le (N01643011) 0CA
Tyler Meira (N01432291) 0CA
*/
package ca.sitesync.sitesync;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterScreen extends AppCompatActivity {
    // Define password validation patterns
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         // at least 1 digit
                    "(?=.*[a-z])" +         // at least 1 lower case letter
                    "(?=.*[A-Z])" +         // at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      // any letter
                    "(?=.*[@#$%^&+=!.])" +    // at least 1 special character
                    "(?=\\S+$)" +           // no white spaces
                    ".{6,}" +               // at least 6 characters
                    "$");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        EditText firstname = findViewById(R.id.firstname);
        EditText lastname = findViewById(R.id.lastname);
        EditText emailRegister = findViewById(R.id.emailInput);
        EditText passwordRegister = findViewById(R.id.passwordInput);
        EditText passwordConfirm = findViewById(R.id.passwordConfirm);
        EditText address = findViewById(R.id.address);
        EditText organization = findViewById(R.id.Organization);
        EditText phonenumber = findViewById(R.id.phonenumber);
        CheckBox employer = findViewById(R.id.employerCheckBox);
        Button registerbutton = findViewById(R.id.RegisterButton1);


        FirebaseFirestore db = FirebaseFirestore.getInstance();



        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredfirstname = firstname.getText().toString().trim();
                String enteredlastname = lastname.getText().toString().trim();
                boolean isEmployer = employer.isChecked();
                String enteredaddress = address.getText().toString().trim();
                String enteredorganization = organization.getText().toString().trim();
                String enteredphonenumber = phonenumber.getText().toString().trim();
                String enteredEmail = emailRegister.getText().toString().trim();
                String enteredPassword = passwordRegister.getText().toString().trim();
                String ConfirmedPassword = passwordConfirm.getText().toString().trim();

                if (enteredfirstname.isEmpty() || enteredlastname.isEmpty()) {
                    Toast.makeText(RegisterScreen.this, R.string.please_enter_your_full_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (enteredaddress.isEmpty()) {
                    Toast.makeText(RegisterScreen.this, R.string.please_enter_your_address, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (enteredorganization.isEmpty()) {
                    Toast.makeText(RegisterScreen.this, R.string.please_enter_your_organization, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!ValidationUtils.isValidEmail(enteredEmail)) {
                    Toast.makeText(RegisterScreen.this, R.string.please_enter_a_valid_email_address, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!ValidationUtils.isValidPhoneNumber(enteredphonenumber)) {
                    Toast.makeText(RegisterScreen.this, R.string.please_enter_a_valid_10_digit_phone_number, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidPassword(enteredPassword)) {
                    passwordRegister.setError(getString(R.string.password_must_be_at_least_6_characters_and_include_an_uppercase_letter_a_number_and_a_special_character));
                    Toast.makeText(RegisterScreen.this, R.string.please_enter_a_stronger_password, Toast.LENGTH_LONG).show();
                    return;
                }
                if (enteredPassword.equals(ConfirmedPassword)) {

                    //On registration, add account to Authentication in firestore.
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        String userId = user.getUid();

                                        Map<String, Object> account = new HashMap<>();
                                        account.put("firstname", enteredfirstname);
                                        account.put("lastname", enteredlastname);
                                        account.put("employer", isEmployer);
                                        account.put("address", enteredaddress);
                                        account.put("organization", enteredorganization);
                                        account.put("phonenumber", enteredphonenumber);
                                        account.put("email", enteredEmail);
                                        account.put("password", enteredPassword);


                                        db.collection("Accounts")
                                                .document(userId)
                                                .set(account)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Log.d(TAG, "DocumentSnapshot added with ID: " + userId);
                                                        Toast.makeText(RegisterScreen.this, R.string.account_created_successfully, Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(RegisterScreen.this, LoginScreen.class));
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding document", e);
                                                        Toast.makeText(RegisterScreen.this, R.string.failed_to_create_account, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // Firebase Auth failed
                                        Toast.makeText(RegisterScreen.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // Passwords do not match — show error
                    Toast.makeText(RegisterScreen.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}