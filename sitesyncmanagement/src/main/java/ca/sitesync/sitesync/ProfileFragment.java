/*
Anthony Mancia (N01643670) OCB
Chris Garcia (N01371506) 0CA
Ngoc Le (N01643011) 0CA
Tyler Meira (N01432291) 0CA
*/
package ca.sitesync.sitesync;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //---Logic to retrieve User Fields and display them on screen ---
        if (user != null) {
            String userId = user.getUid();

            TextView usersFullName = view.findViewById(R.id.UsersName);
            TextView usersEmail = view.findViewById(R.id.UserEmail);
            TextView usersPhoneNo = view.findViewById(R.id.UserPhone);
            TextView usersOrganization = view.findViewById(R.id.UserOrganization);

            db.collection("Accounts")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get data from document
                                String firstName = document.getString("firstname");
                                String lastName = document.getString("lastname");
                                String email = document.getString("email");
                                String phoneNumber = document.getString("phonenumber");
                                String organization = document.getString("organization");

                                // Set the text views with retrieved data
                                if (firstName != null) usersFullName.setText(firstName+" "+lastName);
                                if (email != null) usersEmail.setText(email);
                                if (phoneNumber != null) usersPhoneNo.setText(phoneNumber);
                                if (organization != null) usersOrganization.setText(organization);

                            } else {
                                Log.d("ProfileFragment", "No such document");
                            }
                        } else {
                            Log.d("ProfileFragment", "get failed with ", task.getException());
                        }
                    });
        }

        //--- Buttons---
        Button logoutbtn = view.findViewById(R.id.LogOutButton);
        Button changePasswordBtn = view.findViewById(R.id.ChangePassword);
        Button editProfileBtn = view.findViewById(R.id.EditProfile);
        //---LogOut Button---

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogout();
            }
        });

        //---Edit Profile Button---

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditProfileFragment editProfileFragment = new EditProfileFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, editProfileFragment)
                        .commit();
            }
        });

        //---Change Password Button ---

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });

        return view;

    }
    private void showChangePasswordDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Prevent password change for users signed in with Google/other providers
        if (user != null && !user.getProviderData().get(user.getProviderData().size() - 1).getProviderId().equals("password")) {
            Alertor.toast(getContext(), "Password cannot be changed for accounts signed in with other providers.");
            return;
        }

        // Inflate the custom dialog layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.change_password, null);

        final EditText oldPasswordEditText = dialogView.findViewById(R.id.oldPasswordEditText);
        final EditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        final EditText confirmNewPasswordEditText = dialogView.findViewById(R.id.confirmNewPasswordEditText);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setTitle("Change Password")
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This is intentionally left blank. We will override it to prevent the dialog from closing on error.
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Override the positive button's OnClickListener
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String oldPassword = oldPasswordEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();
                String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

                // 1. Validate input fields
                if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                    Alertor.toast(getContext(), "All fields are required.");
                    return; // Keep dialog open
                }

                if (!newPassword.equals(confirmNewPassword)) {
                    confirmNewPasswordEditText.setError("Passwords do not match.");
                    return; // Keep dialog open
                }

                if (newPassword.length() < 6) {
                    newPasswordEditText.setError("Password must be at least 6 characters.");
                    return; // Keep dialog open
                }

                // All validation passed, proceed with Firebase
                changeUserPassword(user, oldPassword, newPassword, dialog);
            });
        });

        dialog.show();
    }

    private void changeUserPassword(FirebaseUser user, String oldPassword, String newPassword, DialogInterface dialog) {
        // 2. Re-authenticate the user with their old password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                // 3. If re-authentication is successful, update the password
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Alertor.toast(getContext(), "Password changed successfully.");
                        dialog.dismiss(); // Close dialog on success
                    } else {
                        Alertor.toast(getContext(), "Error: " + updateTask.getException().getMessage());
                    }
                });
            } else {
                Alertor.toast(getContext(), "Authentication failed. Incorrect old password.");
            }
        });
    }
    //--- Signout Logic ---
    private void performLogout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear the Remember Me SharedPreferences
        LoginScreen.clearRememberedCredentials(requireContext());

        // Sign out from Google as well
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            googleSignInClient.signOut();
        } catch (Exception e) {
            Log.e("LOGOUT", "Google sign-out failed", e);
        }

        // Redirect to login activity
        Intent intent = new Intent(requireActivity(), LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();

        Alertor.toast(requireContext(), "Logged out successfully");

    }
}