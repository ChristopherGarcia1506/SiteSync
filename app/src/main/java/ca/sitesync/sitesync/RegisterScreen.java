package ca.sitesync.sitesync;

import static android.content.ContentValues.TAG;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        EditText firstname = findViewById(R.id.firstname);
        EditText lastname = findViewById(R.id.lastname);
        EditText emailRegister = findViewById(R.id.emailInput);
        EditText passwordRegister = findViewById(R.id.passwordInput);
        EditText passwordConfirm = findViewById(R.id.passwordConfirm);
        Button registerbutton = findViewById(R.id.RegisterButton1);

        // This is code for the Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();



        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredfirst = firstname.getText().toString().trim();
                String enteredlast = lastname.getText().toString().trim();
                String enteredEmail = emailRegister.getText().toString().trim();
                String enteredPassword = passwordRegister.getText().toString().trim();
                String ConfirmedPassword = passwordConfirm.getText().toString().trim();
                if (enteredPassword.equals(ConfirmedPassword)) {
                    // Passwords match — proceed to save
                    Map<String, Object> account = new HashMap<>();
                    account.put("first", enteredfirst);
                    account.put("last", enteredlast);
                    account.put("email", enteredEmail);
                    account.put("password", enteredPassword);

                    if (!isValidEmail(enteredEmail)) {
                        Toast.makeText(RegisterScreen.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("Accounts")
                            .add(account)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    Toast.makeText(RegisterScreen.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterScreen.this, LoginScreen.class));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                    Toast.makeText(RegisterScreen.this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Passwords do not match — show error
                    Toast.makeText(RegisterScreen.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    public static boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(emailPattern);
    }
}